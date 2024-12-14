package com.twins.core.global.model.user.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.minecraftsolutions.utils.ItemSerializer;
import com.twins.core.CorePlugin;
import com.twins.core.global.model.craft.Craft;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.global.model.user.adapter.CraftAdapter;
import com.twins.core.global.model.user.adapter.UserAdapter;
import com.twins.core.monster.WaveRunnable;
import com.twins.core.monster.type.BuffType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UserRepository implements UserFoundationRepository {

    private final Database database;

    private final UserAdapter adapter = new UserAdapter();

    private final CraftAdapter craftAdapter = new CraftAdapter();

    public UserRepository(Database database) {
        this.database = database;
    }

    @Override
    public void setup() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user (nickname VARCHAR(16) PRIMARY KEY, kills INTEGER, playTime FLOAT, deathlocation VARCHAR(255), tutorialDone BOOLEAN, languageType CHAR(2), forceLanguage BOOLEAN, beginner BOOLEAN, quit BOOLEAN, villainPoints INTEGER, conquerorPoints INTEGER, waveTime INTEGER)")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_buffs (nickname VARCHAR(16), buffType VARCHAR(20), buffTime BIGINT, PRIMARY KEY(nickname, buffType), FOREIGN KEY (nickname) REFERENCES global_user(nickname))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_mailboxes (nickname VARCHAR(16), mailboxType VARCHAR(20), mailboxLocation VARCHAR(255), currentTime BIGINT, PRIMARY KEY (nickname, mailboxType, mailboxLocation, currentTime), FOREIGN KEY (nickname) REFERENCES global_user(nickname))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_recipes (nickname VARCHAR(16), recipe VARCHAR(255), recipeAmount INTEGER, PRIMARY KEY (nickname, recipe), FOREIGN KEY (nickname) REFERENCES global_user(nickname))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_crafts (nickname VARCHAR(16), craftItem TEXT, craftAmount INTEGER, craftDelay INTEGER, FOREIGN KEY (nickname) REFERENCES global_user(nickname))")
                    .write();
        }
    }

    @Override
    public void deleteExpiredBuffs() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("DELETE FROM global_user_buffs WHERE buffTime <= ?")
                    .write(statement -> statement.set(1, System.currentTimeMillis()));
        }
    }


    @Override
    public CompletableFuture<List<Object[]>> insertOrUpdate(Collection<GlobalUser> globalUsers) {
        return CompletableFuture.supplyAsync(() -> {

            try (DatabaseExecutor executor = database.execute()) {

                executor
                        .query("INSERT INTO global_user (nickname, kills, playTime, deathLocation, tutorialDone, languageType, forceLanguage, beginner, quit, villainPoints, conquerorPoints, waveTime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE kills = VALUES(kills), playTime = VALUES(playTime), deathLocation = VALUES(deathLocation), tutorialDone = VALUES(tutorialDone), languageType = VALUES(languageType), forceLanguage = VALUES(forceLanguage), beginner = VALUES(beginner), quit = VALUES(quit), villainPoints = VALUES(villainPoints), conquerorPoints = VALUES(conquerorPoints), waveTime = VALUES(waveTime)")
                        .batch(globalUsers, ((globalUser, statement) -> {
                            statement.set(1, globalUser.getName());
                            statement.set(2, globalUser.getKills());
                            statement.set(3, globalUser.getPlayTime());
                            statement.set(4, globalUser.getDeathLocation());
                            statement.set(5, globalUser.isTutorialDone());
                            statement.set(6, globalUser.getLanguageType().name());
                            statement.set(7, globalUser.isForceLanguage());
                            statement.set(8, globalUser.isBeginner());
                            statement.set(9, globalUser.isQuit());
                            statement.set(10, globalUser.getVillainPoints());
                            statement.set(11, globalUser.getConquerorPoints());
                            statement.set(12, WaveRunnable.WAVE_PLAYERS.get(globalUser.getName()));
                        }));

                executor.query("INSERT INTO global_user_buffs (nickname, buffType, buffTime) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE buffTime = VALUES(buffTime)")
                        .batch(globalUsers.stream()
                                .flatMap(globalUser -> {

                                    Map<BuffType, Long> userMap = new HashMap<>(globalUser.getPersonalBuffs());

                                    if (!userMap.isEmpty()) {
                                        return userMap.entrySet().stream().map(entry -> new Object[]{globalUser.getName(), entry.getKey().name(), entry.getValue()});
                                    }

                                    return java.util.stream.Stream.empty();
                                }).toList(), (params, statement) -> {
                            statement.set(1, params[0]);
                            statement.set(2, params[1]);
                            statement.set(3, params[2]);
                        });

                Map<Boolean, List<Object[]>> groupedMailboxes = globalUsers.stream()
                        .flatMap(globalUser -> globalUser.getMailboxes().stream()
                                .map(mailbox -> new AbstractMap.SimpleEntry<>(mailbox, globalUser.getName())))
                        .map(entry -> {
                            String userName = entry.getValue();
                            Mailbox mailbox = entry.getKey();
                            return new Object[]{
                                    userName,
                                    mailbox
                            };
                        })
                        .collect(Collectors.partitioningBy(params -> ((Mailbox) params[1]).isToDelete(),
                                Collectors.mapping(params -> Arrays.copyOfRange(params, 0, 2), Collectors.toList())));

                List<Object[]> mailboxesToInsert = groupedMailboxes.get(false);

                if (!mailboxesToInsert.isEmpty()) {
                    executor.query("INSERT IGNORE INTO global_user_mailboxes (nickname, mailboxType, mailboxLocation, currentTime) VALUES (?,?,?,?)")
                            .batch(mailboxesToInsert, (params, statement) -> {
                                Mailbox mailbox = (Mailbox) params[1];
                                statement.set(1, params[0]);
                                statement.set(2, mailbox.getType().name());
                                statement.set(3, mailbox.getLocation());
                                statement.set(4, mailbox.getCurrentTime());
                            });
                }

                List<Object[]> mailboxesToDelete = groupedMailboxes.get(true);

                if (!mailboxesToDelete.isEmpty()) {
                    executor.query("DELETE FROM global_user_mailboxes WHERE nickname = ? AND mailboxType = ? AND mailboxLocation = ? AND currentTime = ?")
                            .batch(mailboxesToDelete, (params, statement) -> {
                                Mailbox mailbox = (Mailbox) params[1];
                                statement.set(1, params[0]);
                                statement.set(2, mailbox.getType().name());
                                statement.set(3, mailbox.getLocation());
                                statement.set(4, mailbox.getCurrentTime());
                            });

                    Map<String, GlobalUser> userMap = globalUsers.stream()
                            .collect(Collectors.toMap(GlobalUser::getName, Function.identity()));

                    for (Object[] params : mailboxesToDelete) {

                        String nickname = (String) params[0];
                        Mailbox mailboxToRemove = (Mailbox) params[1];

                        GlobalUser globalUser = userMap.get(nickname);

                        if (globalUser != null) {
                            globalUser.getMailboxes().remove(mailboxToRemove);
                        }

                    }

                }

                executor.query("INSERT INTO global_user_recipes (nickname, recipe, recipeAmount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE recipeAmount = VALUES(recipeAmount)")
                        .batch(globalUsers.stream()
                                .flatMap(globalUser -> {

                                    Map<String, Integer> recipes = new HashMap<>(globalUser.getRecipes());

                                    if (!recipes.isEmpty()) {
                                        return recipes.entrySet().stream().map(entry -> new Object[]{globalUser.getName(), entry.getKey(), entry.getValue()});
                                    }

                                    return java.util.stream.Stream.empty();

                                }).toList(), (params, statement) -> {
                            statement.set(1, params[0]);
                            statement.set(2, params[1]);
                            statement.set(3, params[2]);
                        });

                executor.query("DELETE FROM global_user_crafts WHERE nickname = ?")
                        .batch(globalUsers, (globalUser, statement) -> statement.set(1, globalUser.getName()));

                executor.query("INSERT INTO global_user_crafts (nickname, craftItem, craftAmount, craftDelay) VALUES (?, ?, ?, ?)")
                        .batch(globalUsers.stream()
                                .flatMap(globalUser -> {

                                    List<Craft> crafts = new ArrayList<>(globalUser.getCrafts());

                                    if (!crafts.isEmpty()) {
                                        return crafts.stream().map(craft -> new Object[]{globalUser.getName(), ItemSerializer.itemStackToBase64(craft.getItem()), craft.getAmount(), craft.getDelay()});
                                    }

                                    return java.util.stream.Stream.empty();

                                }).toList(), (params, statement) -> {
                            statement.set(1, params[0]);
                            statement.set(2, params[1]);
                            statement.set(3, params[2]);
                            statement.set(4, params[3]);
                        });

                return mailboxesToDelete;
            }

        }, CorePlugin.INSTANCE.getAsyncExecutor()).exceptionally(e -> {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to insert or update globalUsers data", e);
            return null;
        });
    }

    @Override
    public GlobalUser findOne(String nickname) {
        try (DatabaseExecutor executor = database.execute()) {

            GlobalUser globalUser = executor
//                    .query("SELECT A.*, B.buffType, B.buffTime, C.mailboxType, C.mailboxLocation, C.currentTime, D.recipe, D.recipeAmount, E.craftItem, E.craftAmount, E.craftDelay FROM global_user A LEFT JOIN global_user_buffs B ON A.nickname = B.nickname LEFT JOIN global_user_mailboxes C ON A.nickname = C.nickname LEFT JOIN global_user_recipes D ON A.nickname = D.nickname LEFT JOIN global_user_crafts E ON A.nickname = E.nickname WHERE A.nickname = ?")
                    .query("SELECT A.*, B.buffType, B.buffTime, C.mailboxType, C.mailboxLocation, C.currentTime, D.recipe, D.recipeAmount FROM global_user A LEFT JOIN global_user_buffs B ON A.nickname = B.nickname LEFT JOIN global_user_mailboxes C ON A.nickname = C.nickname LEFT JOIN global_user_recipes D ON A.nickname = D.nickname WHERE A.nickname = ?")
                    .readOne(statement -> statement.set(1, nickname), adapter)
                    .orElse(null);

            if (globalUser != null) {

                List<Craft> crafts = executor
                        .query("SELECT * FROM global_user_crafts WHERE nickname = ?")
                        .readMany(statement -> statement.set(1, nickname), craftAdapter, ArrayList::new);

                boolean value = false;

                for (Craft craft : crafts) {

                    if (craft != null) {

                        globalUser.getCrafts().add(craft);

                        if (!value) {
                            globalUser.setNeedRunnable(true);
                            value = true;
                        }

                    }

                }

            }

            return globalUser;
        }
    }

    @Override
    public List<Object[]> findTop() {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT nickname, villainPoints FROM global_user ORDER BY villainPoints DESC LIMIT 5")
                    .readMany(_ -> {}, query -> {
                        String nickname = (String) query.get("nickname");
                        int villainPoints = (int) query.get("villainPoints");
                        return new Object[]{nickname, villainPoints};
                    }, ArrayList::new);
        }
    }

}

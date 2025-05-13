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
import com.twins.core.utils.BukkitUtils;
import com.twins.core.utils.UUIDConverter;

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
                    .query("CREATE TABLE IF NOT EXISTS global_user (uuid BINARY(16) PRIMARY KEY, nickname VARCHAR(16), kills INTEGER, playTime FLOAT, deathlocation VARCHAR(255), firstJoin BOOLEAN, firstStep BOOLEAN, secondStep BOOLEAN, thirdStep BOOLEAN, fourthStep BOOLEAN, fifthStep BOOLEAN, tutorialDone BOOLEAN, languageType CHAR(2), forceLanguage BOOLEAN, beginner BOOLEAN, quit BOOLEAN, villainPoints INTEGER, conquerorPoints INTEGER, waveTime INTEGER, tag VARCHAR(16))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_buffs (uuid BINARY(16), buffType VARCHAR(20), buffTime BIGINT, PRIMARY KEY(uuid, buffType), FOREIGN KEY (uuid) REFERENCES global_user(uuid))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_mailboxes (uuid BINARY(16), mailboxUuid BINARY(16), mailboxType VARCHAR(20), mailboxLocation VARCHAR(255), currentTime BIGINT, rankingType VARCHAR(16), PRIMARY KEY (uuid, mailboxUuid), FOREIGN KEY (uuid) REFERENCES global_user(uuid))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_recipes (uuid BINARY(16), recipe VARCHAR(255), recipeAmount INTEGER, PRIMARY KEY (uuid, recipe), FOREIGN KEY (uuid) REFERENCES global_user(uuid))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS global_user_crafts (uuid BINARY(16), craftItem TEXT, craftAmount INTEGER, craftDelay INTEGER, FOREIGN KEY (uuid) REFERENCES global_user(uuid))")
                    .write();
        } catch (Exception e) {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to setup tables", e);
        }
    }

    @Override
    public void deleteExpiredBuffs() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("DELETE FROM global_user_buffs WHERE buffTime <= ?")
                    .write(statement -> statement.set(1, System.currentTimeMillis()));
        } catch (Exception e) {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to delete expired buffs", e);
        }
    }

    @Override
    public void updateNickname(UUID uuid, String nickname) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("UPDATE global_user SET nickname = ? WHERE uuid = ?")
                    .write(statement -> {
                        statement.set(1, nickname);
                        statement.set(2, UUIDConverter.convert(uuid));
                    });
        } catch (Exception e) {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to update global user nickname", e);
        }
    }

    @Override
    public CompletableFuture<List<Object[]>> insertOrUpdate(Collection<GlobalUser> globalUsers) {
        return CompletableFuture.supplyAsync(() -> {

            try (DatabaseExecutor executor = database.execute()) {

                executor
                        .query("INSERT INTO global_user (uuid, nickname, kills, playTime, deathLocation, firstJoin, firstStep, secondStep, thirdStep, fourthStep, fifthStep, tutorialDone, languageType, forceLanguage, beginner, quit, villainPoints, conquerorPoints, waveTime, tag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE kills = VALUES(kills), playTime = VALUES(playTime), deathLocation = VALUES(deathLocation), firstJoin = VALUES(firstJoin), firstStep = VALUES(firstStep), secondStep = VALUES(secondStep), thirdStep = VALUES(thirdStep), fourthStep = VALUES(fourthStep), fifthStep = VALUES(fifthStep), tutorialDone = VALUES(tutorialDone), languageType = VALUES(languageType), forceLanguage = VALUES(forceLanguage), beginner = VALUES(beginner), quit = VALUES(quit), villainPoints = VALUES(villainPoints), conquerorPoints = VALUES(conquerorPoints), waveTime = VALUES(waveTime), tag = VALUES(tag)")
                        .batch(globalUsers, ((globalUser, statement) -> {
                            statement.set(1, UUIDConverter.convert(globalUser.getUuid()));
                            statement.set(2, globalUser.getName());
                            statement.set(3, globalUser.getKills());
                            statement.set(4, globalUser.getPlayTime());
                            statement.set(5, globalUser.getDeathLocation());
                            statement.set(6, globalUser.isFirstJoin());
                            statement.set(7, globalUser.isFirstStep());
                            statement.set(8, globalUser.isSecondStep());
                            statement.set(9, globalUser.isThirdStep());
                            statement.set(10, globalUser.isFourthStep());
                            statement.set(11, globalUser.isFifthStep());
                            statement.set(12, globalUser.isTutorialDone());
                            statement.set(13, globalUser.getLanguageType().name());
                            statement.set(14, globalUser.isForceLanguage());
                            statement.set(15, globalUser.isBeginner());
                            statement.set(16, globalUser.isQuit());
                            statement.set(17, globalUser.getVillainPoints());
                            statement.set(18, globalUser.getConquerorPoints());
                            statement.set(19, WaveRunnable.WAVE_PLAYERS.get(globalUser.getUuid()));
                            statement.set(20, BukkitUtils.safeEnumConversion(globalUser.getTag()));
                        }));

                executor.query("INSERT INTO global_user_buffs (uuid, buffType, buffTime) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE buffTime = VALUES(buffTime)")
                        .batch(globalUsers.stream()
                                .flatMap(globalUser -> globalUser.getPersonalBuffs().entrySet().stream().map(entry -> new Object[]{UUIDConverter.convert(globalUser.getUuid()), entry.getKey().name(), entry.getValue()})).toList(), (params, statement) -> {
                            statement.set(1, params[0]);
                            statement.set(2, params[1]);
                            statement.set(3, params[2]);
                        });

                Map<Boolean, List<Object[]>> groupedMailboxes = globalUsers.stream()
                        .flatMap(globalUser -> globalUser.getMailboxes().stream()
                                .map(mailbox -> new AbstractMap.SimpleEntry<>(mailbox, globalUser.getUuid())))
                        .map(entry -> {
                            UUID userUuid = entry.getValue();
                            Mailbox mailbox = entry.getKey();
                            return new Object[]{
                                    userUuid,
                                    mailbox
                            };
                        })
                        .collect(Collectors.partitioningBy(params -> ((Mailbox) params[1]).isToDelete(),
                                Collectors.mapping(params -> Arrays.copyOfRange(params, 0, 2), Collectors.toList())));

                List<Object[]> mailboxesToInsert = groupedMailboxes.get(false);

                if (!mailboxesToInsert.isEmpty()) {
                    executor.query("INSERT IGNORE INTO global_user_mailboxes (uuid, mailboxUuid, mailboxType, mailboxLocation, currentTime, rankingType) VALUES (?,?,?,?,?,?)")
                            .batch(mailboxesToInsert, (params, statement) -> {
                                Mailbox mailbox = (Mailbox) params[1];
                                statement.set(1, UUIDConverter.convert((UUID) params[0]));
                                statement.set(2, UUIDConverter.convert(mailbox.getUuid()));
                                statement.set(3, mailbox.getType().name());
                                statement.set(4, mailbox.getLocation());
                                statement.set(5, mailbox.getCurrentTime());
                                statement.set(6, mailbox.getRankingType() == null ? null : mailbox.getRankingType().name());
                            });
                }

                List<Object[]> mailboxesToDelete = groupedMailboxes.get(true);

                if (!mailboxesToDelete.isEmpty()) {
                    executor.query("DELETE FROM global_user_mailboxes WHERE uuid = ? AND mailboxUuid = ?")
                            .batch(mailboxesToDelete, (params, statement) -> {
                                Mailbox mailbox = (Mailbox) params[1];
                                statement.set(1, UUIDConverter.convert((UUID) params[0]));
                                statement.set(2, UUIDConverter.convert(mailbox.getUuid()));
                            });

                    Map<UUID, GlobalUser> userMap = globalUsers.stream()
                            .collect(Collectors.toMap(GlobalUser::getUuid, Function.identity()));

                    for (Object[] params : mailboxesToDelete) {

                        UUID uuid = (UUID) params[0];
                        Mailbox mailboxToRemove = (Mailbox) params[1];

                        GlobalUser globalUser = userMap.get(uuid);

                        if (globalUser != null) {
                            globalUser.getMailboxes().remove(mailboxToRemove);
                        }

                    }

                }

                executor.query("INSERT INTO global_user_recipes (uuid, recipe, recipeAmount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE recipeAmount = VALUES(recipeAmount)")
                        .batch(globalUsers.stream()
                                .flatMap(globalUser -> globalUser.getRecipes().entrySet().stream().map(entry -> new Object[]{globalUser.getUuid(), entry.getKey(), entry.getValue()})).toList(), (params, statement) -> {
                            statement.set(1, UUIDConverter.convert((UUID) params[0]));
                            statement.set(2, params[1]);
                            statement.set(3, params[2]);
                        });

                executor.query("DELETE FROM global_user_crafts WHERE uuid = ?")
                        .batch(globalUsers, (globalUser, statement) -> statement.set(1, UUIDConverter.convert(globalUser.getUuid())));

                executor.query("INSERT INTO global_user_crafts (uuid, craftItem, craftAmount, craftDelay) VALUES (?, ?, ?, ?)")
                        .batch(globalUsers.stream()
                                .flatMap(globalUser -> {

                                    List<Craft> crafts = new ArrayList<>(globalUser.getCrafts());

                                    if (!crafts.isEmpty()) {
                                        return crafts.stream().map(craft -> new Object[]{globalUser.getUuid(), ItemSerializer.itemStackToBase64(craft.getItem()), craft.getAmount(), craft.getDelay()});
                                    }

                                    return java.util.stream.Stream.empty();

                                }).toList(), (params, statement) -> {
                            statement.set(1, UUIDConverter.convert((UUID) params[0]));
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
    public List<Object[]> insertOrUpdateOnDisable(Collection<GlobalUser> globalUsers) {
        try (DatabaseExecutor executor = database.execute()) {

            executor
                    .query("INSERT INTO global_user (uuid, nickname, kills, playTime, deathLocation, firstJoin, firstStep, secondStep, thirdStep, fourthStep, fifthStep, tutorialDone, languageType, forceLanguage, beginner, quit, villainPoints, conquerorPoints, waveTime, tag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE kills = VALUES(kills), playTime = VALUES(playTime), deathLocation = VALUES(deathLocation), firstJoin = VALUES(firstJoin), firstStep = VALUES(firstStep), secondStep = VALUES(secondStep), thirdStep = VALUES(thirdStep), fourthStep = VALUES(fourthStep), fifthStep = VALUES(fifthStep), tutorialDone = VALUES(tutorialDone), languageType = VALUES(languageType), forceLanguage = VALUES(forceLanguage), beginner = VALUES(beginner), quit = VALUES(quit), villainPoints = VALUES(villainPoints), conquerorPoints = VALUES(conquerorPoints), waveTime = VALUES(waveTime), tag = VALUES(tag)")
                    .batch(globalUsers, ((globalUser, statement) -> {
                        statement.set(1, UUIDConverter.convert(globalUser.getUuid()));
                        statement.set(2, globalUser.getName());
                        statement.set(3, globalUser.getKills());
                        statement.set(4, globalUser.getPlayTime());
                        statement.set(5, globalUser.getDeathLocation());
                        statement.set(6, globalUser.isFirstJoin());
                        statement.set(7, globalUser.isFirstStep());
                        statement.set(8, globalUser.isSecondStep());
                        statement.set(9, globalUser.isThirdStep());
                        statement.set(10, globalUser.isFourthStep());
                        statement.set(11, globalUser.isFifthStep());
                        statement.set(12, globalUser.isTutorialDone());
                        statement.set(13, globalUser.getLanguageType().name());
                        statement.set(14, globalUser.isForceLanguage());
                        statement.set(15, globalUser.isBeginner());
                        statement.set(16, globalUser.isQuit());
                        statement.set(17, globalUser.getVillainPoints());
                        statement.set(18, globalUser.getConquerorPoints());
                        statement.set(19, WaveRunnable.WAVE_PLAYERS.get(globalUser.getUuid()));
                        statement.set(20, BukkitUtils.safeEnumConversion(globalUser.getTag()));
                    }));

            executor.query("INSERT INTO global_user_buffs (uuid, buffType, buffTime) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE buffTime = VALUES(buffTime)")
                    .batch(globalUsers.stream()
                            .flatMap(globalUser -> globalUser.getPersonalBuffs().entrySet().stream().map(entry -> new Object[]{UUIDConverter.convert(globalUser.getUuid()), entry.getKey().name(), entry.getValue()})).toList(), (params, statement) -> {
                        statement.set(1, params[0]);
                        statement.set(2, params[1]);
                        statement.set(3, params[2]);
                    });

            Map<Boolean, List<Object[]>> groupedMailboxes = globalUsers.stream()
                    .flatMap(globalUser -> globalUser.getMailboxes().stream()
                            .map(mailbox -> new AbstractMap.SimpleEntry<>(mailbox, globalUser.getUuid())))
                    .map(entry -> {
                        UUID userUuid = entry.getValue();
                        Mailbox mailbox = entry.getKey();
                        return new Object[]{
                                userUuid,
                                mailbox
                        };
                    })
                    .collect(Collectors.partitioningBy(params -> ((Mailbox) params[1]).isToDelete(),
                            Collectors.mapping(params -> Arrays.copyOfRange(params, 0, 2), Collectors.toList())));

            List<Object[]> mailboxesToInsert = groupedMailboxes.get(false);

            if (!mailboxesToInsert.isEmpty()) {
                executor.query("INSERT IGNORE INTO global_user_mailboxes (uuid, mailboxUuid, mailboxType, mailboxLocation, currentTime, rankingType) VALUES (?,?,?,?,?,?)")
                        .batch(mailboxesToInsert, (params, statement) -> {
                            Mailbox mailbox = (Mailbox) params[1];
                            statement.set(1, UUIDConverter.convert((UUID) params[0]));
                            statement.set(2, UUIDConverter.convert(mailbox.getUuid()));
                            statement.set(3, mailbox.getType().name());
                            statement.set(4, mailbox.getLocation());
                            statement.set(5, mailbox.getCurrentTime());
                            statement.set(6, mailbox.getRankingType() == null ? null : mailbox.getRankingType().name());
                        });
            }

            List<Object[]> mailboxesToDelete = groupedMailboxes.get(true);

            if (!mailboxesToDelete.isEmpty()) {
                executor.query("DELETE FROM global_user_mailboxes WHERE uuid = ? AND mailboxUuid = ?")
                        .batch(mailboxesToDelete, (params, statement) -> {
                            Mailbox mailbox = (Mailbox) params[1];
                            statement.set(1, UUIDConverter.convert((UUID) params[0]));
                            statement.set(2, mailbox.getUuid());
                        });

                Map<UUID, GlobalUser> userMap = globalUsers.stream()
                        .collect(Collectors.toMap(GlobalUser::getUuid, Function.identity()));

                for (Object[] params : mailboxesToDelete) {

                    UUID uuid = (UUID) params[0];
                    Mailbox mailboxToRemove = (Mailbox) params[1];

                    GlobalUser globalUser = userMap.get(uuid);

                    if (globalUser != null) {
                        globalUser.getMailboxes().remove(mailboxToRemove);
                    }

                }

            }

            executor.query("INSERT INTO global_user_recipes (uuid, recipe, recipeAmount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE recipeAmount = VALUES(recipeAmount)")
                    .batch(globalUsers.stream()
                            .flatMap(globalUser -> globalUser.getRecipes().entrySet().stream().map(entry -> new Object[]{globalUser.getUuid(), entry.getKey(), entry.getValue()})).toList(), (params, statement) -> {
                        statement.set(1, UUIDConverter.convert((UUID) params[0]));
                        statement.set(2, params[1]);
                        statement.set(3, params[2]);
                    });

            executor.query("DELETE FROM global_user_crafts WHERE uuid = ?")
                    .batch(globalUsers, (globalUser, statement) -> statement.set(1, UUIDConverter.convert(globalUser.getUuid())));

            executor.query("INSERT INTO global_user_crafts (uuid, craftItem, craftAmount, craftDelay) VALUES (?, ?, ?, ?)")
                    .batch(globalUsers.stream()
                            .flatMap(globalUser -> {

                                List<Craft> crafts = new ArrayList<>(globalUser.getCrafts());

                                if (!crafts.isEmpty()) {
                                    return crafts.stream().map(craft -> new Object[]{globalUser.getUuid(), ItemSerializer.itemStackToBase64(craft.getItem()), craft.getAmount(), craft.getDelay()});
                                }

                                return java.util.stream.Stream.empty();

                            }).toList(), (params, statement) -> {
                        statement.set(1, UUIDConverter.convert((UUID) params[0]));
                        statement.set(2, params[1]);
                        statement.set(3, params[2]);
                        statement.set(4, params[3]);
                    });

            return mailboxesToDelete;
        } catch (Exception e) {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to insert or update globalUsers data on disable", e);
            return null;
        }

    }

    @Override
    public GlobalUser findOne(UUID uuid) throws Exception {
        try (DatabaseExecutor executor = database.execute()) {

            byte[] uuidBytes = UUIDConverter.convert(uuid);

            GlobalUser globalUser = executor
                    .query("SELECT A.*, B.buffType, B.buffTime, C.mailboxUuid, C.mailboxType, C.mailboxLocation, C.currentTime, C.rankingType, D.recipe, D.recipeAmount FROM global_user A LEFT JOIN global_user_buffs B ON A.uuid = B.uuid LEFT JOIN global_user_mailboxes C ON A.uuid = C.uuid LEFT JOIN global_user_recipes D ON A.uuid = D.uuid WHERE A.uuid = ?")
                    .readOne(statement -> statement.set(1, uuidBytes), adapter)
                    .orElse(null);

            if (globalUser != null) {

                List<Craft> crafts = executor
                        .query("SELECT * FROM global_user_crafts WHERE uuid = ?")
                        .readMany(statement -> statement.set(1, uuidBytes), craftAdapter, ArrayList::new);

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
        } catch (Exception e) {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve global user data", e);
            throw e;
        }
    }

    @Override
    public CompletableFuture<Boolean> addMailboxAsync(UUID uuid, Mailbox mailbox) {
        return CompletableFuture.supplyAsync(() -> {

            try (DatabaseExecutor executor = database.execute()) {
                executor.query("INSERT IGNORE INTO global_user_mailboxes (uuid, mailboxUuid, mailboxType, mailboxLocation, currentTime, rankingType) VALUES (?,?,?,?,?,?)")
                        .write(statement -> {
                            statement.set(1, UUIDConverter.convert(uuid));
                            statement.set(2, UUIDConverter.convert(mailbox.getUuid()));
                            statement.set(3, mailbox.getType().name());
                            statement.set(4, mailbox.getLocation());
                            statement.set(5, mailbox.getCurrentTime());
                            statement.set(6, mailbox.getRankingType() == null ? null : mailbox.getRankingType().name());
                        });
            }

            return true;
        }, CorePlugin.INSTANCE.getAsyncExecutor()).exceptionally(e -> {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to add mailbox", e);
            return false;
        });

    }

    @Override
    public List<Object[]> findTop() {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT nickname, conquerorPoints FROM global_user ORDER BY conquerorPoints DESC LIMIT 5")
                    .readMany(_ -> {
                    }, query -> {
                        String nickname = (String) query.get("nickname");
                        int conquerorPoints = (int) query.get("conquerorPoints");
                        return new Object[]{nickname, conquerorPoints};
                    }, ArrayList::new);
        }
    }

}

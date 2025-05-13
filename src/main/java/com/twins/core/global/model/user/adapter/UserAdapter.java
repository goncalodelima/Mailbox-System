package com.twins.core.global.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.twins.core.dailytops.DailyTopType;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.mail.type.MailboxType;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.global.model.user.language.LanguageType;
import com.twins.core.monster.WaveRunnable;
import com.twins.core.monster.type.BuffType;
import com.twins.core.tag.TagEnum;
import com.twins.core.utils.UUIDConverter;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserAdapter implements DatabaseAdapter<GlobalUser> {

    @Override
    public GlobalUser adapt(DatabaseQuery databaseQuery) throws SQLException {

        UUID uuid = UUIDConverter.convert((byte[]) databaseQuery.get("uuid"));
        String name = (String) databaseQuery.get("nickname");
        int kills = (int) databaseQuery.get("kills");
        float playTime = (float) databaseQuery.get("playTime");
        String deathLocation = (String) databaseQuery.get("deathLocation");
        boolean firstJoin = (boolean) databaseQuery.get("firstJoin");
        boolean firstStep = (boolean) databaseQuery.get("firstStep");
        boolean secondStep = (boolean) databaseQuery.get("secondStep");
        boolean thirdStep = (boolean) databaseQuery.get("thirdStep");
        boolean fourthStep = (boolean) databaseQuery.get("fourthStep");
        boolean fifthStep = (boolean) databaseQuery.get("fifthStep");
        boolean tutorialDone = (boolean) databaseQuery.get("tutorialDone");
        boolean beginner = (boolean) databaseQuery.get("beginner");
        boolean forceLanguage = (boolean) databaseQuery.get("forceLanguage");
        boolean quit = (boolean) databaseQuery.get("quit");
        int villainPoints = (int) databaseQuery.get("villainPoints");
        int conquerorPoints = (int) databaseQuery.get("conquerorPoints");
        Integer waveTime = (Integer) databaseQuery.get("waveTime");

        String language = (String) databaseQuery.get("languageType");
        LanguageType languageType;

        if (language.equals("PT")) {
            languageType = LanguageType.PT;
        } else {
            languageType = LanguageType.EN;
        }

        TagEnum tag;

        try {
            tag = TagEnum.valueOf((String) databaseQuery.get("tag"));
        } catch (Exception exception) {
            tag = null;
        }

        GlobalUser globalUser = new GlobalUser(uuid, name, kills, playTime, deathLocation, firstJoin, firstStep, secondStep, thirdStep, fourthStep, fifthStep, tutorialDone, languageType, forceLanguage, beginner, quit, villainPoints, conquerorPoints, ConcurrentHashMap.newKeySet(), tag);

        if (waveTime != null) {
            WaveRunnable.WAVE_PLAYERS.put(uuid, waveTime);
        }

        do {

            String typeName = (String) databaseQuery.get("buffType");

            if (typeName != null) {

                BuffType type = BuffType.valueOf(typeName);
                Long buffTime = (Long) databaseQuery.get("buffTime");

                if (buffTime != null) {
                    globalUser.getPersonalBuffs().put(type, buffTime);
                }

            }

            String type = (String) databaseQuery.get("mailboxType");

            if (type != null) {

                UUID mailboxUuid = UUIDConverter.convert((byte[]) databaseQuery.get("mailboxUuid"));
                MailboxType mailboxType = MailboxType.valueOf(type);
                String location = (String) databaseQuery.get("mailboxLocation");
                long time = (long) databaseQuery.get("currentTime");
                DailyTopType rankingType = databaseQuery.get("rankingType") == null ? null : DailyTopType.valueOf((String) databaseQuery.get("rankingType"));

                globalUser.getMailboxes().add(new Mailbox(mailboxUuid, mailboxType, location, rankingType, time));

            }

            String recipe = (String) databaseQuery.get("recipe");

            if (recipe != null) {

                int recipeAmount = (int) databaseQuery.get("recipeAmount");

                if (!globalUser.hasRecipe(recipe)) {
                    globalUser.getRecipes().put(recipe, recipeAmount);
                }

            }

        } while (databaseQuery.next());


        return globalUser;
    }

}

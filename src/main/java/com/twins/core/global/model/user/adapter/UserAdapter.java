package com.twins.core.global.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.mail.type.MailboxType;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.global.model.user.language.LanguageType;
import com.twins.core.monster.WaveRunnable;
import com.twins.core.monster.type.BuffType;

import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserAdapter implements DatabaseAdapter<GlobalUser> {

    @Override
    public GlobalUser adapt(DatabaseQuery databaseQuery) throws SQLException {

        String name = (String) databaseQuery.get("nickname");
        int kills = (int) databaseQuery.get("kills");
        float playTime = (float) databaseQuery.get("playTime");
        String deathLocation = (String) databaseQuery.get("deathLocation");
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

        GlobalUser globalUser = new GlobalUser(name, kills, playTime, deathLocation, tutorialDone, languageType, forceLanguage, beginner, quit, villainPoints, conquerorPoints, new CopyOnWriteArrayList<>());

        if (waveTime != null) {
            WaveRunnable.WAVE_PLAYERS.put(name, waveTime);
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

                MailboxType mailboxType = MailboxType.valueOf(type);
                String location = (String) databaseQuery.get("mailboxLocation");
                long time = (long) databaseQuery.get("currentTime");

                if (!globalUser.containsMailbox(mailboxType, location, time)) {
                    globalUser.getMailboxes().add(new Mailbox(mailboxType, location, time));
                }

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

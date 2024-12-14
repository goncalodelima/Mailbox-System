package com.twins.core.announcement;

import com.twins.core.CorePlugin;
import com.twins.core.announcement.runnable.AnnouncementRunnable;
import com.twins.core.announcement.runnable.BuffRunnable;
import com.twins.core.announcement.runnable.NotificationRunnable;
import com.twins.core.utils.Configuration;

public class AnnouncementPlugin {

    private final Configuration lang;

    public AnnouncementPlugin() {

        lang = new Configuration(CorePlugin.INSTANCE, "announcement", "lang.yml");
        lang.saveDefaultConfig();

        new AnnouncementRunnable(lang).runTaskTimer(CorePlugin.INSTANCE, 20 * 60 * 15, 20 * 60 * 15);
        new NotificationRunnable(lang).runTaskTimer(CorePlugin.INSTANCE, 20 * 60 * 5, 20 * 60 * 5);
        new BuffRunnable().runTaskTimer(CorePlugin.INSTANCE, 20 * 60 * 2, 20 * 60 * 2);

    }

    public Configuration getLang() {
        return lang;
    }

}

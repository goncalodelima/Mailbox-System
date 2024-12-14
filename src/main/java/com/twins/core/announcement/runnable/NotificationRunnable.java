package com.twins.core.announcement.runnable;

import com.twins.core.CorePlugin;
import com.twins.core.global.GlobalPlugin;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.research.ResearchPlugin;
import com.twins.core.research.model.user.User;
import com.twins.core.utils.BukkitUtils;
import com.twins.core.utils.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class NotificationRunnable extends BukkitRunnable {

    private final Configuration lang;

    public NotificationRunnable(Configuration lang) {
        this.lang = lang;
    }

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            GlobalUser globalUser = GlobalPlugin.INSTANCE.getUserService().get(player.getName());
            User researchUser = ResearchPlugin.INSTANCE.getUserService().get(player.getName());

            List<Mailbox> mailboxes = new ArrayList<>();

            for (Mailbox mailbox : globalUser.getMailboxes()) {

                if (!mailbox.isToDelete()) {
                    mailboxes.add(mailbox);
                }

            }

            boolean emptyMailboxes = mailboxes.isEmpty();
            int points = researchUser.getPoints();

            if (!emptyMailboxes) {
                lang.getStringList(globalUser.getLanguageType(), "mailbox").forEach(line -> player.sendMessage(BukkitUtils.centerText(line)));
            }

            if (points > 0) {
                lang.getStringList(globalUser.getLanguageType(), "level").forEach(line -> player.sendMessage(BukkitUtils.centerText(line.replace("%points%", CorePlugin.INSTANCE.getNumberFormat().format(points)))));
            }

            if (!emptyMailboxes || points > 0) {
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
            }

        }
    }

}

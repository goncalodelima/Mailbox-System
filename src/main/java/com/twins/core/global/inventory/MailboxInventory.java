package com.twins.core.global.inventory;

import com.minecraftsolutions.utils.ItemBuilder;
import com.twins.core.global.GlobalPlugin;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.mail.type.MailboxType;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.global.model.user.language.LanguageType;
import com.twins.core.utils.Configuration;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.Context;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.state.State;
import net.dmulloy2.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MailboxInventory extends View {

    private final Configuration inventory;

    private final State<Pagination> paginationState;

    private final ItemStack none = new ItemBuilder(Material.BARRIER).setDisplayName("§cEmpty").build();

    private final ItemStack back = new ItemBuilder(Material.ARROW).setDisplayName("§cBack").build();

    private final ItemStack next = new ItemBuilder(Material.ARROW).setDisplayName("§aNext").build();

    public MailboxInventory(GlobalPlugin plugin, Configuration inventory) {
        this.inventory = inventory;
        this.paginationState = computedPaginationState(context -> {

                    Player player = context.getPlayer();
                    GlobalUser globalUser = plugin.getUserService().get(player.getName());

                    if (globalUser.getMailboxes().isEmpty()) {

                        List<Mailbox> list = new ArrayList<>();
                        list.add(new Mailbox(null, null, 0));

                        return list;
                    } else {

                        List<Mailbox> mailboxes = new ArrayList<>();

                        for (Mailbox mailbox : globalUser.getMailboxes()) {

                            if (!mailbox.isToDelete()) {
                                mailboxes.add(mailbox);
                            }

                        }

                        if (mailboxes.isEmpty()) {
                            mailboxes.add(new Mailbox(null, null, 0));
                        }

                        return mailboxes;
                    }

                },
                (context, itemComponentBuilder, i, mailbox) -> {

                    Player player = context.getPlayer();
                    GlobalUser globalUser = plugin.getUserService().get(player.getName());

                    if (mailbox.getType() == null) {
                        itemComponentBuilder.withSlot(13).withItem(none);
                    } else {

                        itemComponentBuilder.withItem(
                                new ItemBuilder(Material.PAPER)
                                        .setDisplayName("§eNotification #" + i + " §b| §f" + TimeUtil.formatTimeDifference(mailbox.getCurrentTime(), System.currentTimeMillis()) + " ago")
                                        .setLore(get(mailbox, globalUser.getLanguageType()))
                                        .build()
                        ).onClick(click -> {
                            mailbox.setToDelete(true);
                            plugin.getUserService().update(globalUser);
                            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                            click.update();
                        });

                    }

                });
    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {

        Pagination pagination = paginationState.get(render);

        render.layoutSlot('<', back)
                .updateOnStateChange(paginationState)
                .displayIf(() -> pagination.currentPageIndex() != 0)
                .onClick(_ -> pagination.back());

        render.layoutSlot('>', next)
                .updateOnStateChange(paginationState)
                .displayIf(() -> pagination.currentPageIndex() < pagination.lastPageIndex())
                .onClick(_ -> pagination.advance());

    }

    @Override
    public void onUpdate(@NotNull Context update) {
        paginationState.get(update).forceUpdate();
    }

    @Override
    public void onInit(ViewConfigBuilder config) {
        config
                .title("Mailbox")
                .size(3)
                .layout(
                        "         ",
                        " OOOOOOO ",
                        "<       >"
                )
                .cancelOnClick()
                .cancelOnDrag()
                .cancelOnDrop()
                .cancelOnPickup()
                .build();
    }

    private List<String> get(Mailbox mailbox, LanguageType type) {

        if (mailbox.getType() == MailboxType.QUIT_CRAFT) {
            return inventory.getStringList(type, "mailbox.quit_craft");
        }

        List<String> mailboxLore;

        if (mailbox.getType() == MailboxType.C4) {
            mailboxLore = inventory.getStringList(type, "mailbox.c4");
        } else if (mailbox.getType() == MailboxType.HOMEMADE) {
            mailboxLore = inventory.getStringList(type, "mailbox.homemade");
        } else if (mailbox.getType() == MailboxType.RAIDED) {
            mailboxLore = inventory.getStringList(type, "mailbox.raided");
        } else {
            return List.of("");
        }

        mailboxLore.replaceAll(string -> string.replace("%location%", mailbox.getLocation()));
        return mailboxLore;
    }

}

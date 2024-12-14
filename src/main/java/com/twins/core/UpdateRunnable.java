package com.twins.core;

import com.twins.core.cupboard.CupboardPlugin;
import com.twins.core.cupboard.model.cupboard.Cupboard;
import com.twins.core.cupboard.model.door.Door;
import com.twins.core.cupboard.model.furnace.Furnace;
import com.twins.core.cupboard.model.miningquarry.MiningQuarry;
import com.twins.core.cupboard.model.raid.Raid;
import com.twins.core.cupboard.model.turret.Turret;
import com.twins.core.global.GlobalPlugin;
import com.twins.core.global.model.backpack.Backpack;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.research.ResearchPlugin;
import com.twins.core.research.model.user.User;
import com.twins.core.vehicle.VehiclePlugin;
import com.twins.core.vehicle.model.Vehicle;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class UpdateRunnable extends BukkitRunnable {

    @Override
    public void run() {

        Collection<Raid> raids = CupboardPlugin.INSTANCE.getRaidService().getAll();
        Collection<Door> doors = CupboardPlugin.INSTANCE.getDoorService().getAll();
        Collection<Furnace> furnaces = CupboardPlugin.INSTANCE.getFurnaceService().getAll();
        Collection<MiningQuarry> miningQuarries = CupboardPlugin.INSTANCE.getMiningQuarryService().getAll();
        Collection<Turret> turrets = CupboardPlugin.INSTANCE.getTurretService().getAll();

        Map<String, Backpack> backpacks = GlobalPlugin.INSTANCE.getBackpackService().getPendingUpdates();
        Map<String, User> researchUsers = ResearchPlugin.INSTANCE.getUserService().getPendingUpdates();

        Set<GlobalUser> globalUsers = GlobalPlugin.INSTANCE.getUserService().getPendingUpdates();
        Set<Cupboard> cupboards = CupboardPlugin.INSTANCE.getCupboardService().getPendingUpdates();
        Set<Vehicle> durabilityPendingUpdates = VehiclePlugin.INSTANCE.getVehicleService().getDurabilityPendingUpdates();
        Set<Vehicle> locationPendingUpdates = VehiclePlugin.INSTANCE.getVehicleService().getLocationPendingUpdates();
        Set<Vehicle> itemsPendingUpdates = VehiclePlugin.INSTANCE.getVehicleService().getItemsPendingUpdates();

        if (!raids.isEmpty()) {
            CupboardPlugin.INSTANCE.getRaidService().update(new HashSet<>(raids)).thenAcceptAsync(list -> {

                if (list == null) {
                    return;
                }

                for (Raid raid : list) {
                    raids.remove(raid);
                }

            }, CorePlugin.INSTANCE.getMainExecutor());
        }

        if (!doors.isEmpty()) {
            CupboardPlugin.INSTANCE.getDoorService().update(new HashSet<>(doors));
        }

        if (!furnaces.isEmpty()) {
            CupboardPlugin.INSTANCE.getFurnaceService().update(new HashSet<>(furnaces));
        }

        if (!miningQuarries.isEmpty()) {
            CupboardPlugin.INSTANCE.getMiningQuarryService().update(new HashSet<>(miningQuarries));
        }

        if (!turrets.isEmpty()) {
            CupboardPlugin.INSTANCE.getTurretService().update(new HashSet<>(turrets));
        }

        if (!backpacks.isEmpty()) {
            GlobalPlugin.INSTANCE.getBackpackService().update(new HashMap<>(backpacks));
            backpacks.clear();
        }

        if (!globalUsers.isEmpty()) {
            Set<GlobalUser> globalUsersCopy = new HashSet<>(globalUsers);
            GlobalPlugin.INSTANCE.getUserService().update(globalUsersCopy).thenAcceptAsync(mailboxesToDelete -> {

                if (mailboxesToDelete == null) {
                    return;
                }

                for (Object[] params : mailboxesToDelete) {

                    String nickname = (String) params[0];
                    Mailbox mailboxToRemove = (Mailbox) params[1];

                    GlobalUser globalUser = GlobalPlugin.INSTANCE.getUserService().get(nickname);

                    if (globalUser != null) {
                        globalUser.getMailboxes().remove(mailboxToRemove);
                    }

                }

            }, CorePlugin.INSTANCE.getMainExecutor()).thenRunAsync(() -> {

                for (GlobalUser globalUser : globalUsersCopy) {

                    if (globalUser.getCrafts().isEmpty() || Bukkit.getPlayer(globalUser.getName()) == null) {
                        GlobalPlugin.INSTANCE.getUserService().removeInserting(globalUser.getName());
                        globalUsers.remove(globalUser);
                    }

                }

            }, CorePlugin.INSTANCE.getMainExecutor());

        }

        if (!researchUsers.isEmpty()) {
            ResearchPlugin.INSTANCE.getUserService().update(new HashMap<>(researchUsers));
            researchUsers.clear();
        }

        if (!cupboards.isEmpty()) {
            CupboardPlugin.INSTANCE.getCupboardService().update(new HashSet<>(cupboards));
            cupboards.clear();
        }

        if (!durabilityPendingUpdates.isEmpty()) {
            VehiclePlugin.INSTANCE.getVehicleService().updateByDurability(new HashSet<>(durabilityPendingUpdates));
            durabilityPendingUpdates.clear();
        }

        if (!locationPendingUpdates.isEmpty() || !itemsPendingUpdates.isEmpty()) {
            VehiclePlugin.INSTANCE.getVehicleService().updateByLocationAndDurabilityAndItems(new HashSet<>(locationPendingUpdates), new HashSet<>(itemsPendingUpdates));
            locationPendingUpdates.clear();
            itemsPendingUpdates.clear();
        }

    }

}

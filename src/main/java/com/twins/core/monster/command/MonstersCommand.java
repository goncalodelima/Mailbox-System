package com.twins.core.monster.command;

import com.twins.core.monster.MonsterRunnable;
import com.twins.core.utils.BukkitUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class MonstersCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player player)) {
            return false;
        }

        if (!player.hasPermission("monsters.admin")) {
            return false;
        }

        if (args.length == 0) {
            player.sendMessage("§c/mobs list");
            player.sendMessage("§c/mobs removeall");
            player.sendMessage("§c/mobs wave");
            return false;
        }

        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage("§cMobs List:");
            player.sendMessage("§bCount: §f" + (MonsterRunnable.ANIMALS.size() + MonsterRunnable.WAVE_ANIMALS.size()));
            return true;
        }

        if (args[0].equalsIgnoreCase("wave")) {
            player.sendMessage("§bX: " + MonsterRunnable.START + " - " + MonsterRunnable.END);
            player.sendMessage("§bZ: " + MonsterRunnable.START + " - " + MonsterRunnable.END);
            return true;
        }

        if (args[0].equalsIgnoreCase("removeall") || args[0].equalsIgnoreCase("remove")) {

            player.sendMessage("§cAll mobs have been removed. Mobs spawned in an uncontrolled way (for example by eggs) will be despawned when a chunk is unloaded.");

            for (Map.Entry<UUID, Location> entry : MonsterRunnable.ANIMALS.entrySet()) {

                UUID uuid = entry.getKey();
                Location location = entry.getValue();
                Chunk chunk = location.getChunk();

                LivingEntity entity = BukkitUtils.getLivingEntityByUUIDLoadedChunk(chunk, uuid);

                if (entity != null) {
                    entity.remove();
                }

            }

            for (Map.Entry<UUID, Location> entry : MonsterRunnable.WAVE_ANIMALS.entrySet()) {

                UUID uuid = entry.getKey();
                Location location = entry.getValue();
                Chunk chunk = location.getChunk();

                LivingEntity entity = BukkitUtils.getLivingEntityByUUIDLoadedChunk(chunk, uuid);

                if (entity != null) {
                    entity.remove();
                }

            }

            MonsterRunnable.ANIMALS.clear();
            MonsterRunnable.WAVE_ANIMALS.clear();
            return true;
        }

        player.sendMessage("§c/mobs list");
        player.sendMessage("§c/mobs removeall");
        player.sendMessage("§c/mobs wave");
        return false;

    }


}

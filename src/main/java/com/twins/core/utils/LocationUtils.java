package com.twins.core.utils;

import com.twins.core.CorePlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class LocationUtils {

    public static Location getRandomLocationInArea(int start, int end) {

        int difference = end - start;
        int random = start + CorePlugin.INSTANCE.getRandom().nextInt(difference) + 1;

        World world = CorePlugin.INSTANCE.getRustWorld();

        if (world.isChunkLoaded(random >> 4, random >> 4)) {

            int y = world.getHighestBlockYAt(random, random);

            Location location = new Location(world, random, y, random);
            Block block = location.getBlock();

            Block verifyBlock = block.getRelative(0, -1, 0);
            Material verifyType = verifyBlock.getType();
            byte verifyData = verifyBlock.getData();

            if (isSpawnBlock(verifyType, verifyData)) {
                return location;
            }

        }

        return null;
    }

    public static Location getRandomLocationInChunk(Chunk chunk) {

        World world = chunk.getWorld();

        System.out.println("chunk: " + chunk.getX());
        System.out.println("chunk: " + chunk.getZ());

        int x = (chunk.getX() << 4) + CorePlugin.INSTANCE.getRandom().nextInt(16);
        int z = (chunk.getZ() << 4) + CorePlugin.INSTANCE.getRandom().nextInt(16);

        System.out.println("coord: " + x);
        System.out.println("coord: " + z);

        int y = world.getHighestBlockYAt(x, z);

        Location location = new Location(world, x, y, z);
        Block block = location.getBlock();

        Block verifyBlock = block.getRelative(0, -1, 0);
        Material verifyType = verifyBlock.getType();
        byte verifyData = verifyBlock.getData();

        if (isSpawnBlock(verifyType, verifyData)) {
            return location;
        }

        return null;
    }

    public static boolean isLocationInsideArea(Location location, int corner1, int corner2) {
        double min = Math.min(corner1, corner2);
        double max = Math.max(corner1, corner2);

        return (location.getX() >= min && location.getX() <= max &&
                location.getZ() >= min && location.getZ() <= max);
    }


    public static boolean isLocationInsideArea(Location location, int corner1X, int corner2X, int corner1Z, int corner2Z) {
        double xMin = Math.min(corner1X, corner2X);
        double zMin = Math.min(corner1Z, corner2Z);
        double xMax = Math.max(corner1X, corner2X);
        double zMax = Math.max(corner1Z, corner2Z);

        return (location.getX() >= xMin && location.getX() <= xMax &&
                location.getZ() >= zMin && location.getZ() <= zMax);
    }

    private static boolean isSpawnBlock(Material type, byte data) {

        if (type == Material.GRASS || type == Material.WOOL && (data == 10 || data == 3)
                || type == Material.STAINED_CLAY && (data == 5 || data == 13)
                || type == Material.SNOW_BLOCK || type == Material.SAND
                || (type == Material.QUARTZ_BLOCK && data == 1)
        || (type == Material.PRISMARINE && data == 2)
        || (type == Material.WOOD && data == 3)
        || type == Material.DIRT) {
            return true;
        }

        return switch (type) {
            case SAPLING, WATER, STATIONARY_WATER, LAVA, STATIONARY_LAVA, POWERED_RAIL, DETECTOR_RAIL, WEB, LONG_GRASS,
                 DEAD_BUSH, YELLOW_FLOWER, RED_ROSE, BROWN_MUSHROOM, RED_MUSHROOM, TORCH, FIRE, REDSTONE_WIRE, CROPS,
                 LADDER, RAILS, LEVER, REDSTONE_TORCH_OFF, REDSTONE_TORCH_ON, STONE_BUTTON, SNOW, SUGAR_CANE_BLOCK,
                 PORTAL, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, PUMPKIN_STEM, MELON_STEM, VINE, WATER_LILY, NETHER_WARTS,
                 ENDER_PORTAL, COCOA, TRIPWIRE_HOOK, TRIPWIRE, FLOWER_POT, CARROT, POTATO, WOOD_BUTTON, SKULL,
                 REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON, ACTIVATOR_RAIL, CARPET, DOUBLE_PLANT -> true;
            default -> false;
        };

    }

}

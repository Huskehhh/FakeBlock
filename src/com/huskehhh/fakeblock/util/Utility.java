package com.huskehhh.fakeblock.util;

import com.huskehhh.fakeblock.FakeBlock;
import com.huskehhh.fakeblock.objects.Wall;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Utility {

    /**
     * Data will be stored like
     * walls.data, 'x,y,z,world,x1,y1,z1,blockname'
     */

    public static YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));


    /**
     * Forces refresh of the config file
     * Used to refresh to return manually added Walls
     */

    public static void forceConfigRefresh() {
        config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));
    }

    /**
     * Get StringList of Walls in config
     *
     * @return StringList of Walls
     */

    public static List<String> getAllWalls() {
        return config.getStringList("walls.list");
    }

    /**
     * Get all Walls from config
     *
     * @return Walls from config
     */

    public static List<Wall> getWalls() {
        List<Wall> allWalls = new ArrayList<Wall>();

        List<String> configWalls = getAllWalls();

        ListIterator<String> li = configWalls.listIterator();

        while (li.hasNext()) {
            String name = li.next();
            Wall wall = Wall.getByName(name);
            allWalls.add(wall);
        }

        return allWalls;
    }

    /**
     * Send Wall to the Player
     */

    public static void sendFakeBlocks() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(FakeBlock.plugin, new Runnable() {
            public void run() {
                processBlockSend();
            }
        }, (2 * 20));
    }

    private static void processBlockSend() {
        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            Wall wall = wallIterator.next();

            if (wall != null) {

                List<String> playerNames = processSendBlocksTo(wall);

                Material material = Material.matchMaterial(wall.getBlockName());

                ArrayList<Location> allBlocks = getBlocks(wall);
                ListIterator<Location> locations = allBlocks.listIterator();

                ListIterator<String> players = playerNames.listIterator();

                while (players.hasNext()) {
                    Player p = Bukkit.getServer().getPlayer(players.next());

                    if (p.hasPermission("fakeblock." + wall.getName()) || p.hasPermission("fakeblock.admin")) break;

                    while (locations.hasNext()) {
                        Location send = locations.next();

                        //TODO: Test on stable version of Spigot, doesn't work so far?
                        p.sendBlockChange(send, material.createBlockData());
                    }
                }
            }
        }
    }

    /**
     * Process singular Player instead of processing all players on server
     *
     * @param p - Player to process
     */

    public static void processIndividual(final Player p) {
        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            final Wall wall = wallIterator.next();

            if (wall != null) {

                ArrayList<Location> allBlocks = getBlocks(wall);
                final ListIterator<Location> locations = allBlocks.listIterator();

                if (p.hasPermission("fakeblock." + wall.getName()) || p.hasPermission("fakeblock.admin")) break;

                if (processSendBlocksTo(wall).contains(p.getName())) {

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(FakeBlock.plugin, new Runnable() {
                        public void run() {
                            Material material = Material.matchMaterial(wall.getBlockName());
                            while (locations.hasNext()) {
                                Location send = locations.next();

                                //TODO: Test on stable version of Spigot, doesn't work so far?
                                p.sendBlockChange(send, material.createBlockData());
                            }
                        }
                    }, (2 * 20));
                }
            }
        }
    }

    /**
     * Method to determine which players are eligible to receive the Wall packets
     *
     * @param wall - Wall to check for eligible players
     * @return List of PlayerNames
     */

    private static List<String> processSendBlocksTo(Wall wall) {

        List<String> process = new ArrayList<String>();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getLocation().getWorld() == Bukkit.getServer().getWorld(wall.getWorldname())) {
                if (isNear(wall.getLoc1(), player.getLocation(), 20) || isNear(wall.getLoc2(), player.getLocation(), 20)) {
                    process.add(player.getName());
                }
            }
        }
        return process;
    }


    private static int getMaxX(int x, int x1) {
        return Math.max(x, x1);
    }

    private static int getMinX(int x, int x1) {
        return Math.min(x, x1);
    }

    private static int getMaxY(int y, int y1) {
        return Math.max(y, y1);
    }

    private static int getMinY(int y, int y1) {
        return Math.min(y, y1);
    }

    private static int getMaxZ(int z, int z1) {
        return Math.max(z, z1);
    }

    private static int getMinZ(int z, int z1) {
        return Math.min(z, z1);
    }

    /**
     * Get all blocks in a Wall
     *
     * @param wall - Wall object to check for blocks
     * @return ArrayList of locations that contains all block locations
     */

    public static ArrayList<Location> getBlocks(Wall wall) {

        World w = Bukkit.getServer().getWorld(wall.getWorldname());

        int bx = (int) wall.getLoc1().getX();
        int bx1 = (int) wall.getLoc2().getX();
        int by = (int) wall.getLoc1().getY();
        int by1 = (int) wall.getLoc2().getY();
        int bz = (int) wall.getLoc1().getY();
        int bz1 = (int) wall.getLoc2().getY();

        ArrayList<Location> blocks = new ArrayList<Location>();

        for (int x = getMinX(bx, bx1); x <= getMaxX(bx, bx1); ++x) {
            for (int y = getMinY(by, by1); y <= getMaxY(by, by1); ++y) {
                for (int z = getMinZ(bz, bz1); z <= getMaxZ(bz, bz1); ++z) {
                    blocks.add(new Location(w, x, y, z));
                }
            }
        }

        return blocks;
    }

    /**
     * Check whether a Player is close to a Wall
     *
     * @param p - Player to check
     * @return whether or not the Player is close to a Wall
     */

    public static boolean isNearWall(Player p, int distance) {
        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            Wall wall = wallIterator.next();

            List<Location> locations = wall.getLocations();
            Iterator<Location> locationIterator = locations.listIterator();

            while (locationIterator.hasNext()) {
                Location locationToCheck = locationIterator.next();
                if (isNear(p.getLocation(), locationToCheck, distance)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Method to check if two locations are close
     *
     * @param first    Location number 1
     * @param second   Location number 2
     * @param distance permitted distance between the two points
     * @return whether distance is acceptable
     */
    public static boolean isNear(Location first, Location second, int distance) {
        return second.distanceSquared(first) < distance;
    }

}
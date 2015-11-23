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

    private static FakeBlock fakeblock;

    public Utility(FakeBlock fakeblock) {
        this.fakeblock = fakeblock;
    }

    /**
     * Data will be stored like
     * walls.data, 'x,y,z,world,x1,y1,z1,block-id,data'
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
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(fakeblock, new Runnable() {
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

                Material m = Material.getMaterial(wall.getId());

                ArrayList<Location> allBlocks = getBlocks(wall);
                ListIterator<Location> locations = allBlocks.listIterator();

                ListIterator<String> players = playerNames.listIterator();

                while (players.hasNext()) {
                    Player p = Bukkit.getServer().getPlayer(players.next());

                    if (!p.hasPermission("fakeblock." + wall.getName()) || !p.hasPermission("fakeblock.admin")) break;

                    while (locations.hasNext()) {
                        Location send = locations.next();
                        p.sendBlockChange(send, m, (byte) wall.getData());
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

                final Material m = Material.getMaterial(wall.getId());

                ArrayList<Location> allBlocks = getBlocks(wall);
                final ListIterator<Location> locations = allBlocks.listIterator();

                if (!p.hasPermission("fakeblock." + wall.getName()) || !p.hasPermission("fakeblock.admin")) break;

                if (processSendBlocksTo(wall).contains(p.getName())) {

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(fakeblock, new Runnable() {
                        public void run() {
                            while (locations.hasNext()) {
                                Location send = locations.next();
                                p.sendBlockChange(send, m, (byte) wall.getData());
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

        int x = wall.getX();
        int y = wall.getY();
        int z = wall.getZ();

        int x1 = wall.getX1();
        int y1 = wall.getY1();
        int z1 = wall.getZ1();

        for (Player server : Bukkit.getServer().getOnlinePlayers()) {

            if (server.getLocation().getWorld() == Bukkit.getServer().getWorld(wall.getWorldname())) {

                //TODO: <test>

                if (Bukkit.getWorld(wall.getWorldname()).getChunkAt(new Location(Bukkit.getServer().getWorld(wall.getWorldname()), x, y, z)) == server.getLocation().getChunk()) {
                    process.add(server.getName());
                }

                if (Bukkit.getWorld(wall.getWorldname()).getChunkAt(new Location(Bukkit.getServer().getWorld(wall.getWorldname()), x1, y1, z1)) == server.getLocation().getChunk()) {
                    process.add(server.getName());
                }

                //TODO: </test>

                /**

                 Process using 'getAllPlayersInChunk'

                 if (getAllPlayersInChunk(Bukkit.getWorld(wall.getWorldname()).getChunkAt(new Location(Bukkit.getServer().getWorld(wall.getWorldname()), x, y, z))).contains(server.getName())) {
                 process.add(server.getName());
                 }

                 if (getAllPlayersInChunk(Bukkit.getWorld(wall.getWorldname()).getChunkAt(new Location(Bukkit.getServer().getWorld(wall.getWorldname()), x1, y1, z1))).contains(server.getName())) {
                 process.add(server.getName());
                 }

                 // Expected to result in less performance

                 **/

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

        int bx = wall.getX();
        int bx1 = wall.getX1();
        int by = wall.getY();
        int by1 = wall.getY1();
        int bz = wall.getZ();
        int bz1 = wall.getZ1();

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

    public static boolean isNearWall(Player p) {
        int px = p.getLocation().getBlockX();
        int py = p.getLocation().getBlockX();
        int pz = p.getLocation().getBlockX();

        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            Wall wall = wallIterator.next();

            int wx = wall.getX();
            int wy = wall.getY();
            int wz = wall.getZ();

            int wx1 = wall.getX1();
            int wy1 = wall.getY1();
            int wz1 = wall.getZ1();


            boolean isNear1 = isNear(px, py, pz, wx, wy, wz, 10);
            boolean isNear2 = isNear(px, py, pz, wx1, wy1, wz1, 10);

            if (isNear1 || isNear2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether a Player is super close to a Wall
     *
     * @param p - Player to check
     * @return whether or not the Player is close to a Wall
     */

    public static boolean isSuperNearWall(Player p) {
        int px = p.getLocation().getBlockX();
        int py = p.getLocation().getBlockX();
        int pz = p.getLocation().getBlockX();

        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            Wall wall = wallIterator.next();

            int wx = wall.getX();
            int wy = wall.getY();
            int wz = wall.getZ();

            int wx1 = wall.getX1();
            int wy1 = wall.getY1();
            int wz1 = wall.getZ1();


            boolean isNear1 = isNear(px, py, pz, wx, wy, wz, 2);
            boolean isNear2 = isNear(px, py, pz, wx1, wy1, wz1, 2);

            if (isNear1 || isNear2) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param x        - First x value
     * @param y        - First y value
     * @param z        - First z value
     * @param x1       - Second x value
     * @param y1       - Second y value
     * @param z1       - Second z value
     * @param distance - How many blocks between to check
     * @return whether or not coordinates are close
     */

    public static boolean isNear(int x, int y, int z, int x1, int y1, int z1, int distance) {
        if ((x - distance) < x1 || (x + distance) < x1 || (x - distance) > x1 || (x + distance) > x1) {
            return true;
        } else if ((y - distance) < y1 || (y + distance) < y1 || (y - distance) > y1 || (y + distance) > y1) {
            return true;
        } else if ((z - distance) < z1 || (z + distance) < z1 || (z - distance) > z1 || (z + distance) > z1) {
            return true;
        }
        return false;
    }

    /**
     * Method to return all players in chunk
     *
     * @param chunk - Chunk to check
     * @return list of players in chunk
     */

    public static List<String> getAllPlayersInChunk(Chunk chunk) {
        List<String> ret = new ArrayList<String>();

        Entity[] ent = chunk.getEntities();

        for (int i = 0; i < ent.length; i++) {
            Entity entity = ent[i];
            if (entity instanceof Player) {
                Player p = (Player) entity;
                ret.add(p.getName());
            }
        }

        return ret;
    }

}
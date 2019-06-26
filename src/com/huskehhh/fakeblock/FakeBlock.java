package com.huskehhh.fakeblock;

import com.huskehhh.fakeblock.listeners.FakeBlockListener;
import com.huskehhh.fakeblock.objects.Config;
import com.huskehhh.fakeblock.objects.Wall;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FakeBlock extends JavaPlugin implements Listener {

    public static FakeBlock plugin;
    private static FakeBlockListener listener;

    // Config object
    public static YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

    /**
     * Method to handle Plugin startup.
     */

    public void onEnable() {
        //Set up plugin variable
        plugin = this;
        listener = new FakeBlockListener();

        // Register events
        getServer().getPluginManager().registerEvents(listener, plugin);
        getServer().getPluginManager().registerEvents(this, plugin);

        // Create Config if not already created
        createConfig();

        // Load all Walls from Config
        Wall.loadWalls();
    }


    /**
     * Method to handle Plugin shutdown.
     */

    public void onDisable() {
        // Unload all Wall objects
        Wall.unloadWalls();
        // Unload all Config objects
        unloadConfigObjects();
    }

    /**
     * Unloads all Configuration objects containing partial Walls in the making.
     */

    private void unloadConfigObjects() {
        listener.configObj.clear();
    }

    /**
     * Method to handle the creation of configuration file if not currently present.
     */

    private void createConfig() {
        boolean exists = new File("plugins/FakeBlock/config.yml").exists();

        if (!exists) {
            new File("plugins/FakeBlock").mkdir();
            config.options().header("FakeBlock, made by Husky!");
            List<String> walls = new ArrayList<String>();
            walls.add("default");
            config.set("default.data", "1,2,3,world,1,2,3,46,0");
            config.set("walls.list", walls);

            try {
                config.save("plugins/FakeBlock/config.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Command handler
     *
     * @param sender       - Command Sender
     * @param cmd          - Command sent
     * @param commandLabel - Command sent converted to String
     * @param args         - Arguments of the Command
     * @return whether or not the command worked
     */

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (commandLabel.equalsIgnoreCase("fakeblock") || commandLabel.equalsIgnoreCase("fb")) {

            if (args.length > 0) {
                String para = args[0];

                if (sender.hasPermission("fakeblock.admin")) {

                    if (para.equalsIgnoreCase("set")) {
                        if (sender instanceof Player) {

                            Player p = (Player) sender;

                            /*
                             * /fb set <name> <blockname>
                             */

                            if (args.length == 3) {
                                listener.map.put(p.getName(), args[1]);
                                listener.selecting.add(p.getName());

                                Config conf = new Config();

                                conf.setName(args[1]);

                                conf.setBlockname(Material.matchMaterial(args[2]).toString());

                                listener.configObj.put(p.getName(), conf);

                                p.sendMessage(ChatColor.GREEN + "[FakeBlock] You can now select the blocks you want.");
                            } else {
                                p.sendMessage(ChatColor.RED + "[FakeBlock] Wrong amount of arguments.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "[FakeBlock] Only players can use this command!");
                        }
                    } else if (para.equalsIgnoreCase("reload")) {
                        Wall.unloadWalls();
                        forceConfigRefresh();
                        Wall.loadWalls();
                        sendFakeBlocks();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "[FakeBlock] You don't have permission for this command.");
                }
            }
        }
        return true;
    }

    /**
     * Forces refresh of the config file
     * Used to refresh to return manually added Walls
     */

    public void forceConfigRefresh() {
        config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));
    }

    /**
     * Get StringList of Walls in config
     *
     * @return StringList of Walls
     */

    public List<String> getAllWalls() {
        return config.getStringList("walls.list");
    }

    /**
     * Get all Walls from config
     *
     * @return Walls from config
     */

    public List<Wall> getWalls() {
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

    public void sendFakeBlocks() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                processBlockSend();
            }
        }, (2 * 20));
    }

    private void processBlockSend() {
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

    public void processIndividual(final Player p) {
        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            final Wall wall = wallIterator.next();

            if (wall != null) {

                ArrayList<Location> allBlocks = getBlocks(wall);
                final ListIterator<Location> locations = allBlocks.listIterator();

                if (p.hasPermission("fakeblock." + wall.getName()) || p.hasPermission("fakeblock.admin")) break;

                if (processSendBlocksTo(wall).contains(p.getName())) {

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        public void run() {
                            Material material = Material.matchMaterial(wall.getBlockName());
                            while (locations.hasNext()) {
                                Location send = locations.next();
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

    private List<String> processSendBlocksTo(Wall wall) {

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

    public ArrayList<Location> getBlocks(Wall wall) {

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

    public boolean isNearWall(Player p, int distance) {
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

    public boolean isNear(Location first, Location second, int distance) {
        return second.distanceSquared(first) < distance;
    }
}
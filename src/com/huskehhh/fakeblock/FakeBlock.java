package com.huskehhh.fakeblock;

import com.huskehhh.fakeblock.listeners.FakeBlockListener;
import com.huskehhh.fakeblock.objects.Config;
import com.huskehhh.fakeblock.objects.Wall;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FakeBlock extends JavaPlugin implements Listener {

    public static FakeBlock plugin;
    private static FakeBlockListener listener;

    // Config object
    public static YamlConfiguration config;

    /**
     * Method to handle Plugin startup.
     */

    public void onEnable() {
        // Set up local object variables
        plugin = this;
        listener = new FakeBlockListener(plugin);
        config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

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
            config.set("default.data", "1,2,3,world,1,2,3,ACACIA_LOG");
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
     * <p>
     * //TODO: Clean this command process up + make it idiot-proof
     */

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        String title = ChatColor.BLACK + "[" + ChatColor.AQUA + "FakeBlock" + ChatColor.BLACK + "] ";

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

                                p.sendMessage(title + ChatColor.GREEN + "You can now select the blocks you want.");
                            } else {
                                p.sendMessage(title + ChatColor.RED + "Wrong amount of arguments.");
                            }
                        } else {
                            sender.sendMessage(title + ChatColor.RED + "Only players can use this command!");
                        }
                    } else if (para.equalsIgnoreCase("reload")) {
                        Wall.unloadWalls();
                        forceConfigRefresh();
                        Wall.loadWalls();
                        sendFakeBlocks(1);
                        sender.sendMessage(title + ChatColor.GREEN + "Walls reloaded!");
                    } else if (para.equalsIgnoreCase("delete")) {
                        if (Wall.getByName(args[1]) != null) {
                            Wall.removeByName(args[1]);
                            sender.sendMessage(title + ChatColor.RED + "'" + args[1] + "' has been deleted");
                        }
                    }
                } else {
                    sender.sendMessage(title + ChatColor.RED + "You don't have permission for this command.");
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
     * Function to queue sending fake blocks
     * delay of 2s in order to prevent login fake blocks not showing
     */

    public void sendFakeBlocks(int delay) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                List<Wall> walls = getWalls();
                Iterator<Wall> wallIterator = walls.listIterator();

                while (wallIterator.hasNext()) {
                    Wall wall = wallIterator.next();

                    List<String> playerNames = processSendBlocksTo(wall);

                    Material material = Material.matchMaterial(wall.getBlockName());

                    ArrayList<Location> allBlocks = wall.getBlocks();
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
        }, (delay * 20));
    }

    /**
     * Process singular Player instead of processing all players on server
     *
     * @param p - Player to process
     */

    public void processIndividual(final Player p, int delay) {
        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            final Wall wall = wallIterator.next();

            ArrayList<Location> allBlocks = wall.getBlocks();
            final ListIterator<Location> locations = allBlocks.listIterator();

            if (p.hasPermission("fakeblock." + wall.getName()) || p.hasPermission("fakeblock.admin")) break;

            if (isPlayerNearWall(p)) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        Material material = Material.matchMaterial(wall.getBlockName());
                        while (locations.hasNext()) {
                            Location send = locations.next();
                            p.sendBlockChange(send, material.createBlockData());
                        }
                    }
                }, (delay * 20));
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
                if (isPlayerNearWall(player)) {
                    process.add(player.getName());
                }
            }
        }
        return process;
    }

    /**
     * Check whether a Player is close to a Wall
     *
     * @param p - Player to check
     * @return whether or not the Player is close to a Wall
     */

    public boolean isPlayerNearWall(Player p) {
        List<Wall> walls = getWalls();
        Iterator<Wall> wallIterator = walls.listIterator();

        while (wallIterator.hasNext()) {
            Wall wall = wallIterator.next();

            int widthOfWall = (int) wall.getDistanceBetweenPoints();

            List<Location> locations = wall.getLocations();
            Iterator<Location> locationIterator = locations.listIterator();

            while (locationIterator.hasNext()) {
                Location locationToCheck = locationIterator.next();

                int playerDistanceToWall = (int) p.getLocation().distanceSquared(locationToCheck);

                // Note: Adding 50 to extend the radius
                int distanceToCheck = (widthOfWall + 50) - playerDistanceToWall;

                if (playerDistanceToWall <= distanceToCheck) {
                    return true;
                }
            }
        }
        return false;
    }

}
package com.huskehhh.fakeblock;

import com.huskehhh.fakeblock.commands.CommandHandler;
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

    private static FakeBlock plugin;
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

        // Register commands
        getCommand("fakeblock").setExecutor(new CommandHandler(plugin, listener));
        getCommand("fb").setExecutor(new CommandHandler(plugin, listener));

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
     * Forces refresh of the config file
     * Used to refresh to return manually added Walls
     */

    public void forceConfigRefresh() {
        config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));
    }

    /**
     * Function to queue sending fake blocks
     * delay of 2s in order to prevent login fake blocks not showing
     */

    public void sendFakeBlocks(int delay) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                List<Wall> walls = Wall.getWalls();
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
        List<Wall> walls = Wall.getWalls();
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
        List<Wall> walls = Wall.getWalls();
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
package com.huskehhh.fakeblock;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.huskehhh.fakeblock.commands.CommandHandler;
import com.huskehhh.fakeblock.listeners.FakeBlockListener;
import com.huskehhh.fakeblock.objects.Wall;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private static ProtocolManager protocolManager;

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
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Register events
        getServer().getPluginManager().registerEvents(listener, plugin);
        getServer().getPluginManager().registerEvents(this, plugin);

        // Register commands
        getCommand("fakeblock").setExecutor(new CommandHandler(plugin, listener));
        getCommand("fb").setExecutor(new CommandHandler(plugin, listener));

        /**
         * Utilises ProtocolLib to listen for USE_ITEM packet in order to prevent players destroying the fake wall
         */
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL,
                        PacketType.Play.Client.USE_ITEM) {

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        Player p = event.getPlayer();
                        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                            if (isPlayerNearWall(p)) processIndividual(p, 1);
                        }
                    }
                });

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL,
                        PacketType.Play.Client.ARM_ANIMATION) {

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        Player p = event.getPlayer();
                        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                            if (isPlayerNearWall(p)) processIndividual(p, 1);
                        }
                    }
                });

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

                if (p.getLocation().getWorld() != locationToCheck.getWorld()) return false;

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
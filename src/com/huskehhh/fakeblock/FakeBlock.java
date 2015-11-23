package com.huskehhh.fakeblock;

import com.huskehhh.fakeblock.listeners.FakeBlockListener;
import com.huskehhh.fakeblock.objects.Config;
import com.huskehhh.fakeblock.objects.Wall;
import com.huskehhh.fakeblock.util.Utility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FakeBlock extends JavaPlugin implements Listener {

    // Config object
    YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

    // Selection List / Mapping
    List<String> right = new ArrayList<String>();
    List<String> selecting = new ArrayList<String>();
    HashMap<String, String> map = new HashMap<String, String>();

    // HashMap used to contain the Configuration of a Wall mid creation
    HashMap<String, Config> configObj = new HashMap<String, Config>();

    // Hook for Utility class - UNUSED at the moment
    Utility api = new Utility(this);


    /**
     * Method to handle Plugin startup.
     */

    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(new FakeBlockListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

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
        configObj.clear();
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

            if (sender == getServer().getConsoleSender()) {

                sender.sendMessage(ChatColor.RED + "Only players can use these commands.");

            } else {

                if (args.length > 0) {
                    Player p = (Player) sender;
                    String para = args[0];

                    if (para.equalsIgnoreCase("set")) {

                        /*
                         * /fb set <name> <id>:<materialdata>
                         */

                        if (p.hasPermission("fakeblock.admin")) {

                            if (args.length == 3) {
                                map.put(p.getName(), args[1]);
                                selecting.add(p.getName());

                                Config conf = new Config();

                                conf.setName(args[1]);

                                String getData = args[2];
                                if (getData.contains(":")) {
                                    String[] splitForData = getData.split(":");
                                    if (splitForData.length == 2) {
                                        conf.setData(Integer.parseInt(splitForData[1]));
                                        conf.setId(Integer.parseInt(splitForData[0]));
                                    }
                                } else {
                                    conf.setId(Integer.parseInt(args[2]));
                                }

                                configObj.put(p.getName(), conf);

                                p.sendMessage(ChatColor.GREEN + "[FakeBlock] You can now select the blocks you want.");
                            } else {
                                p.sendMessage(ChatColor.RED + "[FakeBlock] Need more arguments!");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "[FakeBlock] You don't have permission for this command.");
                        }
                    } else if (para.equalsIgnoreCase("reload")) {

                        if (p.hasPermission("fakeblock.admin")) {
                            Wall.unloadWalls();
                            Wall.loadWalls();

                            Utility.sendFakeBlocks();
                        } else {
                            p.sendMessage(ChatColor.RED + "[FakeBlock] You don't have permission for this command.");
                        }
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Listening event to handle the selection paramaters of Walls
     *
     * @param e - PlayerInteractEvent
     */

    @EventHandler
    public void wallSelection(PlayerInteractEvent e) {

        Player p = e.getPlayer();
        if (e.getClickedBlock() != null) {
            Block b = e.getClickedBlock();
            if (p.hasPermission("fakeblock.admin")) {

                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                    if (selecting.contains(p.getName()) && !right.contains(p.getName())) {
                        Location l = b.getLocation();

                        int lx = l.getBlockX();
                        int ly = l.getBlockY();
                        int lz = l.getBlockZ();

                        Config conf = configObj.get(p.getName());
                        conf.setWorldname(l.getWorld().getName());
                        conf.setX(lx);
                        conf.setY(ly);
                        conf.setZ(lz);

                        right.add(p.getName());
                        selecting.remove(p.getName());
                        p.sendMessage(ChatColor.GREEN + "[FakeBlock] Great! Now Please Right-Click and select the second point!");
                        e.setCancelled(true);

                    }
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    if (!selecting.contains(p.getName()) && right.contains(p.getName())) {

                        Location rl = b.getLocation();
                        int rx = rl.getBlockX();
                        int ry = rl.getBlockY();
                        int rz = rl.getBlockZ();

                        Config conf = configObj.get(p.getName());
                        conf.setX1(rx);
                        conf.setY1(ry);
                        conf.setZ1(rz);

                        conf.createObject();

                        configObj.remove(p.getName());

                        p.sendMessage(ChatColor.GREEN + "[FakeBlock] Great! Creating the fake wall now!");

                        right.remove(p.getName());

                        e.setCancelled(true);

                        Utility.sendFakeBlocks();
                    }
                }
            }
        }
    }

}

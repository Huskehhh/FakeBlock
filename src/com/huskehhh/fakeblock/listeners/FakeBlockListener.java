package com.huskehhh.fakeblock.listeners;

import com.huskehhh.fakeblock.FakeBlock;
import com.huskehhh.fakeblock.objects.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FakeBlockListener implements Listener {

    // Selection List / Mapping
    public static List<String> right = new ArrayList<String>();
    public static List<String> selecting = new ArrayList<String>();
    public static HashMap<String, String> map = new HashMap<String, String>();

    // HashMap used to contain the Configuration of a Wall mid creation
    public static HashMap<String, Config> configObj = new HashMap<String, Config>();

    private FakeBlock plugin;

    public FakeBlockListener(FakeBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Method to listen for PlayerJoin, sending the Fake Packets when they do connect.
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerJoinEvent
     */

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        plugin.processIndividual(e.getPlayer(), 2);
    }

    /**
     * Method to listen for Teleportation by a Player, sending the Fake Packets when they do teleport
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerTeleportEvent
     */

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        plugin.processIndividual(e.getPlayer(), 2);
    }

    /**
     * Method to listen for PlayerRespawn, sending the Fake Packets to them
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerRespawnEvent
     */

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.processIndividual(e.getPlayer(), 2);
    }


    /**
     * Listening event to handle the selection paramaters of Walls
     *
     * @param e - PlayerInteractEvent
     */

    @EventHandler
    public void wallSelection(PlayerInteractEvent e) {

        String fakeBlockTitle = ChatColor.BLACK + "[" + ChatColor.AQUA + "FakeBlock" + ChatColor.BLACK + "] ";

        Player p = e.getPlayer();
        if (e.getClickedBlock() != null) {
            Block b = e.getClickedBlock();
            if (p.hasPermission("fakeblock.admin")) {

                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                    if (selecting.contains(p.getName()) && !right.contains(p.getName())) {
                        Location l = b.getLocation();

                        Config conf = configObj.get(p.getName());

                        conf.setLocation1(l);

                        right.add(p.getName());
                        selecting.remove(p.getName());
                        p.sendMessage(fakeBlockTitle + ChatColor.GREEN + "Great! Now Please Right-Click and select the second point!");
                        e.setCancelled(true);
                    }
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    if (!selecting.contains(p.getName()) && right.contains(p.getName())) {
                        Location rl = b.getLocation();

                        Config conf = configObj.get(p.getName());

                        conf.setLocation2(rl);
                        conf.createObject();

                        configObj.remove(p.getName());
                        right.remove(p.getName());
                        p.sendMessage(fakeBlockTitle + ChatColor.GREEN + "Great! Creating the fake wall now!");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Listening event to handle block break near walls
     *
     * @param e - BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        plugin.processIndividual(e.getPlayer(), 0);
    }

}
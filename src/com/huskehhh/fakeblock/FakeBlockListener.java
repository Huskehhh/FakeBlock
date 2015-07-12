package com.huskehhh.fakeblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;

import java.io.File;

public class FakeBlockListener implements Listener {

    private FakeBlock plugin;

    public FakeBlockListener(FakeBlock plugin) {
        this.plugin = plugin;
    }

    Utility utility = new Utility();

    boolean wallExists = true;

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (wallExists) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    utility.sendFakeBlocks(p);
                }
            }, (2 * 20));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();
        if (wallExists) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                        utility.sendFakeBlocks(server);
                    }
                }
            }, (2 * 20));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        if (wallExists) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                        utility.sendFakeBlocks(server);
                    }
                }
            }, (2 * 20));
        }
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (utility.isSuperNearWall(p)) {
            for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                utility.sendFakeBlocks(server);
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (utility.isNearWall(p)) {
            utility.sendFakeBlocks(p);
        }

        if (e.getClickedBlock() != null) {
            Block b = e.getClickedBlock();

            int x = b.getLocation().getBlockX();
            int y = b.getLocation().getBlockY();
            int z = b.getLocation().getBlockZ();

            int px = p.getLocation().getBlockX();
            int py = p.getLocation().getBlockY();
            int pz = p.getLocation().getBlockZ();

            if (utility.isNear(x, y, z, px, py, pz)) {
                for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                    utility.sendFakeBlocks(server);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        int x = b.getLocation().getBlockX();
        int y = b.getLocation().getBlockY();
        int z = b.getLocation().getBlockZ();

        int px = p.getLocation().getBlockX();
        int py = p.getLocation().getBlockY();
        int pz = p.getLocation().getBlockZ();

        if (utility.isNear(x, y, z, px, py, pz)) {
            for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                utility.sendFakeBlocks(server);
            }
        }
    }


}
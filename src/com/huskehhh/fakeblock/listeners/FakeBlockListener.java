package com.huskehhh.fakeblock.listeners;

import com.huskehhh.fakeblock.FakeBlock;
import com.huskehhh.fakeblock.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class FakeBlockListener implements Listener {

    private FakeBlock plugin;

    public FakeBlockListener(FakeBlock plugin) {
        this.plugin = plugin;
    }

    Utility utility = new Utility();

    HashMap<String, Location> tracking = new HashMap<String, Location>();

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

            if (utility.isNear(x, y, z, px, py, pz, 10)) {
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

        if (utility.isNear(x, y, z, px, py, pz, 10)) {
            for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                utility.sendFakeBlocks(server);
            }
        }

    }

    @EventHandler
    public void onEnderPearl(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (tracking.containsKey(p.getName())) {
                if (utility.isNearWall(p)) {
                    p.teleport(tracking.get(p.getName()));
                    tracking.remove(p.getName());
                    p.sendMessage(ChatColor.RED + "[FakeBlock] Unable to teleport through the wall");
                    p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1)); // Refund them their wasted item.
                }
            }
        }
    }

    @EventHandler
    public void onEnderPearl(ProjectileLaunchEvent e) {
        if (e.getEntity().getType() == EntityType.ENDER_PEARL) {
            EnderPearl ep = (EnderPearl) e.getEntity();
            if (ep.getShooter() instanceof Player) {
                Player p = (Player) ep.getShooter();
                tracking.put(p.getName(), p.getLocation());
            }
        }
    }


}
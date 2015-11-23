package com.huskehhh.fakeblock.listeners;

import com.huskehhh.fakeblock.FakeBlock;
import com.huskehhh.fakeblock.util.Utility;
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

    // FakeBlock hook
    private FakeBlock plugin;

    // Constructor
    public FakeBlockListener(FakeBlock plugin) {
        this.plugin = plugin;
    }

    // Users to track for illicit actions
    HashMap<String, Location> tracking = new HashMap<String, Location>();

    /**
     * Method to listen for PlayerJoin, sending the Fake Packets when they do connect.
     * <p/>
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerJoinEvent
     */

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Utility.sendFakeBlocks();
    }

    /**
     * Method to listen for Teleportation by a Player, sending the Fake Packets when they do teleport
     * <p/>
     * TODO: Implement a check if they are close to a Wall or not, saves processing / sending unneeded Packets
     * <p/>
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerTeleportEvent
     */

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Utility.sendFakeBlocks();
    }

    /**
     * Method to listen for PlayerRespawn, sending the Fake Packets to them
     * <p/>
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerRespawnEvent
     */

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Utility.sendFakeBlocks();
    }


    /**
     * Method to Listen for PlayerMove, checking if the Player is close to a Wall, if so, ensure they receive the Fake Packets
     *
     * @param e - PlayerMoveEvent
     */

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (Utility.isSuperNearWall(p) || Utility.isNearWall(p)) {
            Utility.sendFakeBlocks();
        }
    }

    /**
     * Method to listen for PlayerInteract ensuring that if they click a Block that is apart of the Wall, send them the Wall again
     * to ensure that the client does not overwrite the Fake data
     * <p/>
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - PlayerInteractEvent
     */

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (Utility.isNearWall(p)) {
            Utility.sendFakeBlocks();
        }

        if (e.getClickedBlock() != null) {
            Block b = e.getClickedBlock();

            int x = b.getLocation().getBlockX();
            int y = b.getLocation().getBlockY();
            int z = b.getLocation().getBlockZ();

            int px = p.getLocation().getBlockX();
            int py = p.getLocation().getBlockY();
            int pz = p.getLocation().getBlockZ();

            if (Utility.isNear(x, y, z, px, py, pz, 10)) {
                Utility.sendFakeBlocks();
            }
        }
    }

    /**
     * Method to listen for BlockBreak to send the Player the Wall packets again if they are close to the Wall
     * <p/>
     * Note: The delay is to ensure that when they are receiving the World Packets, they do not conflict or overwrite
     * the Fake packets for the Wall
     *
     * @param e - BlockBreakEvent
     */

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

        if (Utility.isNear(x, y, z, px, py, pz, 10)) {
            Utility.sendFakeBlocks();
        }

    }

    /**
     * Method to listen for EnderPearl land Event in order to cancel it if the player is near a wall and currently being tracked
     * for throwing an EnderPearl
     * <p/>
     * TODO: Fix, is currently broken and supposedly not working
     * TODO: Test
     *
     * @param e
     */

    @EventHandler
    public void onEnderPearl(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (tracking.containsKey(p.getName())) {
                if (Utility.isNearWall(p)) {
                    p.teleport(tracking.get(p.getName()));
                    tracking.remove(p.getName());
                    p.sendMessage(ChatColor.RED + "[FakeBlock] Unable to teleport through the wall");
                    p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1)); // Refund them their wasted item.
                }
            }
        }
    }

    /**
     * Method to listen for the launching of an EnderPearl in order to initiate tracking of a Player for when the EnderPearl lands
     *
     * @param e - ProjectileLaunchEvent
     */

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
package pro.husk.fakeblock.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import pro.husk.fakeblock.FakeBlock;

/**
 * Primary listener class for Spigot API events
 */
public class FakeBlockListener implements Listener {

    /**
     * Method to listen for when a server resource pack is applied
     *
     * @param event PlayerResourcePackStatusEvent
     */
    @EventHandler
    public void resourcePackApply(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            process(event.getPlayer());
        }
    }

    /**
     * Method to listen for when a player respawns
     *
     * @param event PlayerRespawnEvent
     */
    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        process(event.getPlayer());
    }

    /**
     * Method to listen for when a player breaks a block
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (FakeBlock.getWallUtility().locationIsInsideWall(event.getBlock().getLocation())
                && !player.hasPermission("fakeblock.admin")) {
            process(player);
            event.setCancelled(true);
        }
    }

    /**
     * Method to listen for when a player places a block
     *
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (FakeBlock.getWallUtility().locationIsInsideWall(event.getBlockPlaced().getLocation())
                && !player.hasPermission("fakeblock.admin")) {
            process(player);
            event.setCancelled(true);
        }
    }

    /**
     * Broad method to process a player, checking if they have permission for any nearby walls on a delay of 3 seconds to avoid latency issues
     *
     * @param player to check
     */
    private void process(Player player) {
        FakeBlock.getWallUtility().processWall(player, 3, false);
    }
}
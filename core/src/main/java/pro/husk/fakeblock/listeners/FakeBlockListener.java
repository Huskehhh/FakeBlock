package pro.husk.fakeblock.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import pro.husk.fakeblock.FakeBlock;

public class FakeBlockListener implements Listener {

    @EventHandler
    public void resourcePackApply(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            process(event.getPlayer());
        }
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        process(event.getPlayer());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (FakeBlock.getWallUtility().locationIsInsideWall(event.getBlock().getLocation())
                && !player.hasPermission("fakeblock.admin")) {
            process(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (FakeBlock.getWallUtility().locationIsInsideWall(event.getBlockPlaced().getLocation())
                && !player.hasPermission("fakeblock.admin")) {
            process(player);
            event.setCancelled(true);
        }
    }

    private void process(Player player) {
        FakeBlock.getWallUtility().processWall(player, 3, false);
    }
}
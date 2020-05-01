package pro.husk.fakeblock.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import pro.husk.fakeblock.FakeBlock;

public class FakeBlockListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        caterForAllConnections(event.getPlayer());
    }

    @EventHandler
    public void playerTeleport(PlayerTeleportEvent event) {
        caterForAllConnections(event.getPlayer());
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        caterForAllConnections(event.getPlayer());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        caterForAllConnections(event.getPlayer());
    }

    @EventHandler
    public void worldChange(PlayerChangedWorldEvent event) {
        caterForAllConnections(event.getPlayer());
    }

    @EventHandler
    public void resourcePackApply(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            caterForAllConnections(event.getPlayer());
        }
    }

    /**
     * Send numerous packets to cater for edge cases (IE: slow chunk load, slow client connection, slow client machine)
     *
     * @param player to send packets to
     */
    private void caterForAllConnections(Player player) {
        FakeBlock.getPlugin().processWall(player, 3, false);
        FakeBlock.getPlugin().processWall(player, 5, false);
        FakeBlock.getPlugin().processWall(player, 7, false);
    }
}
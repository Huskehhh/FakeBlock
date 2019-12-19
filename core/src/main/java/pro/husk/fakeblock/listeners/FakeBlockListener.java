package pro.husk.fakeblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pro.husk.fakeblock.FakeBlock;

public class FakeBlockListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        FakeBlock.getPlugin().processWall(event.getPlayer(), 3, false);
    }

    @EventHandler
    public void playerTeleport(PlayerTeleportEvent event) {
        FakeBlock.getPlugin().processWall(event.getPlayer(), 3, false);
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        FakeBlock.getPlugin().processWall(event.getPlayer(), 3, false);
    }

    // idk if needed now the packet listener is there? idk, who knows, test i guess @test
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        FakeBlock.getPlugin().processWall(event.getPlayer(), 1, false);
    }
}
package pro.husk.fakeblock.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    private void process(Player player) {
        FakeBlock.getWallUtility().processWall(player, 3, false);
    }
}
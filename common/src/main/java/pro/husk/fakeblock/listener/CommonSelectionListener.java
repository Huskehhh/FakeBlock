package pro.husk.fakeblock.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;

import java.util.concurrent.CompletableFuture;

public class CommonSelectionListener implements Listener {

    /**
     * Listener for FakeBlock bound selection
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void playerSelect(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (Config.isSelecting(player.getUniqueId())) {
            if (event.getHand() != EquipmentSlot.HAND) return;
            Block block = event.getClickedBlock();

            if (block == null) return;

            Config config = Config.getCurrentConfigurations().get(player.getUniqueId());

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                player.sendMessage(Language.getPrefix() + " " + Language.getLocationSaved());
                config.setLocation2(block.getLocation());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                player.sendMessage(Language.getPrefix() + " " + Language.getLocationSaved());
                config.setLocation1(block.getLocation());
            }

            if (config.getLocation1() != null && config.getLocation2() != null) {
                WallObject wallObject = config.createWallObject();

                CompletableFuture.supplyAsync(wallObject::loadBlocksInBetween)
                        .thenAccept(loadedWallList -> {
                            wallObject.setBlocksInBetween(loadedWallList);
                            config.remove();
                            player.sendMessage(Language.getPrefix() + " " + Language.getWallsSelectionComplete());
                        });
            }

            event.setCancelled(true);
        }
    }
}

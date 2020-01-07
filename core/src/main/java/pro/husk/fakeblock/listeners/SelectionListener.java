package pro.husk.fakeblock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SelectionListener implements Listener {

    @EventHandler
    public void playerSelect(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (Config.isSelecting(player.getName()) && event.getHand() == EquipmentSlot.HAND) {
            Block block = event.getClickedBlock();

            Config config = Config.getCurrentConfigurations().get(player.getName());

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                player.sendMessage(Language.getPrefix() + " " + Language.getLocationSaved());
                config.setLocation2(block.getLocation());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                player.sendMessage(Language.getPrefix() + " " + Language.getLocationSaved());
                config.setLocation1(block.getLocation());
            }

            if (config.getLocation1() != null && config.getLocation2() != null) {

                // Create WallObject
                WallObject wallObject = config.createWallObject();

                CompletableFuture<List<Location>> loadWallFuture = wallObject.loadBlocksInBetween();

                loadWallFuture.thenAccept(loadedWallList -> {
                    wallObject.setBlocksInBetween(loadedWallList);

                    // Remove config object
                    config.remove();

                    player.sendMessage(Language.getPrefix() + " " + Language.getDisplayingVisualisation());

                    // Render visualisation of the fake wall
                    wallObject.renderWall(player);

                    // TODO: pause creation of wall until user confirms, maybe implement state to config

                    // Reverse the visualisation after 5 seconds
                    Bukkit.getScheduler().scheduleSyncDelayedTask(FakeBlock.getPlugin(), () -> {
                        wallObject.sendRealBlocks(player);

                        // Save Wall to config
                        wallObject.saveWall();

                        player.sendMessage(Language.getPrefix() + " " + Language.getWallsSelectionComplete());
                    }, 5 * 20); // 5 seconds
                });
            }

            event.setCancelled(true);
        }
    }
}
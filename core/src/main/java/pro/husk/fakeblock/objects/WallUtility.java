package pro.husk.fakeblock.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class WallUtility {

    /**
     * Method for checking if a location is close to wall
     *
     * @param playerLocation to check
     * @return null if not, wallObject if they are
     */
    public CompletableFuture<List<WallObject>> getNearbyFakeBlocks(Location playerLocation) {
        return CompletableFuture.supplyAsync(() -> {
            List<WallObject> nearby = new ArrayList<>();

            for (WallObject wallObject : WallObject.getWallObjectList()) {

                if (playerLocation.getWorld() != wallObject.getLocation1().getWorld()) break;

                for (Location location : wallObject.getBlocksInBetween()) {
                    int playerDistanceToWall = (int) playerLocation.distanceSquared(location);
                    int distanceToCheck = (int) (wallObject.getDistanceBetweenPoints() + 500) - playerDistanceToWall;

                    if (playerDistanceToWall <= distanceToCheck && !nearby.contains(wallObject)) {
                        nearby.add(wallObject);
                    }
                }
            }
            return nearby;
        });
    }

    /**
     * Process check of player location near the wall async, and if they are close, send fake blocks
     *
     * @param player to check
     * @param delay  on sending blocks
     */
    public void processWall(Player player, int delay, boolean ignorePermission) {
        getNearbyFakeBlocks(player.getLocation()).thenAcceptAsync(walls -> walls.forEach(wall -> {
            if (player.hasPermission("fakeblock." + wall.getName()) || ignorePermission) {
                wall.sendFakeBlocks(player, delay);
            }
        }));
    }

    /**
     * Process check of player location near the wall async, and if they are close, send either the real or fake blocks
     * Used for LuckPerms hook
     *
     * @param player to check
     */
    public void processWallConditions(Player player) {
        getNearbyFakeBlocks(player.getLocation()).thenAcceptAsync(walls -> walls.forEach(wall -> {
            if (!player.hasPermission("fakeblock." + wall.getName())) {
                wall.sendRealBlocks(player);
            } else {
                wall.sendFakeBlocks(player, 0);
            }
        }));
    }

    /**
     * Little utility helper method to queue all players to be re-checked for permission
     */
    public void queueProcessAllPlayers() {
        FakeBlock.newChain().sync(() -> Bukkit.getOnlinePlayers().forEach(player ->
                processWall(player, 0, false))).execute();
    }
}

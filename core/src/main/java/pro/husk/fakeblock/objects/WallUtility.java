package pro.husk.fakeblock.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class WallUtility {

    private final boolean inverse;

    public WallUtility(boolean inverse) {
        this.inverse = inverse;
    }

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
                    double playerDistanceToWall = playerLocation.distance(location);
                    double distanceToCheck = (Bukkit.getViewDistance() * 15) + wallObject.getDistanceBetweenPoints();

                    if (playerDistanceToWall <= distanceToCheck
                            && !nearby.contains(wallObject)) {
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
            if (ignorePermission) wall.sendFakeBlocks(player, 0);

            boolean hasWallPerm = player.hasPermission("fakeblock." + wall.getName());
            if (inverse && !hasWallPerm) {
                wall.sendFakeBlocks(player, delay);
            } else if (!inverse && hasWallPerm) {
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
            boolean hasWallPerm = player.hasPermission("fakeblock." + wall.getName());
            if (inverse) {
                if (hasWallPerm) {
                    wall.sendRealBlocks(player);
                } else {
                    wall.sendFakeBlocks(player, 0);
                }
            } else {
                if (hasWallPerm) {
                    wall.sendFakeBlocks(player, 0);
                } else {
                    wall.sendRealBlocks(player);
                }
            }
        }));
    }

    /**
     * Helper method to check if a location is between any wall
     * Used for block place and block break checks
     * Time complexity of O(n) with n being the number of walls
     * This is used instead of just checking if the WallObject blocksInBetween contains(location) because
     * There is a near always guarantee that number of blocksInBetween > amount of wall objects
     * Therefore is faster to call this method
     *
     * @param targetLocation location to check
     * @return true if the location is in a wall, false if not
     */
    public boolean locationIsInsideWall(Location targetLocation) {
        for (WallObject wallObject : WallObject.getWallObjectList()) {
            Location inAreaLocation1 = wallObject.getLocation1();
            Location inAreaLocation2 = wallObject.getLocation2();
            if (targetLocation.getWorld() == inAreaLocation1.getWorld()) {
                if ((targetLocation.getBlockX() >= inAreaLocation1.getBlockX() && targetLocation.getBlockX() <= inAreaLocation2.getBlockX())
                        || (targetLocation.getBlockX() <= inAreaLocation1.getBlockX() && targetLocation.getBlockX() >= inAreaLocation2.getBlockX())) {
                    if ((targetLocation.getBlockZ() >= inAreaLocation1.getBlockZ() && targetLocation.getBlockZ() <= inAreaLocation2.getBlockZ())
                            || (targetLocation.getBlockZ() <= inAreaLocation1.getBlockZ() && targetLocation.getBlockZ() >= inAreaLocation2.getBlockZ())) {
                        if (targetLocation.getBlockY() >= inAreaLocation1.getBlockY() && targetLocation.getBlockY() <= inAreaLocation2.getBlockY()
                                || (targetLocation.getBlockY() <= inAreaLocation1.getBlockY() && targetLocation.getBlockY() >= inAreaLocation2.getBlockY())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}

package pro.husk.fakeblock.objects;

import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for Walls, utilised for on the fly handling of FakeBlock visibility functionality
 */
public final class WallUtility {

    private final Map<UUID, List<WallObject>> nearbyCache = ExpiringMap.builder()
            .expiration(5, TimeUnit.SECONDS)
            .build();

    private final boolean inverse;

    /**
     * Constructor of WallUtility
     *
     * @param inverse whether or not to inverse permission checks
     */
    public WallUtility(boolean inverse) {
        this.inverse = inverse;
    }

    /**
     * Method for checking if a location is close to wall
     * Method time complexity is O(n) with n being number of walls
     * Uses an ExpiringMap to reduce calls of users who are quite some distance away
     *
     * @param player to check
     * @return null if not, wallObject if they are
     */
    public CompletableFuture<List<WallObject>> getNearbyFakeBlocks(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            Location playerLocation = player.getLocation();
            List<WallObject> nearby = nearbyCache.getOrDefault(player.getUniqueId(), new ArrayList<>());

            // Return cached
            if (nearby.size() != 0) return nearby;

            boolean shouldCache = true;

            for (WallObject wallObject : WallObject.getWallObjectList()) {
                if (playerLocation.getWorld() != wallObject.getLocation1().getWorld()) break;

                double playerDistanceToWall1 = playerLocation.distance(wallObject.getLocation1());
                double playerDistanceToWall2 = playerLocation.distance(wallObject.getLocation2());
                double distanceToCheck = (Bukkit.getViewDistance() * 15) + wallObject.getDistanceBetweenPoints();

                // If the player is close, then don't cache the result!
                if (playerDistanceToWall1 < 30 || playerDistanceToWall2 < 30) {
                    shouldCache = false;
                }

                if (playerDistanceToWall1 <= distanceToCheck
                        || playerDistanceToWall2 <= distanceToCheck
                        && !nearby.contains(wallObject)) {
                    nearby.add(wallObject);
                }
            }

            if (shouldCache) {
                nearbyCache.put(player.getUniqueId(), nearby);
            }

            return nearby;
        });
    }

    /**
     * Process check of player location near the wall async, and if they are close, send fake blocks
     *
     * @param player           to check
     * @param delay            on sending blocks
     * @param ignorePermission whether or not to ignore whether or not the player has permission
     */
    public void processWall(Player player, int delay, boolean ignorePermission) {
        getNearbyFakeBlocks(player).thenAcceptAsync(walls -> walls.forEach(wall -> {
            if (ignorePermission) {
                wall.sendFakeBlocks(player, 0);
                return;
            }

            // If the wall is a temporary wall, send them the fake blocks and only the fake blocks.
            // This will mean that this is handled by the external plugin rather than FakeBlock
            if (wall.getUsersToDisplayFor().contains(player.getUniqueId())) {
                wall.sendFakeBlocks(player, delay);
                return;
            }

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
        getNearbyFakeBlocks(player).thenAcceptAsync(walls -> walls.forEach(wall -> {
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
     * There is a near always guarantee that number of blocksInBetween is greater than amount of wall objects
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
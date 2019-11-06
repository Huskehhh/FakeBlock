package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;

import java.util.ArrayList;
import java.util.List;

public abstract class WallObject {

    @Getter
    private static List<WallObject> wallObjectList = new ArrayList<WallObject>();

    @Getter
    private String name;

    @Getter
    @Setter
    private Location location1;

    @Getter
    @Setter
    private Location location2;

    @Getter
    private List<Location> blocksInBetween;

    /**
     * Constructor
     *
     * @param name of wall
     */
    public WallObject(String name) {
        this.name = name;

        wallObjectList.add(this);

        // Load Locations in the wall async
        Bukkit.getScheduler().runTaskAsynchronously(FakeBlock.getPlugin(), () -> {
            blocksInBetween = loadBlocksInBetween();
        });
    }

    /**
     * Static getter for WallObject by name of wall
     *
     * @param name of wall
     * @return WallObject or null if not found
     */
    public static WallObject getByName(String name) {
        for (WallObject wallObject : wallObjectList) {
            if (wallObject.getName().equalsIgnoreCase(name)) return wallObject;
        }
        return null;
    }

    /**
     * Method to load wall from config
     */
    public abstract void loadWall();

    /**
     * Method to save wall to config
     */
    public abstract void saveWall();

    /**
     * Method to render wall for player
     *
     * @param player to render wall for
     */
    public abstract void renderWall(Player player);

    /**
     * Gets distance between the two location points
     *
     * @return distanceBetweenPoints
     */
    public double getDistanceBetweenPoints() {
        return getLocation1().distanceSquared(getLocation2());
    }

    /**
     * Method to load all locations between the two points
     *
     * @return list of Location
     */
    private List<Location> loadBlocksInBetween() {
        List<Location> locations = new ArrayList<>();

        Location loc1 = getLocation1();
        Location loc2 = getLocation2();

        int topBlockX = (Math.max(loc1.getBlockX(), loc2.getBlockX()));
        int bottomBlockX = (Math.min(loc1.getBlockX(), loc2.getBlockX()));

        int topBlockY = (Math.max(loc1.getBlockY(), loc2.getBlockY()));
        int bottomBlockY = (Math.min(loc1.getBlockY(), loc2.getBlockY()));

        int topBlockZ = (Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        int bottomBlockZ = (Math.min(loc1.getBlockZ(), loc2.getBlockZ()));

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    locations.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }

        return locations;
    }

    /**
     * Method to send real blocks to the player (used on destruction of object)
     * @param player to send real blocks to
     */
    public abstract void sendRealBlocks(Player player);

    /**
     * Method to remove wall from config
     */
    public abstract void removeFromConfig();

    /**
     * Method to delete the wall
     */
    public void delete() {
        // Send updates to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendRealBlocks(player);
        }

        removeFromConfig();
        wallObjectList.remove(this);
    }
}
package pro.husk.fakeblock.objects;

import com.comphenix.protocol.events.PacketContainer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.hooks.ProtocolLibHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Core Wall data object, outlines a structure which is then implemented to version-specific objects
 */
public abstract class WallObject {

    @Getter
    private static final List<WallObject> wallObjectList = new ArrayList<>();

    @Getter
    private final String name;

    /**
     * Provides access to the cached List of fake PacketContainers
     */
    @Getter
    protected List<PacketContainer> fakeBlockPacketList;

    /**
     * Provides access to the cached list of real PacketContainers
     */
    @Getter
    protected List<PacketContainer> realBlockPacketList;

    /**
     * Provides access to the List of Location's in-between location1 and location2
     */
    @Getter
    @Setter
    protected List<Location> blocksInBetween;

    /**
     * Provides access to the current state of the WallObject's loading
     */
    @Getter
    protected boolean loadingData;

    @Getter
    @Setter
    private Location location1;

    @Getter
    @Setter
    private Location location2;

    private double distanceBetweenPoints;

    private final HashSet<UUID> usersToDisplayFor = new HashSet<>();

    /**
     * Default constructor
     *
     * @param name of wall
     */
    public WallObject(String name) {
        this.name = name;

        wallObjectList.add(this);
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
     * Simple setter method use for setting the data map directly for non-persistent walls
     *
     * @param fakeBlockDataHashMap data map for non-persistent wall
     */
    public abstract void setFakeBlockDataHashMap(HashMap<Location, FakeBlockData> fakeBlockDataHashMap);

    /**
     * Gets distance between the two location points
     *
     * @return distanceBetweenPoints
     */
    public double getDistanceBetweenPoints() {
        // Lazy load, then cache
        if (distanceBetweenPoints == 0) {
            distanceBetweenPoints = getLocation1().distance(getLocation2());
        }
        return distanceBetweenPoints;
    }

    /**
     * Method to send fake blocks to player with given delay
     *
     * @param player to send fake blocks to
     * @param delay  to send the blocks on (seconds)
     */
    public void sendFakeBlocks(Player player, int delay) {
        if (!loadingData) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(FakeBlock.getPlugin(), () -> fakeBlockPacketList.forEach(packetContainer -> {
                try {
                    ProtocolLibHelper.getProtocolManager().sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }), delay * 20);
        }
    }

    /**
     * Method to send real blocks to the player
     *
     * @param player to send real blocks to
     */
    public void sendRealBlocks(Player player) {
        if (!loadingData) {
            FakeBlock.newChain().async(() -> realBlockPacketList.forEach(packetContainer -> {
                try {
                    ProtocolLibHelper.getProtocolManager().sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            })).execute();
        }
    }

    /**
     * Abstract method used in order to build ProtocolLib's PacketContainers in a list
     *
     * @param fake whether or not to create fake/real packets
     * @return List of PacketContainer
     */
    protected abstract List<PacketContainer> buildPacketList(boolean fake);

    /**
     * Method to load all locations between the two points
     *
     * @return list of Location
     */
    public List<Location> loadBlocksInBetween() {
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
     * Method to remove all blocks in selection
     */
    protected void removeOriginalBlocks() {
        getBlocksInBetween().forEach(location -> location.getBlock().setType(Material.AIR));
    }

    /**
     * Method to restore the original blocks
     */
    abstract void restoreOriginalBlocks();

    /**
     * Method to remove data from config
     */
    public void removeFromConfig() {
        FileConfiguration config = FakeBlock.getPlugin().getConfig();
        config.set(getName() + ".location1", null);
        config.set(getName() + ".location2", null);
        config.set(getName() + ".material-data", null);
        config.set(getName(), null);
        FakeBlock.getPlugin().saveConfig();
    }

    /**
     * Method to delete the wall
     */
    public void delete() {
        restoreOriginalBlocks();
        // Send updates to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendRealBlocks(player);
        }

        removeFromConfig();
        wallObjectList.remove(this);
    }

    /**
     * Method to load a map of chunks with their respective locations of fake blocks
     * Used for intermediate and legacy
     *
     * @return sorted map
     */
    protected HashMap<Chunk, List<Location>> loadSortedChunkMap() {
        HashMap<Chunk, List<Location>> sortedChunkMap = new HashMap<>();
        getBlocksInBetween().forEach(location -> {
            Chunk chunk = location.getChunk();
            List<Location> locationList = sortedChunkMap.getOrDefault(chunk, new ArrayList<>());
            locationList.add(location);
            sortedChunkMap.put(chunk, locationList);
        });
        return sortedChunkMap;
    }

    /**
     * Method to get the users who are going to be shown the wall
     * DO NOT USE THIS TO EDIT WHO WILL SEE THE WALL, instead use below methods!
     *
     * @return HashSet of UUID of the users who will see the walL
     */
    public HashSet<UUID> getUsersToDisplayFor() {
        return usersToDisplayFor;
    }

    /**
     * Helper method to add user to display the wall for
     *
     * @param player to display for
     */
    public void addUserToDisplayFor(Player player) {
        usersToDisplayFor.add(player.getUniqueId());
        FakeBlock.getWallUtility().processWall(player, 0, false);
    }

    /**
     * Helper method to remove user to display the wall for
     *
     * @param player to display for
     */
    public void removeUserToDisplayFor(Player player) {
        usersToDisplayFor.remove(player.getUniqueId());
        FakeBlock.getWallUtility().processWall(player, 0, false);
    }

    /**
     * Creates a non persistent wall through code
     *
     * @param fakeBlockDataHashMap data to create the wall with
     * @param location1            first corner of the wall
     * @param location2            second location of the corner
     * @return WallObject created
     */
    public WallObject createNonPersistentWall(HashMap<Location, FakeBlockData> fakeBlockDataHashMap,
                                              Location location1, Location location2) {
        this.loadingData = true;

        this.setLocation1(location1);
        this.setLocation2(location2);

        FakeBlock.newChain()
                .async(() -> {
                    this.setFakeBlockDataHashMap(fakeBlockDataHashMap);
                    this.blocksInBetween.addAll(fakeBlockDataHashMap.keySet());
                })
                .async(() -> {
                    this.fakeBlockPacketList = this.buildPacketList(true);
                    this.realBlockPacketList = this.buildPacketList(false);
                    this.loadingData = false;
                })
                .execute();

        return this;
    }
}
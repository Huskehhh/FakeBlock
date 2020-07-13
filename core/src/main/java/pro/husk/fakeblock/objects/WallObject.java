package pro.husk.fakeblock.objects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.hooks.ProtocolLibHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class WallObject {

    @Getter
    private static final List<WallObject> wallObjectList = new ArrayList<>();

    @Getter
    private final String name;

    @Getter
    @Setter
    protected List<Location> blocksInBetween;

    @Getter
    protected HashMap<Chunk, List<Location>> sortedChunkMap;

    @Getter
    protected List<PacketContainer> fakeBlockPacketList;

    @Getter
    @Setter
    private Location location1;

    @Getter
    @Setter
    private Location location2;

    @Getter
    protected boolean loadingData;

    /**
     * Constructor
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
     * Gets distance between the two location points
     *
     * @return distanceBetweenPoints
     */
    public double getDistanceBetweenPoints() {
        return getLocation1().distanceSquared(getLocation2());
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
            FakeBlock.newChain().async(() -> {
                List<PacketContainer> realPackets = buildPacketList(false);
                realPackets.forEach(packetContainer -> {
                    try {
                        ProtocolLibHelper.getProtocolManager().sendServerPacket(player, packetContainer);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            }).execute();
        }
    }

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

    abstract List<PacketContainer> buildPacketList(boolean fake);

    /**
     * Method to remove all blocks in selection
     */
    protected void removeOriginalBlocks() {
        getBlocksInBetween().forEach(location -> location.getBlock().setType(Material.AIR));
    }

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
}
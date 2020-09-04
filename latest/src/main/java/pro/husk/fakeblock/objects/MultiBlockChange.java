package pro.husk.fakeblock.objects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;

import java.util.ArrayList;

public class MultiBlockChange {

    private final ArrayList<Location> locationList;
    private final ArrayList<WrappedBlockData> blockDataList;

    /**
     * Constructor to create a new MultiBlockChange
     */
    protected MultiBlockChange() {
        this.locationList = new ArrayList<>();
        this.blockDataList = new ArrayList<>();
    }

    /**
     * Method to turn a location into a short ready for the MultiBlockChange packet
     * logic taken from PlayerChunk - thanks Paper for the simplification!
     *
     * @param location location to convert
     * @return short of Location with padding
     */
    private static short locToShort(Location location) {
        return (short) ((location.getBlockX() & 15) << 8 | (location.getBlockZ() & 15) << 4 | location.getBlockY() & 15);
    }

    /**
     * Method to add block data at the location given to the list of changes in the packet
     *
     * @param wrappedBlockData block data of the new block state
     * @param location         location of the block change
     */
    public void addBlockDataAtLocation(WrappedBlockData wrappedBlockData, Location location) {
        locationList.add(location);
        blockDataList.add(wrappedBlockData);
    }

    /**
     * Method to turn MultiBlockChange into a PacketContainer
     *
     * @return PacketContainer of the current MultiBlockChange instance
     */
    public PacketContainer build() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        short[] shortArray = new short[locationList.size()];
        WrappedBlockData[] blockDataArray = new WrappedBlockData[blockDataList.size()];

        for (int i = 0; i < blockDataList.size(); i++) {
            shortArray[i] = locToShort(locationList.get(i));
            blockDataArray[i] = blockDataList.get(i);
        }

        Location location = locationList.get(0);
        packet.getSectionPositions().writeSafely(0, new BlockPosition(location.getChunk().getX(), location.getBlockY() / 16, location.getChunk().getZ()));
        packet.getShortArrays().writeSafely(0, shortArray);
        packet.getBlockDataArrays().writeSafely(0, blockDataArray);

        return packet;
    }
}
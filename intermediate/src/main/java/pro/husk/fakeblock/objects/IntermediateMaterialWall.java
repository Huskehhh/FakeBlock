package pro.husk.fakeblock.objects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class IntermediateMaterialWall extends CommonMaterialWall {

    /**
     * Constructor for walls loaded from config
     *
     * @param name of wall
     */
    public IntermediateMaterialWall(String name) {
        super(name);
    }

    /**
     * Constructor when creating a new wall
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     */
    public IntermediateMaterialWall(String name, Location location1, Location location2) {
        super(name, location1, location2);
    }

    /**
     * Method to build the packets required for sending the blocks
     *
     * @param fake whether or not you want the real or fake blocks
     * @return list of PacketContainer ready to send to player
     */
    protected List<PacketContainer> buildPacketList(boolean fake) {
        List<PacketContainer> fakeBlockPackets = new ArrayList<>();

        BlockData dummyData = Material.AIR.createBlockData();

        loadSortedChunkMap().forEach((chunkMapKey, locationList) -> {
            PacketContainer fakeChunk = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
            ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkMapKey.getX(),
                    chunkMapKey.getZ());
            MultiBlockChangeInfo[] blockChangeInfo = new MultiBlockChangeInfo[locationList.size()];

            int i = 0;
            for (Location location : locationList) {
                BlockData blockData;
                if (fake) {
                    blockData = fakeBlockDataHashMap.getOrDefault(location, dummyData);
                } else {
                    blockData = location.getBlock().getBlockData();
                }
                blockChangeInfo[i] = new MultiBlockChangeInfo(location, WrappedBlockData.createData(blockData));
                i++;
            }

            fakeChunk.getChunkCoordIntPairs().write(0, chunkCoordIntPair);
            fakeChunk.getMultiBlockChangeInfoArrays().write(0, blockChangeInfo);

            fakeBlockPackets.add(fakeChunk);
        });

        return fakeBlockPackets;
    }
}
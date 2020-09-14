package pro.husk.fakeblock.objects;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class LatestMaterialWall extends CommonMaterialWall {

    /**
     * Constructor for walls loaded from config
     *
     * @param name of wall
     */
    public LatestMaterialWall(String name) {
        super(name);
    }

    /**
     * Constructor when creating a new wall
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     */
    public LatestMaterialWall(String name, Location location1, Location location2) {
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

        MultiBlockChangeHandler handler = new MultiBlockChangeHandler();
        for (Location location : getBlocksInBetween()) {
            BlockPosition blockPosition =
                    new BlockPosition(location.getChunk().getX(), location.getBlockY() >> 4, location.getChunk().getZ());

            MultiBlockChange multiBlockChange = handler.getOrCreate(blockPosition);
            BlockData blockData;

            if (fake) {
                blockData = fakeBlockDataHashMap.getOrDefault(location, Material.AIR.createBlockData());
            } else {
                blockData = location.getBlock().getBlockData();
            }

            multiBlockChange.addBlockDataAtLocation(WrappedBlockData.createData(blockData), location);
        }

        handler.getMultiBlockChangeHashMap().values().forEach(multiBlockChange ->
                fakeBlockPackets.add(multiBlockChange.build()));

        return fakeBlockPackets;
    }
}
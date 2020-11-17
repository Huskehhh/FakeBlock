package pro.husk.fakeblock.objects;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Wrapper object to encompass BlockData for use in data HashMaps
 */
public class FakeBlockData {

    @Getter
    private BlockData blockData;

    @Getter
    private Material material;

    @Getter
    private byte data;

    /**
     * Constructor for latest + intermediate
     *
     * @param blockData of the block
     */
    public FakeBlockData(BlockData blockData) {
        this.blockData = blockData;
    }

    /**
     * Constructor for legacy
     *
     * @param material of the block
     * @param data     of the material
     */
    public FakeBlockData(Material material, byte data) {
        this.material = material;
        this.data = data;
    }
}
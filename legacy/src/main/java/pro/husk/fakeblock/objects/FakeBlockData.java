package pro.husk.fakeblock.objects;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

public class FakeBlockData {

    @Getter
    private final Material material;

    @Getter
    private final byte data;

    /**
     * Wrapper for BlockData
     *
     * @param material material of the block
     * @param data     data of the block
     */
    public FakeBlockData(Material material, byte data) {
        this.material = material;
        this.data = data;
    }

    /**
     * Takes location instead of Material and byte
     *
     * @param location location of the block
     */
    public FakeBlockData(Location location) {
        this(location.getBlock().getType(), location.getBlock().getData());
    }
}

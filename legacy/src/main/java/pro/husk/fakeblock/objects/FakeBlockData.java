package pro.husk.fakeblock.objects;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

public class FakeBlockData {

    @Getter
    private final Material material;

    @Getter
    private final byte data;

    public FakeBlockData(Material material, byte data) {
        this.material = material;
        this.data = data;
    }

    public FakeBlockData(Location location) {
        this(location.getBlock().getType(), location.getBlock().getData());
    }
}

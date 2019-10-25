package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;

import java.util.ArrayList;
import java.util.List;

public class IDWall extends WallObject {

    @Getter
    private static List<IDWall> materialWallList = new ArrayList<IDWall>();

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private int data;

    /**
     * Constructor
     *
     * @param name of wall
     */
    public IDWall(String name) {
        super(name);

        materialWallList.add(this);
    }

    /**
     * Constructor
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     * @param id        of wall material
     * @param data      of wall material
     */
    public IDWall(String name, Location location1, Location location2, int id, int data) {
        this(name);

        setLocation1(location1);
        setLocation2(location2);
        setId(id);
        setData(data);
    }

    /**
     * Method to render wall for player
     *
     * @param player to render wall for
     */
    @Override
    public void renderWall(Player player) {
        for (Location location : getBlocksInBetween()) {
            player.sendBlockChange(location, Material.getMaterial(id), (byte) data);
        }
    }

    /**
     * Method to load wall from config
     */
    @Override
    public void loadWall() {
        Location location1 = (Location) FakeBlock.getPlugin().getConfig().get(getName() + ".location1");
        Location location2 = (Location) FakeBlock.getPlugin().getConfig().get(getName() + ".location2");

        if (location1.getWorld() == location2.getWorld()) {
            setLocation1(location1);
            setLocation2(location2);

            id = FakeBlock.getPlugin().getConfig().getInt(getName() + ".id");
            data = FakeBlock.getPlugin().getConfig().getInt(getName() + ".data");

            FakeBlock.getConsole().info("Loaded wall '" + getName() + "' successfully");
        } else {
            FakeBlock.getConsole().warning("[FakeBlock] Wall '" + getName() + "' is configured wrong, the world cannot be different");
        }
    }

    /**
     * Method to save wall to config
     */
    @Override
    public void saveWall() {
        FakeBlock.getPlugin().getConfig().set(getName() + ".location1", getLocation1());
        FakeBlock.getPlugin().getConfig().set(getName() + ".location1", getLocation2());
        FakeBlock.getPlugin().getConfig().set(getName() + ".id", getId());
        FakeBlock.getPlugin().getConfig().set(getName() + ".data", getData());
        FakeBlock.getPlugin().saveConfig();
    }
}

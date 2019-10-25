package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MaterialWall extends WallObject {

    @Getter
    private static List<MaterialWall> materialWallList = new ArrayList<MaterialWall>();

    @Getter
    @Setter
    private Material material;

    /**
     * Constructor
     *
     * @param name of wall
     */
    public MaterialWall(String name) {
        super(name);
    }

    /**
     * Constructor
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     * @param material  of wall
     */
    public MaterialWall(String name, Location location1, Location location2, Material material) {
        this(name);

        setLocation1(location1);
        setLocation2(location2);

        this.material = material;
    }

    /**
     * Method to render fake block wall for player
     *
     * @param player to render for
     */
    @Override
    public void renderWall(Player player) {
        for (Location location : getBlocksInBetween()) {
            player.sendBlockChange(location, material.createBlockData());
        }
    }

    /**
     * Method to load wall from config
     */
    @Override
    public void loadWall() {
        Location location1 = (Location) FakeBlock.getPlugin().getConfig().get(getName() + ".location1");
        Location location2 = (Location) FakeBlock.getPlugin().getConfig().get(getName() + ".location2");

        if (location1 != null && location2 != null) {

            if (location1.getWorld() == location2.getWorld()) {
                setLocation1(location1);
                setLocation2(location2);

                String materialName = FakeBlock.getPlugin().getConfig().getString(getName() + ".material");
                Material material = Material.matchMaterial(materialName);

                if (material != null) {
                    this.material = material;
                }

                FakeBlock.getConsole().info("Loaded wall '" + getName() + "' successfully");
            } else {
                FakeBlock.getConsole().warning("[FakeBlock] Wall '" + getName() + "' is configured wrong, the world cannot be different");
            }
        }
    }

    /**
     * Method to save wall to config
     */
    @Override
    public void saveWall() {
        FakeBlock.getPlugin().getConfig().set(getName() + ".location1", getLocation1());
        FakeBlock.getPlugin().getConfig().set(getName() + ".location2", getLocation2());
        FakeBlock.getPlugin().getConfig().set(getName() + ".material", getMaterial().toString());
        FakeBlock.getPlugin().saveConfig();
    }

    @Override
    public void sendRealBlocks(Player player) {
        CompletableFuture<WallObject> future = CompletableFuture.supplyAsync(() -> {
            return FakeBlock.getPlugin().isNearWall(player.getLocation());
        });

        future.thenRun(() -> {
            try {
                WallObject wall = future.get();

                if (wall != null) {
                    for (Location location : wall.getBlocksInBetween()) {
                        Block block = location.getBlock();
                        player.sendBlockChange(location, block.getBlockData());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method to remove data from config
     */
    @Override
    public void removeFromConfig() {
        setLocation1(null);
        setLocation2(null);
        setMaterial(null);

        saveWall();
    }
}

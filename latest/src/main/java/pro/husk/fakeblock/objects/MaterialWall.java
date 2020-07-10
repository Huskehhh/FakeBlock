package pro.husk.fakeblock.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pro.husk.fakeblock.FakeBlock;

public class MaterialWall extends WallObject {

    /**
     * Constructor for walls loaded from config
     *
     * @param name of wall
     */
    public MaterialWall(String name) {
        super(name);
    }

    /**
     * Constructor when creating a new wall
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     */
    public MaterialWall(String name, Location location1, Location location2) {
        this(name);

        setLocation1(location1);
        setLocation2(location2);

        this.loadingData = true;
        FakeBlock.newChain()
                .async(() -> {
                    this.blocksInBetween = loadBlocksInBetween();
                    this.materialMap = generateMaterialMapFromWorld();
                })
                .sync(this::removeOriginalBlocks)
                .async(() -> {
                    this.sortedChunkMap = loadSortedChunkMap();
                    this.fakeBlockPacketList = loadPacketList(true);
                    this.loadingData = false;
                    saveWall();
                }).execute();
    }

    /**
     * Method to load wall from config
     */
    @Override
    public void loadWall() {
        this.loadingData = true;
        FakeBlock.newChain().async(() -> {
            FileConfiguration config = FakeBlock.getPlugin().getConfig();

            Location location1 = config.getLocation(getName() + ".location1");
            Location location2 = config.getLocation(getName() + ".location2");

            if (location1 != null && location2 != null) {

                if (location1.getWorld() == location2.getWorld()) {
                    setLocation1(location1);
                    setLocation2(location2);

                    ConfigurationSection configurationSection = config.getConfigurationSection(getName() + ".material-data");

                    if (configurationSection == null) return;

                    configurationSection.getKeys(false).forEach(key -> {
                        String[] split = key.split(",");

                        World world = Bukkit.getWorld(split[0]);
                        int x = Integer.parseInt(split[1]);
                        int y = Integer.parseInt(split[2]);
                        int z = Integer.parseInt(split[3]);

                        Location built = new Location(world, x, y, z);
                        String materialString = configurationSection.getString(key);

                        if (materialString != null) {
                            Material material = Material.getMaterial(materialString);
                            materialMap.put(built, material);
                        }
                    });

                    // Load all data to cache
                    this.blocksInBetween = loadBlocksInBetween();
                    this.sortedChunkMap = loadSortedChunkMap();
                    this.fakeBlockPacketList = loadPacketList(true);
                    this.loadingData = false;
                    FakeBlock.getConsole().info("Loaded wall '" + getName() + "' successfully");
                } else {
                    FakeBlock.getConsole().warning("Wall '" + getName() + "' is configured wrong, the world cannot be different");
                }
            }
        }).execute();
    }

    /**
     * Method to save wall to config
     */
    @Override
    public void saveWall() {
        FakeBlock plugin = FakeBlock.getPlugin();
        FileConfiguration config = plugin.getConfig();
        config.set(getName() + ".location1", getLocation1());
        config.set(getName() + ".location2", getLocation2());

        getMaterialMap().keySet().forEach(location -> {
            String locationAsKey = location.getWorld().getName()
                    + "," + location.getBlockX() + ","
                    + location.getBlockY() + "," + location.getBlockZ();

            Material material = getMaterialMap().getOrDefault(location, Material.AIR);

            if (material == Material.AIR) return;

            String materialString = material.toString();

            config.set(getName() + ".material-data." + locationAsKey, materialString);
        });

        plugin.saveConfig();
    }
}

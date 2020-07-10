package pro.husk.fakeblock.objects;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pro.husk.fakeblock.FakeBlock;

import java.util.HashMap;

public class IDWall extends WallObject {

    @Getter
    private HashMap<Location, Byte> blockDataMap;

    /**
     * Constructor for walls loaded from config
     *
     * @param name of wall
     */
    public IDWall(String name) {
        super(name);
    }

    /**
     * Constructor when creating a new wall
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     */
    public IDWall(String name, Location location1, Location location2) {
        this(name);

        setLocation1(location1);
        setLocation2(location2);

        FakeBlock.newChain().async(() -> {
            this.blocksInBetween = loadBlocksInBetween();
            this.materialMap = generateMaterialMapFromWorld();
            this.blockDataMap = loadDataMap();
        }).sync(this::removeOriginalBlocks).async(() -> {
            this.sortedChunkMap = loadSortedChunkMap();
            this.fakeBlockPacketList = loadPacketList(true);
            saveWall();
        }).execute();
    }

    /**
     * Method to load wall from config
     */
    @Override
    public void loadWall() {
        FakeBlock.newChain().async(() -> {
            FileConfiguration config = FakeBlock.getPlugin().getConfig();

            Location location1 = (Location) config.get(getName() + ".location1");
            Location location2 = (Location) config.get(getName() + ".location2");

            if (location1 != null && location2 != null) {
                if (location1.getWorld() == location2.getWorld()) {
                    setLocation1(location1);
                    setLocation2(location2);

                    this.materialMap = new HashMap<>();
                    this.blockDataMap = new HashMap<>();

                    ConfigurationSection materialSection = config.getConfigurationSection(getName() + ".material-data");

                    if (materialSection == null) return;

                    materialSection.getKeys(false).forEach(key -> {
                        String[] split = key.split(",");

                        World world = Bukkit.getWorld(split[0]);
                        int x = Integer.parseInt(split[1]);
                        int y = Integer.parseInt(split[2]);
                        int z = Integer.parseInt(split[3]);

                        Location built = new Location(world, x, y, z);
                        String materialDataString = materialSection.getString(key);

                        String[] dataSplit = materialDataString.split(":");

                        String materialString = dataSplit[0];
                        byte blockData = Byte.parseByte(dataSplit[1]);

                        if (materialString != null) {
                            Material material = Material.getMaterial(materialString);
                            materialMap.put(built, material);
                        }

                        blockDataMap.put(built, blockData);
                    });

                    // Load all data to cache
                    this.blocksInBetween = loadBlocksInBetween();
                    this.sortedChunkMap = loadSortedChunkMap();
                    this.fakeBlockPacketList = loadPacketList(true);
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
            byte dataByte = blockDataMap.get(location);

            String saveString = materialString + ":" + dataByte;

            config.set(getName() + ".material-data." + locationAsKey, saveString);
        });

        plugin.saveConfig();
    }

    private HashMap<Location, Byte> loadDataMap() {
        HashMap<Location, Byte> dataMap = new HashMap<>();
        getBlocksInBetween().forEach(location -> dataMap.put(location, location.getBlock().getData()));
        return dataMap;
    }
}

package pro.husk.fakeblock.objects;

import pro.husk.fakeblock.FakeBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class Wall {

    /**
     * Stores all Wall objects while runtime is active
     */

    public static HashMap<String, Wall> wallObjects = new HashMap<String, Wall>();

    private String name;
    private String blockName;
    private Location loc1;
    private Location loc2;

    private ArrayList<Location> locations = new ArrayList<Location>();
    private ArrayList<Location> blocks = new ArrayList<Location>();

    /**
     * Constructor
     *
     * @param loc1      - First location
     * @param loc2      - First location
     * @param name      - Name of the Wall
     * @param blockName - Block name of the Wall
     */

    public Wall(Location loc1, Location loc2, String name, String blockName) {
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.name = name;
        this.blockName = blockName;

        wallObjects.put(name, this);
        writeToConfig();

        locations.add(loc1);
        locations.add(loc2);

        generateBlockArray();
    }

    /**
     * Loads all the Walls in the config file
     */

    public static void loadWalls() {

        List<String> configWalls = getAllWalls();

        ListIterator<String> li = configWalls.listIterator();

        while (li.hasNext()) {
            String name = li.next();
            String data = FakeBlock.config.getString(name + ".data");
            Wall create = objectFromString(data, name);
            wallObjects.put(name, create);
        }

        System.out.println("[FakeBlock] Walls loaded successfully");
    }

    /**
     * Unloads all the Walls currently loaded.
     */

    public static void unloadWalls() {
        wallObjects.clear();
    }

    /**
     * Converts a Wall objects to String
     *
     * @return String of the current Wall objects
     */

    public String convertToString() {
        String sep = ",";

        int x = (int) loc1.getX();
        int x1 = (int) loc2.getX();
        int y = (int) loc1.getY();
        int y1 = (int) loc2.getY();
        int z = (int) loc1.getZ();
        int z1 = (int) loc2.getZ();

        String worldname = loc1.getWorld().getName();


        return x + sep + y + sep + z + sep + worldname + sep + x1 + sep + y1 + sep + z1 + sep + blockName;
    }

    /**
     * Creates an objects from a string containing all the information a wall contains
     * Used for loading from config.
     *
     * @param data - Data in String form
     * @param name - Name of the Wall
     * @return new Wall objects containing given data.
     */

    public static Wall objectFromString(String data, String name) {

        String[] split = data.split(",");
        int x = convert(split[0]);
        int y = convert(split[1]);
        int z = convert(split[2]);
        String world = split[3];
        int x1 = convert(split[4]);
        int y1 = convert(split[5]);
        int z1 = convert(split[6]);
        String blockname = split[7];


        World w = Bukkit.getWorld(world);

        Location buildLoc1 = new Location(w, x, y, z);
        Location buildLoc2 = new Location(w, x1, y1, z1);


        return new Wall(buildLoc1, buildLoc2, name, blockname);
    }

    /**
     * Returns the Integer value of a string
     *
     * @param num - Number in String form
     * @return Integer value of num
     */

    public static int convert(String num) {
        return Integer.parseInt(num);
    }

    /**
     * Returns the World name of the Wall
     *
     * @return World name of the Wall
     */

    public String getWorldname() {
        return loc1.getWorld().getName();
    }

    /**
     * Returns the locations in an arraylist
     *
     * @return arraylist of locations
     */

    public List<Location> getLocations() {
        return locations;
    }

    /**
     * Method to isolate loc1
     *
     * @return location number 1
     */
    public Location getLoc1() {
        return loc1;
    }

    /**
     * Method to isolate loc2
     *
     * @return location number 2
     */

    public Location getLoc2() {
        return loc2;
    }

    /**
     * Returns the name of the Wall
     *
     * @return Name of the Wall
     */

    public String getName() {
        return name;
    }

    /**
     * Returns block name of the Wall
     *
     * @return Block name of the Wall
     */

    public String getBlockName() {
        return blockName;
    }

    /**
     * Method to set block type to something different
     *
     * @param blockName new block name
     */

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    /**
     * Gets generated block location array
     *
     * @return block location array
     */

    public ArrayList<Location> getBlocks() {
        return this.blocks;
    }

    /**
     * Gets distance between the two location points
     *
     * @return distanceBetweenPoints
     */
    public double getDistanceBetweenPoints() {
        return loc1.distanceSquared(loc2);
    }

    /**
     * Retrieves wall by name
     *
     * @param name - Name of the wall to be retrieved
     * @return Wall objects from Name
     */

    public static Wall getByName(String name) {
        return wallObjects.get(name);
    }

    /**
     * Removes wall by name
     *
     * @param name - Name of the wall
     */

    public static void removeByName(String name) {
        getByName(name).removeFromConfig();
        wallObjects.remove(name);
    }


    /**
     * Removes objects from config
     */

    public void removeFromConfig() {
        if (FakeBlock.config.getString(name + ".data") != null) {

            FakeBlock.config.set(name + ".data", null);

            List<String> ls = getAllWalls();

            ls.remove(name);

            FakeBlock.config.set("walls.list", ls);

            try {
                FakeBlock.config.save("plugins/FakeBlock/config.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes objects in string form to config.
     */

    private void writeToConfig() {

        if (FakeBlock.config.getString(name + ".data") == null) {
            String converted = convertToString();

            YamlConfiguration config = FakeBlock.config;

            config.set(name + ".data", converted);
            List<String> ls = getAllWalls();
            ls.add(name);
            config.set("walls.list", ls);

            try {
                config.save("plugins/FakeBlock/config.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get all blocks in a Wall
     * Also calculates distance between the two points
     *
     * @return ArrayList of locations that contains all block locations
     */

    private void generateBlockArray() {

        World w = loc1.getWorld();

        int bx = (int) getLoc1().getX();
        int bx1 = (int) getLoc2().getX();
        int by = (int) getLoc1().getY();
        int by1 = (int) getLoc2().getY();
        int bz = (int) getLoc1().getZ();
        int bz1 = (int) getLoc2().getZ();

        for (int x = Math.min(bx, bx1); x <= Math.max(bx, bx1); ++x) {
            for (int y = Math.min(by, by1); y <= Math.max(by, by1); ++y) {
                for (int z = Math.min(bz, bz1); z <= Math.max(bz, bz1); ++z) {
                    blocks.add(new Location(w, x, y, z));
                }
            }
        }
    }

    /**
     * Get StringList of Walls in config
     *
     * @return StringList of Walls
     */

    public static List<String> getAllWalls() {
        return FakeBlock.config.getStringList("walls.list");
    }

    /**
     * Get all Walls from config
     *
     * @return Walls from config
     */

    public static List<Wall> getWalls() {
        List<Wall> allWalls = new ArrayList<Wall>();

        List<String> configWalls = getAllWalls();

        ListIterator<String> li = configWalls.listIterator();

        while (li.hasNext()) {
            String name = li.next();
            Wall wall = Wall.getByName(name);
            allWalls.add(wall);
        }

        return allWalls;
    }

}

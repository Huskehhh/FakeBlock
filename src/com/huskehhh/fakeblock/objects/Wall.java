package com.huskehhh.fakeblock.objects;

import com.huskehhh.fakeblock.FakeBlock;
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

    String name;
    String blockname;
    Location loc1;
    Location loc2;

    private List<Location> locations = new ArrayList<Location>();

    /**
     * Constructor
     *
     * @param loc1      - First location
     * @param loc2      - First location
     * @param name      - Name of the Wall
     * @param blockname - Block name of the Wall
     */

    public Wall(Location loc1, Location loc2, String name, String blockname) {
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.name = name;
        this.blockname = blockname;

        wallObjects.put(name, this);
        writeToConfig(name);

        locations.add(loc1);
        locations.add(loc2);
    }

    /**
     * Loads all the Walls in the config file
     */

    public static void loadWalls() {

        List<String> configWalls = FakeBlock.plugin.getAllWalls();

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
        FakeBlock.plugin.getWalls().clear();
    }

    /**
     * Converts a Wall object to String
     *
     * @return String of the current Wall object
     */

    public String convertToString() {
        String sep = ",";

        double x = loc1.getX();
        double x1 = loc2.getX();
        double y = loc1.getY();
        double y1 = loc2.getY();
        double z = loc1.getZ();
        double z1 = loc2.getZ();

        String worldname = loc1.getWorld().getName();


        return x + sep + y + sep + z + sep + worldname + sep + x1 + sep + y1 + sep + z1 + sep + blockname;
    }

    /**
     * Creates an object from a string containing all the information a wall contains
     * Used for loading from config.
     *
     * @param data - Data in String form
     * @param name - Name of the Wall
     * @return new Wall object containing given data.
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
        return blockname;
    }

    /**
     * Retrieves wall by name
     *
     * @param name - Name of the wall to be retrieved
     * @return Wall object from Name
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
        wallObjects.remove(name);
    }

    /**
     * Writes object in string form to config.
     *
     * @param nme - Object that's converted to string to be written to config.
     */

    public void writeToConfig(String nme) {

        if (FakeBlock.config.getString(nme + ".data") == null) {
            String converted = convertToString();
            YamlConfiguration config = FakeBlock.config;

            config.set(nme + ".data", converted);
            List<String> ls = config.getStringList("walls.list");
            ls.add(nme);
            config.set("walls.list", ls);

            try {
                config.save("plugins/FakeBlock/config.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.huskehhh.fakeblock.objects;

import com.huskehhh.fakeblock.util.Utility;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class Wall {

    /**
     * Stores all Wall objects while runtime is active
     */

    public static HashMap<String, Wall> wallObjects = new HashMap<String, Wall>();

    public int x, y, z, x1, y1, z1, id, data;
    public String worldname;
    public String name;

    /**
     * Constructor
     *
     * @param x         - First x coordinate
     * @param y         - First y coordinate
     * @param z         - First z coordinate
     * @param worldname - Name of the world where the Wall was created
     * @param x1        - Second x coordinate
     * @param y1        - Second y coordinate
     * @param z1        - Second z coordinate
     * @param name      - Name of the Wall
     * @param id        - Block ID of the Wall
     * @param data      - Material data of the wall
     */

    public Wall(int x, int y, int z, String worldname, int x1, int y1, int z1, String name, int id, int data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldname = worldname;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.name = name;
        this.id = id;
        this.data = data;

        wallObjects.put(name, this);
        writeToConfig(name);
    }

    /**
     * Loads all the Walls in the config file
     */

    public static void loadWalls() {

        List<String> configWalls = Utility.getAllWalls();

        ListIterator<String> li = configWalls.listIterator();

        while (li.hasNext()) {
            String name = li.next();
            String data = Utility.config.getString(name + ".data");
            Wall create = objectFromString(data, name);
            wallObjects.put(name, create);
        }

        System.out.println("[FakeBlock] Walls loaded successfully");
    }

    /**
     * Unloads all the Walls currently loaded.
     */

    public static void unloadWalls() {
        Utility.getWalls().clear();
    }

    /**
     * Converts a Wall object to String
     *
     * @return String of the current Wall object
     */

    public String convertToString() {
        String sep = ",";
        return x + sep + y + sep + z + sep + worldname + sep + x1 + sep + y1 + sep + z1 + sep + id + sep + data;
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
        int id = convert(split[7]);

        int dataID = 0;

        if (split.length == 9) {
            dataID = convert(split[8]);
        }

        return new Wall(x, y, z, world, x1, y1, z1, name, id, dataID);
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
        return worldname;
    }

    /**
     * Returns the first 'x' coordinate
     *
     * @return First x coordinate
     */

    public int getX() {
        return x;
    }

    /**
     * Returns the second 'x' coordinate
     *
     * @return Second x coordinate
     */

    public int getX1() {
        return x1;
    }

    /**
     * Returns the first 'y' coordinate
     *
     * @return First y coordinate
     */

    public int getY() {
        return y;
    }

    /**
     * Returns the second 'y' coordinate
     *
     * @return Second y coordinate
     */

    public int getY1() {
        return y1;
    }

    /**
     * Returns the first 'z' coordinate
     *
     * @return First z coordinate
     */

    public int getZ() {
        return z;
    }

    /**
     * Returns the second 'z' coordinate
     *
     * @return Second z coordinate
     */

    public int getZ1() {
        return z1;
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
     * Returns ID of the Wall
     *
     * @return Block ID of the Wall
     */

    public int getId() {
        return id;
    }

    /**
     * Returns material data of Wall
     *
     * @return material data
     */

    public int getData() {
        return data;
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
        Wall wall = getByName(name);
        wallObjects.remove(name);
        wall = null;
    }

    /**
     * Writes object in string form to config.
     *
     * @param nme - Object that's converted to string to be written to config.
     */

    public void writeToConfig(String nme) {

        if (Utility.config.getString(nme + ".data") == null) {
            String converted = convertToString();
            YamlConfiguration config = Utility.config;

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

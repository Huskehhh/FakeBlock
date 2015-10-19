package com.huskehhh.fakeblock.objects;

import com.huskehhh.fakeblock.Utility;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class Wall {

    public static HashMap<String, Wall> wallObjects = new HashMap<String, Wall>();

    public int x, y, z, x1, y1, z1, id, data;
    public String worldname;
    public String name;

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

    public static void unloadWalls() {
        List<Wall> allWalls = Utility.getWalls();
        ListIterator<Wall> wallIterator = allWalls.listIterator();

        while (wallIterator.hasNext()) {
            Wall next = wallIterator.next();
            next.removeByName(next.getName());
        }
    }

    public String convertToString() {
        String sep = ",";
        return x + sep + y + sep + z + sep + worldname + sep + x1 + sep + y1 + sep + z1 + sep + id;
    }

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
        int dataID = convert(split[8]);

        return new Wall(x, y, z, world, x1, y1, z1, name, id, dataID);
    }

    public static int convert(String num) {
        return Integer.parseInt(num);
    }

    public String getWorldname() {
        return worldname;
    }

    public int getX() {
        return x;
    }

    public int getX1() {
        return x1;
    }

    public int getY() {
        return y;
    }

    public int getY1() {
        return y1;
    }

    public int getZ() {
        return z;
    }

    public int getZ1() {
        return z1;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getData() { return data; }


    public static Wall getByName(String name) {
        return wallObjects.get(name);
    }

    public static void removeByName(String name) {
        Wall wall = getByName(name);
        wallObjects.remove(name);
        wall = null;
    }

    public void writeToConfig(String nme) {

        if (Utility.config.getString(nme + ".data") == null) {
            String converted = convertToString();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

            config.set(nme + ".data", converted);
            List<String> ls = config.getStringList("walls.list");
            ls.add(nme);
            config.set("walls.list", ls);

            try {
                config.save("plugins/FakeBlock/config.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Utility api = new Utility();
            for (Player server : Bukkit.getServer().getOnlinePlayers()) {
                api.sendFakeBlocks(server);
            }

        }

    }

}

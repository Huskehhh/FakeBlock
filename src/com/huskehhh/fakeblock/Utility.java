package com.huskehhh.fakeblock;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Utility {

    /**
     * Data will be stored like
     * walls.'x,y,z,world,x1,y1,z1,block-id'
     */

    private static YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

    public static List<String> getAllWalls() {
        return config.getStringList("walls");
    }

    public static List<Wall> getWalls() {
        List<Wall> walls = new ArrayList<Wall>();
        List<String> wall = getAllWalls();
        ListIterator<String> li = wall.listIterator();
        while (li.hasNext()) {
            walls.add(Wall.objectFromString(li.next()));
        }
        return walls;
    }


    public void sendFakeBlocks(Player p) {

    }
}
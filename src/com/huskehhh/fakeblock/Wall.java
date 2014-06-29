package com.huskehhh.fakeblock;

public class Wall {

    public int x, y, z, id, x1, y1, z1;
    public String worldname;

    public Wall(int x, int y, int z, String worldname, int x1, int y1, int z1, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldname = worldname;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.id = id;
    }

    public String convertToString() {
        String sep = ",";
        return x + sep + y + sep + z + sep + worldname + sep + x1 + sep + y1 + sep + z1 + sep + id;
    }

    public static Wall objectFromString(String data) {
        String[] split = data.split(",");
        int x = convert(split[0]);
        int y = convert(split[1]);
        int z = convert(split[2]);
        String world = split[3];
        int x1 = convert(split[4]);
        int y1 = convert(split[5]);
        int z1 = convert(split[6]);
        int id = convert(split[7]);
        return new Wall(x,y,z,world,x1,y1,z1,id);
    }

    private static int convert(String num) {
        return Integer.getInteger(num);
    }


}

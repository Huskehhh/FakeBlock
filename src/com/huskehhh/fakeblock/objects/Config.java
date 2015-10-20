package com.huskehhh.fakeblock.objects;

public class Config {

    public int x, y, z, x1, y1, z1, id, data = 0;
    public String worldname;
    public String name;

    public void setX(int x) {
        this.x = x;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public void setZ(int z) { this.z = z; }

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorldname(String worldname) { this.worldname = worldname; }

    public void setId(int id) {
        this.id = id;
    }

    public void setData(int data) { this.data = data; }

    public Wall createObject() {
        return new Wall(x, y, z, worldname, x1, y1, z1, name, id, data);
    }


}

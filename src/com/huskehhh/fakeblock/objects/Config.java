package com.huskehhh.fakeblock.objects;

public class Config {

    public int x, y, z, x1, y1, z1, id, data = 0;
    public String worldname;
    public String name;

    /**
     * Set first x coordinate
     *
     * @param x - First x coordinate of selected location
     */

    public void setX(int x) {
        this.x = x;
    }

    /**
     * Set second x coordinate
     *
     * @param x1 - Second x coordinate of selected location
     */

    public void setX1(int x1) {
        this.x1 = x1;
    }

    /**
     * Set first y coordinate
     *
     * @param y - First y coordinate of selected location
     */

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Set second x coordinate
     *
     * @param y1 - Second x coordinate of selected location
     */

    public void setY1(int y1) {
        this.y1 = y1;
    }

    /**
     * Set first z coordinate
     *
     * @param z - First z coordinate of selected location
     */

    public void setZ(int z) {
        this.z = z;
    }

    /**
     * Set second z coordinate
     *
     * @param z1 - Second z coordinate of selected location
     */

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    /**
     * Set name of the Wall to be created
     *
     * @param name - Name of the Wall given through command
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the name of the world the Wall is to be created in
     *
     * @param worldname - Name of the world taken from block selection location
     */

    public void setWorldname(String worldname) {
        this.worldname = worldname;
    }

    /**
     * Set block ID of the Wall to be created
     *
     * @param id - ID Given through command
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Set material data of the block
     *
     * @param data - Material data integer value
     */

    public void setData(int data) {
        this.data = data;
    }

    /**
     * Create a Wall object from stored data.
     *
     * @return Wall object from data
     */

    public Wall createObject() {
        return new Wall(x, y, z, worldname, x1, y1, z1, name, id, data);
    }


}

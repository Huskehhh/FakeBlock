package pro.husk.fakeblock;

import org.bukkit.entity.Player;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.WallObject;

public interface FakeBlockModuleHandler {

    /**
     * Method to load all walls
     */
    abstract void loadWalls();

    /**
     * Method to load wall given a config object
     *
     * @param config object to get values from
     * @return WallObject child
     */
    abstract WallObject loadWall(Config config);
}

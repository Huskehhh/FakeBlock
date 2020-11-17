package pro.husk.fakeblock;

import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.WallObject;

/**
 * Module handler to abstract the loading of WallObjects to version specific implementations
 */
public interface FakeBlockModuleHandler {

    /**
     * Method to load all walls
     */
    void loadWalls();

    /**
     * Method to load wall given a config object
     *
     * @param config object to get values from
     * @return WallObject child
     */
    WallObject loadWall(Config config);

    /**
     * Register listeners for module
     */
    void registerListeners();
}

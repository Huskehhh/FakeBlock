package pro.husk.fakeblock.modules;

import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.FakeBlockModuleHandler;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.MaterialWall;
import pro.husk.fakeblock.objects.WallObject;

public class LatestModule implements FakeBlockModuleHandler {

    /**
     * Method to load walls from config
     */
    @Override
    public void loadWalls() {
        FakeBlock.getPlugin().getConfig().getKeys(false).forEach(key -> new MaterialWall(key).loadWall());
    }

    /**
     * Method to load wall from config object
     *
     * @param config object to get values from
     * @return MaterialWall object
     */
    @Override
    public WallObject loadWall(Config config) {
        return new MaterialWall(config.getWallName(), config.getLocation1(), config.getLocation2(), config.getMaterial());
    }
}

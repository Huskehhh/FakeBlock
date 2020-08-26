package pro.husk.fakeblock.modules;

import org.bukkit.Bukkit;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.FakeBlockModuleHandler;
import pro.husk.fakeblock.listener.CommonSelectionListener;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.IntermediateMaterialWall;
import pro.husk.fakeblock.objects.WallObject;

public class IntermediateModule implements FakeBlockModuleHandler {

    /**
     * Method to load walls from config
     */
    @Override
    public void loadWalls() {
        FakeBlock.getPlugin().getConfig().getKeys(false).forEach(key -> {
            if (!key.equalsIgnoreCase("inverse-permission-check")) new IntermediateMaterialWall(key).loadWall();
        });
    }

    /**
     * Method to load wall from config object
     *
     * @param config object to get values from
     * @return MaterialWall object
     */
    @Override
    public WallObject loadWall(Config config) {
        return new IntermediateMaterialWall(config.getWallName(), config.getLocation1(), config.getLocation2());
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new CommonSelectionListener(), FakeBlock.getPlugin());
    }
}

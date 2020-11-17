package pro.husk.fakeblock.modules;

import org.bukkit.Bukkit;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.FakeBlockModuleHandler;
import pro.husk.fakeblock.listener.LegacySelectionListener;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.IDWall;
import pro.husk.fakeblock.objects.WallObject;

/**
 * Module for legacy versions of Minecraft
 */
public class LegacyModule implements FakeBlockModuleHandler {

    /**
     * Method to load walls from config
     */
    @Override
    public void loadWalls() {
        FakeBlock.getPlugin().getConfig().getKeys(false).forEach(key -> {
            if (!key.equalsIgnoreCase("inverse-permission-check")) new IDWall(key).loadWall();
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
        return new IDWall(config.getWallName(), config.getLocation1(), config.getLocation2());
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new LegacySelectionListener(), FakeBlock.getPlugin());
    }
}

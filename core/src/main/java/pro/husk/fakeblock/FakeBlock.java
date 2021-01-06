package pro.husk.fakeblock;

import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pro.husk.fakeblock.commands.CommandHandler;
import pro.husk.fakeblock.hooks.LuckPermsHelper;
import pro.husk.fakeblock.hooks.ProtocolLibHelper;
import pro.husk.fakeblock.listeners.FakeBlockListener;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;
import pro.husk.fakeblock.objects.WallUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Entry point for the FakeBlock plugin
 *
 * @author Jordyn Newnham
 */
public class FakeBlock extends JavaPlugin {

    @Getter
    private static FakeBlock plugin;

    @Getter
    private static WallUtility wallUtility;

    @Getter
    private static Logger console;

    @Getter
    private static FakeBlockModuleHandler fakeBlockModuleHandler;

    private static TaskChainFactory taskChainFactory;
    private static PaperCommandManager manager;

    @Getter
    private static Language language;

    @Getter
    private static YamlConfiguration languageConfig;

    /**
     * Returns a new TaskChain
     *
     * @param <T> generic type
     * @return new TaskChain of type T
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Method to setup data on plugin load
     */
    @Override
    public void onEnable() {
        plugin = this;
        taskChainFactory = BukkitTaskChainFactory.create(this);
        console = getLogger();

        if (checkPlugin("ProtocolLib")) {
            getServer().getPluginManager().registerEvents(new FakeBlockListener(), plugin);

            saveDefaultConfig();
            setupLanguageFile();

            manager = new PaperCommandManager(plugin);

            // Register @walls command completion
            manager.getCommandCompletions().registerAsyncCompletion("@walls", c -> {
                List<String> wallNames = new ArrayList<>();
                for (WallObject wallObject : WallObject.getWallObjectList()) {
                    wallNames.add(wallObject.getName());
                }
                return wallNames;
            });

            manager.registerDependency(Language.class, language);
            manager.registerCommand(new CommandHandler());

            boolean inversePermissionCheck = getConfig().getBoolean("inverse-permission-check", false);
            wallUtility = new WallUtility(inversePermissionCheck);

            // Load walls of child class
            ServiceLoader<FakeBlockModuleHandler> loader =
                    ServiceLoader.load(FakeBlockModuleHandler.class, plugin.getClassLoader());

            loader.forEach(handler -> {
                handler.loadWalls();
                handler.registerListeners();
                fakeBlockModuleHandler = handler;
            });

            // Register packet listener with ProtocolLib
            ProtocolLibHelper.addPacketListener();

            // LuckPerms hook
            if (checkPlugin("LuckPerms")) {
                LuckPermsHelper.setupLuckPermsHelper();
            } else {
                console.warning("LuckPerms not detected, FakeBlock will be unable to listen for permission node changes");
            }
        } else {
            console.warning("ProtocolLib not detected. Disabling!");
            setEnabled(false);
        }
    }

    /**
     * Method used for language file loading
     */
    private void setupLanguageFile() {
        File languageFile = new File(getDataFolder(), "language.yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        if (!languageFile.exists()) {
            languageConfig.options().header("- FakeBlock Language configuration -");
            languageConfig.set("prefix", "&5[FakeBlock]");
            languageConfig.set("no-permission", "&4You don't have permission to do that!");
            languageConfig.set("invalid-argument-length", "&4Invalid amount of arguments...");
            languageConfig.set("wall-deleted", "&4Wall has been deleted!");
            languageConfig.set("walls-reloaded", "&aWalls and language file reloaded");
            languageConfig.set("walls-selection", "&aGreat! Please use left and right click to select the bounds!");
            languageConfig.set("walls-selection-complete", "&aWall created, please refer to the configuration " +
                    "if you wish to make changes");
            languageConfig.set("walls-selection-location-saved", "&aLocation saved.");
            languageConfig.set("walls-toggled", "&aWalls have been toggled for specified player.");
            languageConfig.set("cant-find-player", "&4Cannot find that player!");

            try {
                languageConfig.save(languageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        language = new Language(this, languageConfig);
    }

    /**
     * Reload all configurations from file
     */
    public void reloadConfigs() {
        reloadConfig();
        setupLanguageFile();
    }

    /**
     * Checks to see if a plugin is installed on the server
     *
     * @param pluginName to check
     * @return true if exists, false if not
     */
    private boolean checkPlugin(String pluginName) {
        return getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    /**
     * Method to cleanup plugin data on server shutdown
     */
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        ProtocolLibHelper.closeSubscriptions();

        if (checkPlugin("LuckPerms")) {
            LuckPermsHelper.closeSubscriptions();
        }

        manager.unregisterCommands();
    }
}

package pro.husk.fakeblock;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pro.husk.fakeblock.commands.CommandHandler;
import pro.husk.fakeblock.commands.TabCompleteHandler;
import pro.husk.fakeblock.hooks.LuckPermsHelper;
import pro.husk.fakeblock.hooks.ProtocolLibHelper;
import pro.husk.fakeblock.listeners.FakeBlockListener;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallUtility;

import java.io.File;
import java.io.IOException;
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

    @Getter
    private static YamlConfiguration language;

    private static TaskChainFactory taskChainFactory;

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

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            getServer().getPluginManager().registerEvents(new FakeBlockListener(), plugin);

            PluginCommand pluginCommand = getCommand("fakeblock");

            if (pluginCommand != null) {
                pluginCommand.setExecutor(new CommandHandler());
                pluginCommand.setTabCompleter(new TabCompleteHandler());
            }

            saveDefaultConfig();
            setupLanguageFile();

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
            if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
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
        language = YamlConfiguration.loadConfiguration(languageFile);

        if (!languageFile.exists()) {
            language.options().header("- FakeBlock Language configuration -");
            language.set("prefix", "&5[FakeBlock]");
            language.set("no-permission", "&4You don't have permission to do that!");
            language.set("invalid-argument-length", "&4Invalid amount of arguments...");
            language.set("wall-deleted", "&4Wall has been deleted!");
            language.set("walls-reloaded", "&aWalls and language file reloaded");
            language.set("walls-selection", "&aGreat! Please use left and right click to select the bounds!");
            language.set("walls-selection-complete", "&aWall created, please refer to the configuration " +
                    "if you wish to make changes");
            language.set("walls-selection-location-saved", "&aLocation saved.");
            language.set("walls-toggled", "&aWalls have been toggled for specified player.");
            language.set("cant-find-player", "&4Cannot find that player!");

            try {
                language.save(languageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Language.loadValues();
    }

    /**
     * Reload all configurations from file
     */
    public void reloadConfigs() {
        reloadConfig();
        setupLanguageFile();
    }

    /**
     * Method to cleanup plugin data on server shutdown
     */
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        LuckPermsHelper.closeSubscriptions();
        ProtocolLibHelper.closeSubscriptions();
    }
}

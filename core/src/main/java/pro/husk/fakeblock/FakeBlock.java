package pro.husk.fakeblock;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.track.UserDemoteEvent;
import net.luckperms.api.event.user.track.UserPromoteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pro.husk.fakeblock.commands.CommandHandler;
import pro.husk.fakeblock.commands.TabCompleteHandler;
import pro.husk.fakeblock.hooks.LuckPermsHelper;
import pro.husk.fakeblock.listeners.FakeBlockListener;
import pro.husk.fakeblock.listeners.SelectionListener;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class FakeBlock extends JavaPlugin {

    // Plugin instance
    @Getter
    private static FakeBlock plugin;

    @Getter
    private static Logger console;

    @Getter
    private static FakeBlockModuleHandler fakeBlockModuleHandler;

    @Getter
    private static YamlConfiguration language;

    @Getter
    private static LuckPerms api;

    @Getter
    private static ProtocolManager protocolManager;

    private static final List<EventSubscription> subscriptions = new ArrayList<>();

    /**
     * Method to handle Plugin startup.
     */
    public void onEnable() {
        // Set up local objects variables
        plugin = this;
        console = getLogger();

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {

            // Register events
            getServer().getPluginManager().registerEvents(new FakeBlockListener(), plugin);
            getServer().getPluginManager().registerEvents(new SelectionListener(), plugin);

            // Register commands
            getCommand("fakeblock").setExecutor(new CommandHandler());
            getCommand("fakeblock").setTabCompleter(new TabCompleteHandler());

            // Create configs if not already created
            saveDefaultConfig();
            setupLanguageFile();

            // Load walls of child class
            ServiceLoader<FakeBlockModuleHandler> loader =
                    ServiceLoader.load(FakeBlockModuleHandler.class, plugin.getClassLoader());

            loader.forEach(handler -> {
                handler.loadWalls();
                fakeBlockModuleHandler = handler;
            });

            // Register packet listener with ProtocolLib
            addPacketListener();

            if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                if (provider != null) {
                    api = provider.getProvider();
                    subscriptions.add(api.getEventBus().subscribe(NodeAddEvent.class, LuckPermsHelper::onNodeAdd));
                    subscriptions.add(api.getEventBus().subscribe(NodeRemoveEvent.class, LuckPermsHelper::onNodeRemove));
                    subscriptions.add(api.getEventBus().subscribe(UserPromoteEvent.class, LuckPermsHelper::onUserPromote));
                    subscriptions.add(api.getEventBus().subscribe(UserDemoteEvent.class, LuckPermsHelper::onUserDemote));
                }
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
        String DATA_PATH = "plugins/FakeBlock/language.yml";
        boolean exists = (new File(DATA_PATH)).exists();
        language = YamlConfiguration.loadConfiguration(new File(DATA_PATH));

        if (!exists) {
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
            language.set("no-material-found", "&4No Material found with that name");
            language.set("wall-displaying-visualisation", "&aDisplaying a visualisation of what the wall will" +
                    " look like... In 5 seconds this will disappear!");
            language.set("walls-toggled", "&aWalls have been toggled for specified player.");
            language.set("cant-find-player", "&4Cannot find that player!");

            try {
                language.save(DATA_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Language.loadValues();
    }

    /**
     * Method to reload configuration + language file
     */
    public void reloadConfigs() {
        reloadConfig();
        setupLanguageFile();
    }

    /**
     * Method to handle Plugin shutdown.
     */
    public void onDisable() {
        subscriptions.forEach(EventSubscription::close);
    }

    /**
     * Utilises ProtocolLib to listen for USE_ITEM packet in order to prevent players destroying the fake wall
     */
    private void addPacketListener() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.HIGH,
                        PacketType.Play.Client.USE_ITEM) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        handlePacketEvent(event);
                    }
                });

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.HIGH,
                        PacketType.Play.Client.ARM_ANIMATION) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        handlePacketEvent(event);
                    }
                });
    }

    /**
     * Private method to handle PacketEvent
     *
     * @param event PacketEvent to handle
     */
    private void handlePacketEvent(PacketEvent event) {
        Player player = event.getPlayer();
        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM ||
                event.getPacketType() == PacketType.Play.Client.ARM_ANIMATION) {
            processWall(player, 2, false);
        }
    }

    /**
     * Current method for checking if a location is close to wall
     * Call me async
     *
     * @param playerLocation to check
     * @return null if not, wallObject if they are
     */
    public List<WallObject> isNearWall(Location playerLocation) {
        List<WallObject> nearby = new ArrayList<>();

        for (WallObject wallObject : WallObject.getWallObjectList()) {

            if (playerLocation.getWorld() != wallObject.getLocation1().getWorld()) break;

            for (Location location : wallObject.getBlocksInBetween()) {
                int playerDistanceToWall = (int) playerLocation.distanceSquared(location);
                int distanceToCheck = (int) (wallObject.getDistanceBetweenPoints() + 500) - playerDistanceToWall;

                if (playerDistanceToWall <= distanceToCheck && !nearby.contains(wallObject)) {
                    nearby.add(wallObject);
                }
            }
        }
        return nearby;
    }

    /**
     * Process check of player location near the wall async, and if they are close, send wall sync
     *
     * @param player to check
     * @param delay  on sending blocks
     */
    public void processWall(Player player, int delay, boolean ignorePermission) {
        CompletableFuture<List<WallObject>> future = CompletableFuture.supplyAsync(() ->
                isNearWall(player.getLocation()));

        future.thenAccept(walls -> walls.forEach(wall -> {
            if (ignorePermission) sendFakeBlocks(wall, player, delay);

            if (!player.hasPermission("fakeblock.admin") &&
                    !player.hasPermission("fakeblock." + wall.getName())) {
                sendFakeBlocks(wall, player, delay);
            }
        }));
    }

    /**
     * Little helper method to render wall to player on delay
     *
     * @param wall   to render
     * @param player to render wall for
     * @param delay  on rendering (used for logging in)
     */
    private void sendFakeBlocks(WallObject wall, Player player, int delay) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> wall.renderWall(player), delay * 20);
    }
}

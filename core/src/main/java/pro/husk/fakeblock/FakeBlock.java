package pro.husk.fakeblock;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pro.husk.fakeblock.commands.CommandHandler;
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
            language.set("walls-reloaded", "&aWalls reloaded");
            language.set("walls-selection", "&aGreat! Please use left and right click to select the bounds!");
            language.set("walls-selection-complete", "&aWall created, please refer to the configuration " +
                    "if you wish to make changes");
            language.set("walls-selection-located-saved", "&aLocation saved.");
            
            try {
                language.save(DATA_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Language.loadValues();
    }

    /**
     * Method to handle Plugin shutdown.
     */
    public void onDisable() {
    }

    /**
     * Utilises ProtocolLib to listen for USE_ITEM packet in order to prevent players destroying the fake wall
     */
    private void addPacketListener() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

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
            processWall(player, 2);
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
    public void processWall(Player player, int delay) {
        CompletableFuture<List<WallObject>> future = CompletableFuture.supplyAsync(() -> {
            return isNearWall(player.getLocation());
        });

        future.thenAccept(walls -> {
            walls.forEach(wall -> {
                if (!player.hasPermission("fakeblock.admin")) {
                    if (!player.hasPermission("fakeblock." + wall.getName())) {
                        sendFakeBlocks(wall, player, delay);
                    }
                }
            });
        });
    }

    /**
     * Little helper method to render wall to player on delay
     *
     * @param wall   to render
     * @param player to render wall for
     * @param delay  on rendering (used for logging in)
     */
    private void sendFakeBlocks(WallObject wall, Player player, int delay) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            wall.renderWall(player);
        }, delay * 20);
    }
}

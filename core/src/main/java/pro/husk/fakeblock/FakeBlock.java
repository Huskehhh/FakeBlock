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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pro.husk.fakeblock.commands.CommandHandler;
import pro.husk.fakeblock.listeners.FakeBlockListener;
import pro.husk.fakeblock.objects.WallObject;

import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class FakeBlock extends JavaPlugin {

    // Plugin instance
    @Getter
    private static FakeBlock plugin;

    @Getter
    private static Logger console;

    @Getter
    private static FakeBlockModuleHandler fakeBlockModuleHandler;

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

            // Register commands
            getCommand("fakeblock").setExecutor(new CommandHandler());

            // Create Config if not already created
            this.saveDefaultConfig();

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
            this.setEnabled(false);
        }
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

            processWall(player, 1);
        }
    }

    /**
     * Current method for checking if a location is close to wall
     * Call me async
     *
     * @param playerLocation to check
     * @return null if not, wallObject if they are
     */
    private WallObject isNearWall(Location playerLocation) {
        for (WallObject wallObject : WallObject.getWallObjectList()) {
            for (Location location : wallObject.getBlocksInBetween()) {
                if (location.getWorld() != location.getWorld()) return null;

                int playerDistanceToWall = (int) playerLocation.distanceSquared(location);

                int distanceToCheck = (int) (wallObject.getDistanceBetweenPoints() + 50) - playerDistanceToWall;

                if (playerDistanceToWall <= distanceToCheck) {
                    return wallObject;
                }
            }
        }
        return null;
    }

    /**
     * Process check of player location near the wall async, and if they are close, send wall sync
     *
     * @param player to check
     * @param delay  on sending blocks
     */
    public void processWall(Player player, int delay) {
        CompletableFuture<WallObject> future = CompletableFuture.supplyAsync(() -> {
            return isNearWall(player.getLocation());
        });

        future.thenRun(() -> {
            try {
                WallObject wall = future.get();

                if (wall != null) {
                    sendFakeBlocks(wall, player, delay);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
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

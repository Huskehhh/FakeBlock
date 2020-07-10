package pro.husk.fakeblock.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.track.UserDemoteEvent;
import net.luckperms.api.event.user.track.UserPromoteEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.objects.WallUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LuckPermsHelper {

    private static final List<EventSubscription<? extends LuckPermsEvent>> subscriptions = new ArrayList<>();

    public static void setupLuckPermsHelper() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            subscriptions.add(api.getEventBus().subscribe(NodeAddEvent.class, LuckPermsHelper::onNodeAdd));
            subscriptions.add(api.getEventBus().subscribe(NodeRemoveEvent.class, LuckPermsHelper::onNodeRemove));
            subscriptions.add(api.getEventBus().subscribe(UserPromoteEvent.class, LuckPermsHelper::onUserPromote));
            subscriptions.add(api.getEventBus().subscribe(UserDemoteEvent.class, LuckPermsHelper::onUserDemote));
        }
    }

    public static void closeSubscriptions() {
        subscriptions.forEach(EventSubscription::close);
    }

    /**
     * Listen to LuckPerms NodeAddEvent
     *
     * @param event NodeAddEvent
     */
    private static void onNodeAdd(NodeAddEvent event) {
        handleNodeEvents(event.getNode(), event.getTarget());
    }

    /**
     * Listen to LuckPerms NodeRemoveEvent
     *
     * @param event NodeRemoveEvent
     */
    private static void onNodeRemove(NodeRemoveEvent event) {
        handleNodeEvents(event.getNode(), event.getTarget());
    }

    /**
     * Listen to LuckPerms UserPromoteEvent
     *
     * @param event UserPromoteEvent
     */
    private static void onUserPromote(UserPromoteEvent event) {
        FakeBlock.getWallUtility().queueProcessAllPlayers();
    }

    /**
     * Listen to LuckPerms UserDemoteEvent
     *
     * @param event UserDemoteEvent
     */
    private static void onUserDemote(UserDemoteEvent event) {
        FakeBlock.getWallUtility().queueProcessAllPlayers();
    }

    /**
     * Little utility method to handle processing individual node changes
     *
     * @param node   that changed
     * @param holder target of the change
     */
    private static void handleNodeEvents(Node node, PermissionHolder holder) {
        FakeBlock plugin = FakeBlock.getPlugin();
        WallUtility utility = FakeBlock.getWallUtility();

        if (node.getType() == NodeType.PERMISSION &&
                node.getKey().contains("fakeblock.") &&
                holder.getIdentifier().getType().equals("user")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {

                UUID uuid = UUID.fromString(holder.getIdentifier().getName());
                Player player = plugin.getServer().getPlayer(uuid);

                if (player != null) {
                    if (node.hasExpired() || !node.getValue()) {
                        utility.processWall(player, 0, false);
                    } else {
                        utility.getNearbyFakeBlocks(player.getLocation()).thenAccept(wallObjects ->
                                wallObjects.forEach(wallObject -> wallObject.sendRealBlocks(player)));
                    }
                }
            });
        }
    }
}

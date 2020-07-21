package pro.husk.fakeblock.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.track.UserDemoteEvent;
import net.luckperms.api.event.user.track.UserPromoteEvent;
import net.luckperms.api.node.Node;
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
        handleNodeEvents(event.getNode());
    }

    /**
     * Listen to LuckPerms NodeRemoveEvent
     *
     * @param event NodeRemoveEvent
     */
    private static void onNodeRemove(NodeRemoveEvent event) {
        handleNodeEvents(event.getNode());
    }

    /**
     * Listen to LuckPerms UserPromoteEvent
     *
     * @param event UserPromoteEvent
     */
    private static void onUserPromote(UserPromoteEvent event) {
        handlePromoteAndDemote(event.getUser().getUniqueId());
    }

    /**
     * Listen to LuckPerms UserDemoteEvent
     *
     * @param event UserDemoteEvent
     */
    private static void onUserDemote(UserDemoteEvent event) {
        handlePromoteAndDemote(event.getUser().getUniqueId());
    }

    /**
     * Little utility method to handle processing individual node changes
     *
     * @param node that changed
     */
    private static void handleNodeEvents(Node node) {
        WallUtility utility = FakeBlock.getWallUtility();
        if (node.getKey().contains("fakeblock.") &&
                !node.getKey().equalsIgnoreCase("fakeblock.admin")) {
            Bukkit.getOnlinePlayers().forEach(utility::processWallConditions);
        }
    }

    private static void handlePromoteAndDemote(UUID uuid) {
        WallUtility utility = FakeBlock.getWallUtility();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            utility.processWallConditions(player);
        }
    }
}

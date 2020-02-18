package pro.husk.fakeblock.hooks;

import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.track.UserDemoteEvent;
import net.luckperms.api.event.user.track.UserPromoteEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;

import java.util.UUID;

public class LuckPermsHelper {

    /**
     * Listen to LuckPerms NodeAddEvent
     *
     * @param event NodeAddEvent
     */
    public static void onNodeAdd(NodeAddEvent event) {
        handleNodeEvents(event.getNode(), event.getTarget());
    }

    /**
     * Listen to LuckPerms NodeRemoveEvent
     *
     * @param event NodeRemoveEvent
     */
    public static void onNodeRemove(NodeRemoveEvent event) {
        handleNodeEvents(event.getNode(), event.getTarget());
    }

    /**
     * Listen to LuckPerms UserPromoteEvent
     *
     * @param event UserPromoteEvent
     */
    public static void onUserPromote(UserPromoteEvent event) {
        queueWallToAll();
    }

    /**
     * Listen to LuckPerms UserDemoteEvent
     *
     * @param event UserDemoteEvent
     */
    public static void onUserDemote(UserDemoteEvent event) {
        queueWallToAll();
    }

    /**
     * Little utility method to handle processing individual node changes
     *
     * @param node   that changed
     * @param holder target of the change
     */
    private static void handleNodeEvents(Node node, PermissionHolder holder) {
        FakeBlock plugin = FakeBlock.getPlugin();

        if (node.getType() == NodeType.PERMISSION && node.getKey().contains("fakeblock.")) {
            if (holder.getIdentifier().getType().equals("user")) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {

                    UUID uuid = UUID.fromString(holder.getIdentifier().getName());
                    Player player = plugin.getServer().getPlayer(uuid);

                    if (player != null) {
                        if (node.hasExpired() || !node.getValue()) {
                            plugin.processWall(player, 0, false);
                        } else {
                            plugin.isNearWall(player.getLocation()).forEach(wallObject -> wallObject.sendRealBlocks(player));
                        }
                    }
                });
            }
        }
    }

    /**
     * Little utility helper method to queue all players to be re-checked for permission
     */
    private static void queueWallToAll() {
        FakeBlock plugin = FakeBlock.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getOnlinePlayers().forEach(player -> plugin.processWall(player, 0, false)));
    }
}

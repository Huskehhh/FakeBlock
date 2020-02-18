package pro.husk.fakeblock.hooks;

import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;

import java.util.UUID;

public class LuckPermsHook {

    /**
     * Listener for NodeAddEvent from LuckPerms to allow instant updating of the wall if
     * the user is given the correct permission
     *
     * @param event to process
     */
    public static void onNodeChange(NodeAddEvent event) {
        Node node = event.getNode();
        PermissionHolder holder = event.getTarget();

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
}

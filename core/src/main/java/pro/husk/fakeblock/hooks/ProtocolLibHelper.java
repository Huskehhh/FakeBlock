package pro.husk.fakeblock.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import pro.husk.fakeblock.FakeBlock;

public class ProtocolLibHelper {

    @Getter
    private static ProtocolManager protocolManager;

    /**
     * Utilises ProtocolLib to listen for certain packets in order to prevent players circumventing the wall
     */
    public static void addPacketListener() {
        FakeBlock plugin = FakeBlock.getPlugin();
        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH,
                        PacketType.Play.Client.USE_ITEM) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        handlePacketEvent(event);
                    }
                });
        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH,
                        PacketType.Play.Client.ARM_ANIMATION) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        handlePacketEvent(event);
                    }
                });
        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH,
                        PacketType.Play.Server.MAP_CHUNK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        handlePacketEvent(event);
                    }
                });
    }

    private static void handlePacketEvent(PacketEvent event) {
        FakeBlock.getWallUtility().processWall(event.getPlayer(), 0, false);
    }

    public static void closeSubscriptions() {
        protocolManager.removePacketListeners(FakeBlock.getPlugin());
    }
}

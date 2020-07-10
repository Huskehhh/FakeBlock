package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import pro.husk.fakeblock.FakeBlock;

import java.util.HashMap;

public class Config {

    @Getter
    private static final HashMap<String, Config> currentConfigurations = new HashMap<>();

    @Getter
    private final String wallName;
    @Getter
    private final String playerName;
    @Getter
    @Setter
    private Location location1;
    @Getter
    @Setter
    private Location location2;

    public Config(String playerName, String wallName) {
        this.playerName = playerName;
        this.wallName = wallName;

        currentConfigurations.put(playerName, this);
    }

    public static boolean isSelecting(String playerName) {
        return currentConfigurations.containsKey(playerName);
    }

    public WallObject createWallObject() {
        return FakeBlock.getFakeBlockModuleHandler().loadWall(this);
    }

    public void remove() {
        currentConfigurations.remove(playerName);
    }
}

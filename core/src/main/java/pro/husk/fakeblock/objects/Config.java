package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import pro.husk.fakeblock.FakeBlock;

import java.util.HashMap;

public class Config {

    @Getter
    private static HashMap<String, Config> currentConfigurations = new HashMap<>();

    // BOTH
    @Getter
    String wallName;

    @Getter
    @Setter
    Location location1;

    @Getter
    @Setter
    Location location2;

    @Getter
    String playerName;

    // LATEST
    @Getter
    @Setter
    Material material;

    // LEGACY
    @Getter
    @Setter
    int id;

    @Getter
    @Setter
    int data;

    public Config(String playerName, String wallName) {
        this.playerName = playerName;
        this.wallName = wallName;

        currentConfigurations.put(playerName, this);
    }

    public static boolean isSelecting(String playerName) {
        return getCurrentConfigurations().containsKey(playerName);
    }

    public WallObject createWallObject() {
        return FakeBlock.getFakeBlockModuleHandler().loadWall(this);
    }

    public void remove() {
        getCurrentConfigurations().remove(playerName);
    }
}

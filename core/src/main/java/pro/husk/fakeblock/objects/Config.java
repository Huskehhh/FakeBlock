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
    private String wallName;

    @Getter
    @Setter
    private Location location1;

    @Getter
    @Setter
    private Location location2;

    @Getter
    private String playerName;

    // LATEST
    @Getter
    @Setter
    private Material material;

    // LEGACY
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private int data;

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

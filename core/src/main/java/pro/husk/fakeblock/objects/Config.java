package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import pro.husk.fakeblock.FakeBlock;

import java.util.HashMap;
import java.util.UUID;

/**
 * Configuration object of a wall's state, storing the locations selected by a user
 * Preparing the data to be converted to a WallObject
 */
public class Config {

    @Getter
    private static final HashMap<UUID, Config> currentConfigurations = new HashMap<>();

    @Getter
    private final String wallName;

    @Getter
    private final UUID uuid;

    @Getter
    @Setter
    private Location location1;

    @Getter
    @Setter
    private Location location2;

    /**
     * Constructor for a Config object
     *
     * @param uuid     of the player that is selecting
     * @param wallName of the wall they want to create
     */
    public Config(UUID uuid, String wallName) {
        this.uuid = uuid;
        this.wallName = wallName;

        currentConfigurations.put(uuid, this);
    }

    /**
     * Method to check if a given player is selecting
     *
     * @param uuid to check
     * @return whether or not this player is selecting
     */
    public static boolean isSelecting(UUID uuid) {
        return currentConfigurations.containsKey(uuid);
    }

    /**
     * Method to convert the current Config object into a WallObject
     *
     * @return WallObject implementation for the current version
     */
    public WallObject createWallObject() {
        return FakeBlock.getFakeBlockModuleHandler().loadWall(this);
    }

    /**
     * Method to remove a Config
     */
    public void remove() {
        currentConfigurations.remove(uuid);
    }
}

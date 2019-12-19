package pro.husk.fakeblock.objects;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import pro.husk.fakeblock.FakeBlock;

public class Language {

    @Getter
    private static String prefix;

    @Getter
    private static String noPermission;

    @Getter
    private static String wrongArgumentLength;

    @Getter
    private static String wallsReloaded;
    
    @Getter
    private static String wallDeleted;

    @Getter
    private static String wallsSelection;

    @Getter
    private static String wallsSelectionComplete;

    @Getter
    private static String locationSaved;

    /**
     * Method used to load all values into memory to minimise I/O
     */
    public static void loadValues() {
        YamlConfiguration language = FakeBlock.getLanguage();

        prefix = colourise(language.getString("prefix"));
        noPermission = colourise(language.getString("no-permission"));
        wrongArgumentLength = colourise(language.getString("wrong-argument-length"));
        wallDeleted = colourise(language.getString("wall-deleted"));
        wallsReloaded = colourise(language.getString("walls-reloaded"));
        wallsSelection = colourise(language.getString("walls-selection"));
        wallsSelectionComplete = colourise(language.getString("walls-selection-complete"));
        locationSaved = colourise(language.getString("walls-selection-location-saved"));
    }

    /**
     * Helper for colourising chat colour
     *
     * @param input | String to process colour for
     * @return colourised String
     */
    private static String colourise(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}

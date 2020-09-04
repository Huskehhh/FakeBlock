package pro.husk.fakeblock.objects;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
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

    @Getter
    private static String wallsToggled;

    @Getter
    private static String cantFindPlayer;

    /**
     * Method used to load all values into memory to minimise I/O
     */
    public static void loadValues() {
        YamlConfiguration language = FakeBlock.getLanguage();

        prefix = colourise(language.getString("prefix"));
        noPermission = colourise(language.getString("no-permission"));
        wrongArgumentLength = colourise(language.getString("invalid-argument-length"));
        wallDeleted = colourise(language.getString("wall-deleted"));
        wallsReloaded = colourise(language.getString("walls-reloaded"));
        wallsSelection = colourise(language.getString("walls-selection"));
        wallsSelectionComplete = colourise(language.getString("walls-selection-complete"));
        locationSaved = colourise(language.getString("walls-selection-location-saved"));
        wallsToggled = colourise(language.getString("walls-toggled"));
        cantFindPlayer = colourise(language.getString("cant-find-player"));
    }

    /**
     * Helper method to colourise a string with a combination of both hex and legacy chat colours
     *
     * @param input string to colourise
     * @return colourised string
     */
    public static String colourise(String input) {
        while (input.contains("#")) {
            int index = input.indexOf("#");
            if (index != 0 && input.charAt(index - 1) == '&') {
                String hexSubstring = input.substring(index - 1, index + 7).replaceAll("&", "");

                try {
                    ChatColor transformed = ChatColor.of(hexSubstring);
                    // Apply transformation to original string
                    input = input.replaceAll("&" + hexSubstring, transformed + "");
                } catch (IllegalArgumentException ignored) {

                }
            } else {
                break;
            }
        }

        // Apply legacy transformations at end
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}

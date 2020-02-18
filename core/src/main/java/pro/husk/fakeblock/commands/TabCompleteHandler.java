package pro.husk.fakeblock.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import pro.husk.fakeblock.objects.WallObject;

import java.util.ArrayList;
import java.util.List;

public class TabCompleteHandler implements TabCompleter {

    /**
     * Method to handle tab completion
     *
     * @param commandSender - Command Sender
     * @param command       - Command sent
     * @param commandLabel  - Command sent converted to String
     * @param arguments     - Arguments of the Command
     * @return list of tab complete responses
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String commandLabel, String[] arguments) {

        List<String> tabComplete = new ArrayList<>();

        if (arguments.length == 1) {
            tabComplete.add("create");
            tabComplete.add("delete");
            tabComplete.add("reload");
            tabComplete.add("list");
            tabComplete.add("toggle");
        }

        if (arguments[0].equalsIgnoreCase("create") && arguments.length == 3) {
            for (Material material : Material.values()) {
                if (material.toString().contains(arguments[2].toUpperCase())) {
                    tabComplete.add(material.toString());
                }
            }
        }

        if (arguments[0].equalsIgnoreCase("delete") && arguments.length == 2) {
            WallObject.getWallObjectList().forEach(wallObject -> tabComplete.add(wallObject.getName()));
        }

        return tabComplete;
    }
}

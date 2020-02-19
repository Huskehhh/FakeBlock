package pro.husk.fakeblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    private static List<UUID> toggledPlayers = new ArrayList<>();

    /**
     * Command handler
     *
     * @param commandSender - Command Sender
     * @param command       - Command sent
     * @param commandLabel  - Command sent converted to String
     * @param arguments     - Arguments of the Command
     * @return whether or not the command worked
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] arguments) {
        String fakeBlockTitle = Language.getPrefix();

        if (commandSender.hasPermission("fakeblock.admin")) {
            if (arguments.length == 0) {
                help(commandSender, command, fakeBlockTitle);
            } else {
                if (arguments[0].equalsIgnoreCase("delete")) {
                    if (arguments.length == 2) {
                        delete(commandSender, arguments, fakeBlockTitle);
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                } else if (arguments[0].equalsIgnoreCase("reload")) {
                    if (arguments.length == 1) {
                        reload(commandSender, fakeBlockTitle);
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                } else if (arguments[0].equalsIgnoreCase("create")) {
                    if (arguments.length == 3) {
                        create(commandSender, arguments, fakeBlockTitle);
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                } else if (arguments[0].equalsIgnoreCase("list")) {
                    if (arguments.length == 1) {
                        list(commandSender, fakeBlockTitle);
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                } else if (arguments[0].equalsIgnoreCase("toggle")) {
                    if (arguments.length == 2) {
                        toggle(commandSender, arguments, fakeBlockTitle);
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                }
            }
        } else {
            commandSender.sendMessage(fakeBlockTitle + " " + Language.getNoPermission());
        }
        return true;
    }

    /**
     * Private helper method to handle the default help messages
     * @param commandSender sender
     * @param command command
     * @param fakeBlockTitle prefix
     */
    private void help(CommandSender commandSender, Command command, String fakeBlockTitle) {
        commandSender.sendMessage(ChatColor.GREEN + " --------- " + ChatColor.AQUA + fakeBlockTitle + ChatColor.GREEN + " Help --------- ");
        commandSender.sendMessage(ChatColor.GREEN + "/" + command.getLabel() + " | Aliases: /fakeblock, /fb");
        commandSender.sendMessage(ChatColor.GREEN + "/" + command.getLabel() + " create <wall name> <material name> | Creates a wall under specified name with given material");
        commandSender.sendMessage(ChatColor.GREEN + "/" + command.getLabel() + " delete <wall name> | Deletes wall");
        commandSender.sendMessage(ChatColor.GREEN + "/" + command.getLabel() + " reload | Reloads the walls from config");
        commandSender.sendMessage(ChatColor.GREEN + "/" + command.getLabel() + " list | Lists all walls");
        commandSender.sendMessage(ChatColor.GREEN + "/" + command.getLabel() + " toggle <player> | Shows all nearby walls to a player");
        commandSender.sendMessage(ChatColor.GREEN + "------------------------------------");
    }

    /**
     * Private helper method to handle the delete command
     * @param commandSender sender
     * @param arguments arguments
     * @param fakeBlockTitle prefix
     */
    private void delete(CommandSender commandSender, String[] arguments, String fakeBlockTitle) {
        WallObject wallObject = WallObject.getByName(arguments[1]);
        if (wallObject != null) {
            wallObject.delete();
            commandSender.sendMessage(fakeBlockTitle + " " + Language.getWallDeleted());
        }
    }

    /**
     * Private helper method to handle the reload command
     * @param commandSender sender
     * @param fakeBlockTitle prefix
     */
    private void reload(CommandSender commandSender, String fakeBlockTitle) {
        FakeBlock.getPlugin().reloadConfigs();
        WallObject.getWallObjectList().forEach(WallObject::loadWall);
        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWallsReloaded());
    }

    /**
     * Private helper method to handle the create command
     * @param commandSender sender
     * @param arguments arguments
     * @param fakeBlockTitle prefix
     */
    private void create(CommandSender commandSender, String[] arguments, String fakeBlockTitle) {
        String wallName = arguments[1];
        String materialArgument = arguments[2].toUpperCase();
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            Config config = new Config(player.getName(), wallName);
            Material material = Material.getMaterial(materialArgument);

            if (material != null) {
                config.setMaterial(material);
                player.sendMessage(fakeBlockTitle + " " + Language.getWallsSelection());
            } else {
                player.sendMessage(fakeBlockTitle + " " + Language.getNoMaterialFound());
            }
        }
    }

    /**
     * Private helper method to handle the list command
     * @param commandSender sender
     * @param fakeBlockTitle prefix
     */
    private void list(CommandSender commandSender, String fakeBlockTitle) {
        commandSender.sendMessage(fakeBlockTitle + ChatColor.GOLD + " Walls");
        WallObject.getWallObjectList().forEach(wallObject ->
                commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + wallObject.getName()));
    }

    /**
     * Private helper method to handle the toggle command
     * @param commandSender sender
     * @param arguments arguments
     * @param fakeBlockTitle prefix
     */
    private void toggle(CommandSender commandSender, String[] arguments, String fakeBlockTitle) {
        Player target = null;

        if (commandSender instanceof Player) {
            target = (Player) commandSender;
        }

        if (arguments.length == 2) {
            String targetName = arguments[1];
            target = Bukkit.getPlayer(targetName);
        }

        if (target != null) {
            FakeBlock.getPlugin().processWall(target, 1, toggledPlayers.contains(target.getUniqueId()));

            if (toggledPlayers.contains(target.getUniqueId())) {
                toggledPlayers.remove(target.getUniqueId());
            } else {
                toggledPlayers.add(target.getUniqueId());
            }

            commandSender.sendMessage(fakeBlockTitle + " " + Language.getWallsToggled());
        } else {
            commandSender.sendMessage(fakeBlockTitle + " " + Language.getCantFindPlayer());
        }
    }
}

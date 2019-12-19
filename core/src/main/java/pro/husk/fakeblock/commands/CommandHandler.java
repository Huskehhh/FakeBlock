package pro.husk.fakeblock.commands;

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

public class CommandHandler implements CommandExecutor {

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

                commandSender.sendMessage(ChatColor.GREEN + " --------- " + ChatColor.AQUA + fakeBlockTitle + ChatColor.GREEN + " Help --------- ");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + command.getLabel() + " | Aliases: /fakeblock, /fb");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + command.getLabel() + " create <wall name> <material name> | Creates a wall under specified name with given material");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + command.getLabel() + " delete <wall name> | Deletes wall");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + command.getLabel() + " reload | Reloads the walls from config");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + command.getLabel() + " list | Lists all walls");
                commandSender.sendMessage(ChatColor.GREEN + "------------------------------------");

            } else {
                if (arguments[0].equalsIgnoreCase("delete")) {
                    if (arguments.length == 2) {
                        WallObject wallObject = WallObject.getByName(arguments[1]);
                        if (wallObject != null) {
                            wallObject.delete();
                            commandSender.sendMessage(fakeBlockTitle + " " + Language.getWallDeleted());
                        }
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                } else if (arguments[0].equalsIgnoreCase("reload")) {
                    if (arguments.length == 1) {

                        FakeBlock.getPlugin().reloadConfig();

                        for (WallObject wallObject : WallObject.getWallObjectList()) {
                            wallObject.loadWall();
                        }

                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWallsReloaded());
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + " " + Language.getWrongArgumentLength());
                    }
                } else if (arguments[0].equalsIgnoreCase("create")) {
                    if (arguments.length == 3) {
                        String wallName = arguments[1];
                        String materialArgument = arguments[2];
                        if (commandSender instanceof Player) {
                            Player player = (Player) commandSender;

                            Config config = new Config(player.getName(), wallName);

                            Material material = Material.getMaterial(materialArgument);

                            if (material != null) {
                                config.setMaterial(material);

                                player.sendMessage(fakeBlockTitle + " " + Language.getWallsSelection());
                            }
                        }
                    }
                } else if (arguments[0].equalsIgnoreCase("list")) {
                    if (arguments.length == 1) {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.GOLD + " Walls");
                        for (WallObject wallObject : WallObject.getWallObjectList()) {
                            commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + wallObject.getName());
                        }
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
}

package pro.husk.fakeblock.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.WallObject;

public class CommandHandler implements CommandExecutor {

    //todo create wall thru command

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

        String fakeBlockTitle = ChatColor.BLACK + "[" + ChatColor.AQUA + "FakeBlock" + ChatColor.BLACK + "] ";
        commandLabel = commandLabel.toLowerCase();

        if (commandSender.hasPermission("fakeblock.admin")) {
            if (arguments.length == 0) {

                commandSender.sendMessage(ChatColor.GREEN + " --------- " + ChatColor.AQUA + "FakeBlock" + ChatColor.GREEN + " Help --------- ");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " | Aliases: /fakeblock, /fb");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " create <wall name> | Creates a wall under specified name");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " delete <wall name> | Deletes wall");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " reload | Reloads the walls from config");
                commandSender.sendMessage(ChatColor.GREEN + "------------------------------------");

            } else {
                if (arguments[0].equalsIgnoreCase("delete")) {
                    if (arguments.length == 2) {
                        WallObject wallObject = WallObject.getByName(arguments[1]);
                        if (wallObject != null) {
                            wallObject.delete();
                            commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "'" + arguments[1] + "' has been deleted");
                        }
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock delete <wall name>");
                    }
                } else if (arguments[0].equalsIgnoreCase("reload")) {
                    if (arguments.length == 1) {

                        FakeBlock.getPlugin().reloadConfig();

                        for (WallObject wallObject : WallObject.getWallObjectList()) {
                            wallObject.loadWall();
                        }

                        commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "Walls reloaded!");
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock reload");
                    }
                } else if (arguments[0].equalsIgnoreCase("create")) {
                    if (arguments.length == 2) {
                        String wallName = arguments[1];
                        if (commandSender instanceof Player) {
                            Player player = (Player) commandSender;

                            Config config = new Config(player.getName(), wallName);

                            config.setMaterial(Material.TNT);
                            config.setLocation1(player.getLocation());
                            config.setLocation2(player.getLocation());

                            WallObject wallObject = config.createWallObject();

                            wallObject.saveWall();

                            player.sendMessage(fakeBlockTitle + ChatColor.GREEN + "Wall '" + wallName + "' created. " +
                                    "Please refer to the configuration to edit it");
                        }
                    }
                } else if (arguments[0].equalsIgnoreCase("list")) {
                    if (arguments.length == 1) {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "Walls |");
                        for (WallObject wallObject : WallObject.getWallObjectList()) {
                            commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + wallObject.getName());
                        }
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock list");
                    }
                }
            }
        } else {
            commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "You don't have permission to do that!");
        }
        return true;
    }
}

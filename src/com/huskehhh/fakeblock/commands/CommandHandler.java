package com.huskehhh.fakeblock.commands;

import com.huskehhh.fakeblock.FakeBlock;
import com.huskehhh.fakeblock.listeners.FakeBlockListener;
import com.huskehhh.fakeblock.objects.Config;
import com.huskehhh.fakeblock.objects.Wall;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ListIterator;

public class CommandHandler implements CommandExecutor {

    private static FakeBlock plugin;
    private static FakeBlockListener listener;

    /**
     * Constructor
     *
     * @param plugin
     * @param listener
     */

    public CommandHandler(FakeBlock plugin, FakeBlockListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }


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

                commandSender.sendMessage(ChatColor.GREEN + " --------- FakeBlock Help --------- ");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " | Aliases: /fakeblock, /fb");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " set <wall name> <block name> | Creates a wall under specified name using specified block");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " delete <wall name> | Deletes wall");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " replace <wall name> <block name> | Changes material of wall to specified material");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " reload | Reloads plugin");
                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "/" + commandLabel + " list | Lists all fake walls!");
                commandSender.sendMessage(ChatColor.GREEN + "------------------------------------");

            } else {
                if (arguments[0].equalsIgnoreCase("set")) {
                    if (commandSender instanceof Player) {
                        Player player = (Player) commandSender;
                        if (arguments.length == 3) {
                            if (Material.matchMaterial(arguments[2]) != null) {
                                listener.map.put(player.getName(), arguments[1]);
                                listener.selecting.add(player.getName());

                                Config conf = new Config();

                                conf.setName(arguments[1]);
                                conf.setBlockname(Material.matchMaterial(arguments[2]).toString());
                                listener.configObj.put(player.getName(), conf);

                                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "You can now select the blocks you want.");
                            } else {
                                commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Unknown block name!");
                            }
                        } else {
                            commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock set <wall name> <block name>");
                        }
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "You must be a player to do that!");
                    }
                } else if (arguments[0].equalsIgnoreCase("delete")) {
                    if (arguments.length == 2) {
                        if (Wall.getByName(arguments[1]) != null) {
                            Wall.removeByName(arguments[1]);

                            plugin.forceConfigRefresh();

                            Wall.unloadWalls();
                            Wall.loadWalls();

                            commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "'" + arguments[1] + "' has been deleted");
                        }
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock delete <wall name>");
                    }
                } else if (arguments[0].equalsIgnoreCase("replace")) {
                    if (arguments.length == 3) {
                        if (Wall.getByName(arguments[1]) != null) {
                            if (Material.matchMaterial(arguments[2]) != null) {
                                Wall wall = Wall.getByName(arguments[1]);
                                Material replacementMaterial = Material.matchMaterial(arguments[2]);

                                wall.setBlockName(replacementMaterial.toString());

                                plugin.forceConfigRefresh();

                                commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "'" + arguments[1] + "'s block has been replaced with " + arguments[2]);
                            } else {
                                commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Unknown block name!");
                            }
                        } else {
                            commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid wall name!");
                        }
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock replace <wall name> <block name>");
                    }
                } else if (arguments[0].equalsIgnoreCase("reload")) {
                    if (arguments.length == 1) {
                        Wall.unloadWalls();

                        plugin.forceConfigRefresh();

                        Wall.loadWalls();

                        plugin.sendFakeBlocks(1);

                        commandSender.sendMessage(fakeBlockTitle + ChatColor.GREEN + "Walls reloaded!");
                    } else {
                        commandSender.sendMessage(fakeBlockTitle + ChatColor.RED + "Invalid amount of arguments! Usage: /fakeblock reload");
                    }
                } else if (arguments[0].equalsIgnoreCase("list")) {
                    if (arguments.length == 1) {
                        ListIterator<String> wallListIterator = Wall.getAllWalls().listIterator();

                        commandSender.sendMessage(ChatColor.GREEN + " ---- FakeBlock Wall list ---- ");
                        while (wallListIterator.hasNext()) {
                            commandSender.sendMessage(ChatColor.GREEN + " ---- " + ChatColor.LIGHT_PURPLE + wallListIterator.next() + ChatColor.GREEN + " ---- ");
                        }
                        commandSender.sendMessage(ChatColor.GREEN + "---------------------------------");
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

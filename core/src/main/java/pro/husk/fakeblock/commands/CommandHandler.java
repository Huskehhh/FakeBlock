package pro.husk.fakeblock.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.objects.Config;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;

import java.util.HashSet;
import java.util.UUID;

/**
 * Primary command handler for the FakeBlock command
 */
@CommandAlias("fakeblock|fb")
@Description("FakeBlock-related commands")
public class CommandHandler extends BaseCommand {

    @Dependency
    private Language language;

    private static final HashSet<UUID> toggledPlayers = new HashSet<>();

    @Default
    @CommandPermission("fakeblock.admin")
    public void help(CommandSender commandSender) {
        commandSender.sendMessage(Language.colourise(ChatColor.GREEN + " --------- " + ChatColor.AQUA + language.getPrefix() + ChatColor.GREEN + " Help --------- "));
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock | Aliases: /fakeblock, /fb");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock create <wall name> <material name> | Creates a wall under specified name with given material");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock delete <wall name> | Deletes wall");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock reload | Reloads the walls from config");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock list | Lists all walls");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock toggle <player> | Shows all nearby walls to a player");
        commandSender.sendMessage(ChatColor.GREEN + "------------------------------------");
    }

    @Subcommand("delete")
    @CommandPermission("fakeblock.admin")
    @CommandCompletion("@walls")
    public void delete(CommandSender commandSender, String wallName) {
        WallObject wallObject = WallObject.getByName(wallName);
        if (wallObject != null) {
            wallObject.delete();
            commandSender.sendMessage(Language.colourise(language.getPrefix() + " " + language.getWallDeleted()));
        }
    }

    @Subcommand("reload")
    @CommandPermission("fakeblock.admin")
    public void reload(CommandSender commandSender) {
        FakeBlock.getPlugin().reloadConfigs();
        WallObject.getWallObjectList().forEach(WallObject::loadWall);
        commandSender.sendMessage(Language.colourise(language.getPrefix() + " " + language.getWallsReloaded()));
    }

    @Subcommand("create")
    @CommandPermission("fakeblock.admin")
    public void create(Player player, String wallName) {
        new Config(player.getUniqueId(), wallName);
        player.sendMessage(Language.colourise(language.getPrefix() + " " + language.getWallsSelection()));
    }

    @Subcommand("list")
    @CommandPermission("fakeblock.admin")
    public void list(CommandSender commandSender) {
        commandSender.sendMessage(Language.colourise(language.getPrefix() + ChatColor.GOLD + " Walls"));
        WallObject.getWallObjectList().forEach(wallObject ->
                commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + wallObject.getName()));
    }

    @Subcommand("toggle")
    @CommandPermission("fakeblock.admin")
    @CommandCompletion("@players")
    public void toggle(CommandSender commandSender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            boolean isToggled = toggledPlayers.contains(target.getUniqueId());
            FakeBlock.getWallUtility().processWall(target, 1, isToggled);

            if (isToggled) {
                toggledPlayers.remove(target.getUniqueId());
            } else {
                toggledPlayers.add(target.getUniqueId());
            }

            commandSender.sendMessage(Language.colourise(language.getPrefix() + " " + language.getWallsToggled()));
        } else {
            commandSender.sendMessage(Language.colourise(language.getPrefix() + " " + language.getCantFindPlayer()));
        }
    }
}

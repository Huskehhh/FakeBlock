package code.husky;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeBlock extends JavaPlugin implements Listener {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

	List<String> right = new ArrayList<String>();
	List<String> selecting = new ArrayList<String>();
	boolean fakewall = false;
	FakeBlockAPI api = new FakeBlockAPI();

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new FakeBlockListener(), this);
		getServer().getPluginManager().registerEvents(this, this);
		createConfig();
	}

	private void createConfig() {
		boolean exists = new File("plugins/FakeBlock/config.yml").exists();
		if (!exists) {
			new File("plugins/FakeBlock").mkdir();
			config.options().header("FakeBlock, made by Husky!");
			config.set("#", "ID of the FakeBlock viewed to players without permission.");
			config.set("FakeBlock-ID", 1);
			config.set("Data.FakeWall.bounds.x-start", 0);
			config.set("Data.FakeWall.bounds.y-start", 0);
			config.set("Data.FakeWall.bounds.z-start", 0);
			config.set("Data.FakeWall.bounds.x-end", 0);
			config.set("Data.FakeWall.bounds.y-end", 0);
			config.set("Data.FakeWall.bounds.z-end", 0);
			try {
				config.save("plugins/FakeBlock/config.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(commandLabel.equalsIgnoreCase("fakeblock") || commandLabel.equalsIgnoreCase("fb")) {
			if(sender == getServer().getConsoleSender()) {
				sender.sendMessage(ChatColor.RED + "Only players can use these commands.");
			} else {
				if(args.length > 0 && args.length < 2) {
					Player p = (Player) sender;
					String para = args[0];
					if (para.equals("set")) {
						selecting.add(p.getName());
						p.sendMessage(ChatColor.GREEN + "[FakeBlock] You can now select the blocks you want..");
					}
				}
				return true;
			}
		}
		return false;
	}

	public boolean wallExists() {
		return config.getInt("Data.FakeWall.bounds.x-start") != 0;
	}

	@EventHandler
	public void interact(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(p.hasPermission("fakeblock.admin")) {
			if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(selecting.contains(p.getName()) && !right.contains(p.getName())) {
					Location l = b.getLocation();
					int lx = l.getBlockX();
					int ly = l.getBlockY();
					int lz = l.getBlockZ();
					config.set("Data.FakeWall.bounds.x-start", lx);
					config.set("Data.FakeWall.bounds.y-start", ly);
					config.set("Data.FakeWall.bounds.z-start", lz);
					try {
						config.save("plugins/FakeBlock/config.yml");
						right.add(p.getName());
						selecting.remove(p.getName());
						p.sendMessage(ChatColor.GREEN + "[FakeBlock] Great! Now Please Right-Click and select the second point!");
						e.setCancelled(true);
					} catch (IOException eeee) {
						eeee.printStackTrace();
					}
				}
			} else if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(!selecting.contains(p.getName()) && right.contains(p.getName())) {
					if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
						Location rl = b.getLocation();
						int rx = rl.getBlockX();
						int ry = rl.getBlockY();
						int rz = rl.getBlockZ();
						config.set("Data.FakeWall.bounds.x-end", rx);
						config.set("Data.FakeWall.bounds.y-end", ry);
						config.set("Data.FakeWall.bounds.z-end", rz);
						try {
							config.save("plugins/FakeBlock/config.yml");
							p.sendMessage(ChatColor.GREEN + "[FakeBlock] Great! Creating the fake wall now!");
						} catch (IOException eeee) {
							eeee.printStackTrace();
						}
						right.remove(p.getName());
						api.sendFakeBlocks(p);
					}
				}
			}
		}
	}
}

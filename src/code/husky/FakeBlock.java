package code.husky;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeBlock extends JavaPlugin {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

	List<String> selecting = new ArrayList<String>();
	boolean fakewall = false;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new FakeBlockListener(), this);
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
			config.set("selecting", new ArrayList<String>());
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
						addToList(p.getName());
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

	public void addToList(String name) {
		config.getStringList("selecting").add(name);
	}
	
}

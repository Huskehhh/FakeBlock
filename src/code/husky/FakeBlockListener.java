package code.husky;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class FakeBlockListener implements Listener {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));
	FakeBlock fb = new FakeBlock();
	FakeBlockAPI api = new FakeBlockAPI();

	List<String> select = fb.returnList();
	List<String> right = new ArrayList<String>();
	boolean wallExists = fb.wallExists();

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(select.contains(p.getName())) {
			p.sendMessage(ChatColor.GREEN + "[FakeBlock] Please Left-Click the start of the Fake Wall.");
			if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(!select.contains(p.getName()) && right.contains(p.getName())) {
					Location l = b.getLocation();
					int lx = l.getBlockX();
					int ly = l.getBlockY();
					int lz = l.getBlockZ();
					config.set("Data.FakeWall.bounds.x-start", lx);
					config.set("Data.FakeWall.bounds.y-start", ly);
					config.set("Data.FakeWall.bounds.z-start", lz);
					try {
						config.save("plugins/FakeBlock/config.yml");
						p.sendMessage(ChatColor.GREEN + "[FakeBlock] Great! Now Please Right-Click and select the second point!");
					} catch (IOException eeee) {
						eeee.printStackTrace();
					}
				} else {
					p.sendMessage(ChatColor.RED + "[FakeBlock] You need to Right-Click, not Left-Click!");
				}
			} else if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(select.contains(p.getName()) && right.contains(p.getName())) {
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
					select.remove(p.getName());
					right.remove(p.getName());
					api.sendFakeBlocks(p);
				}
			} else {
				e.setCancelled(false); // housekeeping
			}
		} else {
			e.setCancelled(false); // housekeeping
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(wallExists) {
			api.sendFakeBlocks(p);
		} else {
			// do nothing - housekeeping
		}
	}


}

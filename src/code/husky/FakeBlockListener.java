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
import org.bukkit.scheduler.BukkitRunnable;

public class FakeBlockListener implements Listener {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

	FakeBlockAPI api = new FakeBlockAPI();
	FakeBlock fb = new FakeBlock();

	List<String> select = config.getStringList("selecting");
	List<String> right = new ArrayList<String>();

	boolean wallExists = true;

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(select.contains(p.getName()) || right.contains(p.getName())) {
			p.sendMessage(ChatColor.GREEN + "[FakeBlock] Please Left-Click the start of the Fake Wall.");
			if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(select.contains(p.getName()) && !right.contains(p.getName())) {
					Location l = b.getLocation();
					int lx = l.getBlockX();
					int ly = l.getBlockY();
					int lz = l.getBlockZ();
					config.set("Data.FakeWall.bounds.x-start", lx);
					config.set("Data.FakeWall.bounds.y-start", ly);
					config.set("Data.FakeWall.bounds.z-start", lz);
					config.getStringList("selecting").remove(0);
					try {
						config.save("plugins/FakeBlock/config.yml");
						right.add(p.getName());
						p.sendMessage(ChatColor.GREEN + "[FakeBlock] Great! Now Please Right-Click and select the second point!");
						e.setCancelled(true);
					} catch (IOException eeee) {
						eeee.printStackTrace();
					}
				} else {
					p.sendMessage(ChatColor.RED + "[FakeBlock] You need to Right-Click, not Left-Click!");
					e.setCancelled(true);
				}
			} else if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(!select.contains(p.getName()) && right.contains(p.getName())) {
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
				e.setCancelled(false);
			}
		} else {
			e.setCancelled(false);
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		if(wallExists) {
			new BukkitRunnable(){
				public void run(){
					api.sendFakeBlocks(p);
				}
			}.runTaskLater(fb, (5 * 20));
		}
	}
	
	@EventHandler
	public void fakeBlockBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Location l = b.getLocation();
		List<Location> boop = api.blocks;
		if(boop.contains(l)) {
			e.setCancelled(true);
		}
	}

}
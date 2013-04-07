package code.husky;

import java.io.File;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FakeBlockListener implements Listener {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

	FakeBlockAPI api = new FakeBlockAPI();
	FakeBlock fb = new FakeBlock();

	boolean wallExists = true;

	
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		if(wallExists) {
			new BukkitRunnable(){
				public void run() {
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

	@EventHandler
	public void playerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location l = p.getLocation();
		if(api.blocks.contains(l)) {
			api.sendFakeBlocks(p);
		}
	}

}
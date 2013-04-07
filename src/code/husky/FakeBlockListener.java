package code.husky;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FakeBlockListener implements Listener {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

	FakeBlockAPI api = new FakeBlockAPI();
	FakeBlock fb = new FakeBlock();

	boolean wallExists = true;


	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		if(wallExists) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(new FakeBlock(), new Runnable() {
			     public void run() {
			          api.sendFakeBlocks(p);
			     }
			}, (5 * 20));
		}
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location l = p.getLocation();
		if(api.blocks.contains(l)) {
			api.sendFakeBlocks(p);
		}

		Location newblock = l.add(1, 0, 0);
		if(newblock.getBlock().getTypeId() == config.getInt("FakeBlock-ID")) {
			api.sendFakeBlocks(p);
		}

		Location another = l.add(0, 0, 1);
		if(another.getBlock().getTypeId() == config.getInt("FakeBlock-ID")) {
			api.sendFakeBlocks(p);
		}

		Location a = l.add(0, -1, 0);
		if(a.getBlock().getTypeId() == config.getInt("FakeBlock-ID")) {
			api.sendFakeBlocks(p);
		}

		Location as = l.add(0, 1, 0);
		if(as.getBlock().getTypeId() == config.getInt("FakeBlock-ID")) {
			api.sendFakeBlocks(p);
		}
		
		
	}

}
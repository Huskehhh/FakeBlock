package code.husky;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class FakeBlockAPI {

	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));
	ArrayList<Location> blocks = new ArrayList<Location>();

	int x = config.getInt("Data.FakeWall.bounds.x-start");
	int x1 = config.getInt("Data.FakeWall.bounds.x-end");
	int y = config.getInt("Data.FakeWall.bounds.y-start");
	int y1 = config.getInt("Data.FakeWall.bounds.y-end");
	int z = config.getInt("Data.FakeWall.bounds.z-start");
	int z1 = config.getInt("Data.FakeWall.bounds.z-end");

	public void sendFakeBlocks(Player p) {
		getBlocks(p.getWorld());
		for(Player s : Bukkit.getServer().getOnlinePlayers()) {
			if(!s.hasPermission("fakeblock.user")) {
				ListIterator<Location> li = blocks.listIterator();
				while(li.hasNext()) {
					Location nec = li.next();
					Material m = Material.getMaterial(config.getInt("FakeBlock-ID"));
					s.sendBlockChange(nec, m, (byte) 0);
				}
			} else {}
		}
	}

	public int getMaxX() {
		return Math.max(x,x1);
	}

	public int getMinX() {
		return Math.min(x,x1);
	}

	public int getMaxY() {
		return Math.max(y, y1);
	}

	public int getMinY() {
		return Math.min(y,y1);
	}

	public int getMaxZ() {
		return Math.max(z,z1);
	}

	public int getMinZ() {
		return Math.min(z,z1);
	}

	public void getBlocks(World w) {
		for (int x = this.getMinX(); x <= this.getMaxX(); x++) {
			for (int y = this.getMinY(); y <= this.getMaxY(); y++) {
				for (int z = this.getMinZ(); z <= this.getMaxZ(); z++) {
					this.blocks.add(new Location(w, x, y, z));
				}
			}
		}
	}
}
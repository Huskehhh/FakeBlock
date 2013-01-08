package code.husky;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FakeBlockAPI {
	
	public void sendFakeBlocks(Player p) {
		for(Player s : Bukkit.getServer().getOnlinePlayers()) {
			if(!s.hasPermission("fakeblock.user")) {
				s.sendBlockChange(arg0, arg1, arg2);
			} else {
				// do nothing
			}
		}
	}
	
	public Block[] getFakeWall() {
		return null;
	}

}

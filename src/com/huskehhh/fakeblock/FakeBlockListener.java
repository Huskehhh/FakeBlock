package com.huskehhh.fakeblock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;

public class FakeBlockListener implements Listener {

    YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/FakeBlock/config.yml"));

    Utility utility = new Utility();

    boolean wallExists = true;

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (wallExists) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(new FakeBlock(), new Runnable() {
                public void run() {
                    utility.sendFakeBlocks(p);
                }
            }, (5 * 20));
        }
    }

}
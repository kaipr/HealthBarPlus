package net.blockheaven.kaipr.healthbarplus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.getspout.spoutapi.SpoutManager;

public class HBPListener implements Listener {
    public static HealthBarPlus plugin;

    public HBPListener(HealthBarPlus instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.updateTitle(SpoutManager.getPlayer(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.updateTitle(SpoutManager.getPlayer(event.getPlayer()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player))
            return;
        
        plugin.delayedUpdateTitle(SpoutManager.getPlayer((Player) event.getEntity()));
    }
    
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player))
            return;
        
        plugin.delayedUpdateTitle(SpoutManager.getPlayer((Player) event.getEntity()));
    }
}

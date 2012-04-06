package net.blockheaven.kaipr.healthbarplus;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;

public class HealthBarPlus extends JavaPlugin {
    
    private static Logger logger;
    private static Heroes heroes = null;
    
    private HBPConfig config;
    
    private long updateRun;
    private HashMap<String, Integer> healthCache = new HashMap<String, Integer>();

    @Override
    public void onEnable() {
        logger = getLogger();
        
        config = new HBPConfig(this);
        config.load();
        
        // TODO: check if enabled and use a proper plugin listener
        Plugin test = getServer().getPluginManager().getPlugin("Heroes");
        if (test != null) {
            heroes = (Heroes) test;
            logInfo("Heroes found - using Heroes to get max health");
        }
        
        getServer().getPluginManager().registerEvents(new HBPListener(this), this);
        
        if (config.updateTicks > 0) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimerTask() {
                @Override
                public void run() {
                    boolean forceUpdate = false;
                    if (config.forceUpdateEvery > 0 && updateRun % config.forceUpdateEvery == 0) {
                        forceUpdate = true;
                    }
                    
                    for (SpoutPlayer player : SpoutManager.getOnlinePlayers()) {
                        updateTitle(player, forceUpdate);
                    }
                    
                    updateRun++;
                }
            }, config.updateTicks, config.updateTicks);
            
            logInfo("Manually updating health bars every " + config.updateTicks + " ticks");
        }
        
        logInfo("Enabled");
    }
    
    @Override
    public void onDisable() {
        logInfo("Disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("healthbarplus.reload")) {
            config.load();
            sender.sendMessage("&8[HealthBarPlus] &aReloaded Configuration");
            return true;
        }
        
        return false;
    }
    
    public void delayedUpdateTitle(final SpoutPlayer player) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new TimerTask() {
            @Override
            public void run() {
                updateTitle(player);
            }
        });
    }
    
    public void updateTitle(SpoutPlayer player) {
        updateTitle(player, false);
    }

    public void updateTitle(SpoutPlayer player, boolean forceUpdate) {
        Integer health;
        Integer maxHealth;
        
        // If we use Heroes and it's enabled in this world, use it to get health values
        if (heroes != null && !Heroes.properties.disabledWorlds.contains(player.getWorld().getName())) {
            Hero hero = HealthBarPlus.heroes.getCharacterManager().getHero(player.getPlayer());
            health    = (int)hero.getHealth();
            maxHealth = (int)hero.getMaxHealth();
        } else {
            health    = player.getHealth();
            maxHealth = player.getMaxHealth();
        }
        
        // Skip if maxHealth+health is the same as on last update (unless forceUpdate is true)
        if (!forceUpdate && (healthCache.containsKey(player.getName()) && healthCache.get(player.getName()) == (maxHealth + health)))
            return;
        
        // How many bars should be printed for health/damage?
        Integer healthCharCount = (int)((double)config.barCharacterCount / (double)maxHealth * health);
        Integer damageCharCount = (int)((double)config.barCharacterCount / (double)maxHealth * (maxHealth - health));
        
        // Create those shiny bars
        String healthBar = StringUtils.repeat(config.barCharacter, healthCharCount);
        String damageBar = StringUtils.repeat(config.barCharacter, damageCharCount);

        // Replace the placeholders
        String title = config.fullFormat;
        title = title.replace("{health_bar}", healthBar);
        title = title.replace("{damage_bar}", damageBar);
        title = title.replace("{health}", String.valueOf(health));
        title = title.replace("{max_health}", String.valueOf(maxHealth));

        // If we use permissions, we have to set titles for every single player
        if (config.useSeePermission) {
            // TODO: Only set for players which are in the same world, conflicts with cache atm
            for (SpoutPlayer otherPlayer : SpoutManager.getOnlinePlayers()) {
                if (otherPlayer.hasPermission("healthbarplus.see")) {
                    String oldTitle = player.getTitleFor(otherPlayer).split(config.separator)[0];
                    player.setTitleFor(otherPlayer, oldTitle + title);
                }
            }
        } else {
            String oldTitle = player.getTitle().split(config.separator)[0];
            player.setTitle(oldTitle + title);
        }
        
        // Update the health cache
        healthCache.put(player.getName(), maxHealth + health);
    }
    
    public void logInfo(String message) {
        log(message, Level.INFO);
    }
    
    public void log(String message, Level level) {
        logger.log(level, "[" + getDescription().getFullName() + "] " + message);
    }

}
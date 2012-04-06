/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.blockheaven.kaipr.healthbarplus;

import java.io.*;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author kaipr
 */
public class HBPConfig {
    private HealthBarPlus plugin;
    
    private final File configFile;
    private YamlConfiguration config;
    
    public String barFormat;
    public String barCharacter;
    public Integer barCharacterCount;
    public String separator;
    public Boolean useSeePermission;
    public Integer updateTicks;
    public Integer forceUpdateEvery;
    
    public String fullFormat;

    
    public HBPConfig(HealthBarPlus plugin) {
        this.plugin = plugin;
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
    }
    
    public boolean load() {
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Set defaults
        InputStream defaultConfigStream = plugin.getResource("defaults" + File.separator + configFile.getName());
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
            config.setDefaults(defaultConfig);
        }

        barFormat         = config.getString("bar.format");
        barCharacter      = config.getString("bar.character");
        barCharacterCount = config.getInt("bar.character_count");
        separator         = config.getString("system.separator");
        useSeePermission  = config.getBoolean("system.use_see_permission");
        updateTicks       = config.getInt("system.update_ticks");
        forceUpdateEvery  = config.getInt("system.force_update_every");
        
        fullFormat = separator + barFormat;

        return true;
    }
    
    private void createDefaultConfig() {
        try {
            configFile.getParentFile().mkdir();
            configFile.createNewFile();
            OutputStream output = new FileOutputStream(configFile, false);
            InputStream input = plugin.getResource("defaults" + File.separator + configFile.getName());
            byte[] buf = new byte[8192];
            while (true) {
                int length = input.read(buf);
                if (length < 0) {
                    break;
                }
                output.write(buf, 0, length);
            }
            input.close();
            output.close();
        } catch (Exception e) {
            plugin.log("Unable to create config file, disabling plugin...", Level.SEVERE);
            plugin.log(e.getMessage(), Level.SEVERE);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
}

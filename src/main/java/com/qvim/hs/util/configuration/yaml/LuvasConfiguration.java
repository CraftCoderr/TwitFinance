package com.qvim.hs.util.configuration.yaml;

import java.io.File;
import java.io.IOException;

import com.qvim.hs.util.logger.Logger;
import lombok.Getter;
import com.qvim.hs.util.configuration.FileConfiguration;

/**
 * Created by RINES on 21.04.17.
 */
public class LuvasConfiguration {
    
    @Getter
    private final String name;
    private final File file;
    private FileConfiguration config;

    public LuvasConfiguration(String name) {
        this.name = name;
        this.file = new File(name + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        if(config == null)
            this.reload();
        return config;
    }

    public void save() {
        if(file == null || config == null)
            return;
        try {
            get().save(file);
        }catch (IOException ex) {
            Logger.warn("Can't save configuration " + getName() + "!", ex);
        }
    }
    
}

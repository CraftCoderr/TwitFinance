package com.qvim.hs.util.configuration;

import java.util.HashMap;
import java.util.Map;
import com.qvim.hs.util.configuration.yaml.LuvasConfiguration;

/**
 * Created by RINES on 21.04.17.
 */
public class ConfigurationManager {
    
    private final static Map<String, LuvasConfiguration> configs = new HashMap<>();

    public static FileConfiguration getConfig(String name) {
        return getLConfig(name).get();
    }
    
    public static void saveConfig(String name) {
        getLConfig(name).save();
    }
    
    public static void reloadConfig(String name) {
        getLConfig(name).reload();
    }
    
    private static LuvasConfiguration getLConfig(String name) {
        LuvasConfiguration lc = configs.get(name);
        if(lc != null)
            return lc;
        lc = new LuvasConfiguration(name);
        configs.put(name, lc);
        return lc;
    }
    
}

package com.qvim.hs.util.configuration.yaml;

import lombok.Data;
import com.qvim.hs.util.configuration.Configuration;
import com.qvim.hs.util.configuration.FileConfigurationOptions;

/**
 * Created by RINES on 21.04.17.
 */
@Data
public class YamlConfigurationOptions extends FileConfigurationOptions {
    
    private int indent = 2;

    public YamlConfigurationOptions(Configuration configuration) {
        super(configuration);
    }

}

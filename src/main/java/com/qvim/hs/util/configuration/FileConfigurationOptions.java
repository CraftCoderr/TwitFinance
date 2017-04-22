package com.qvim.hs.util.configuration;

import lombok.Data;

/**
 * Created by RINES on 21.04.17.
 */
@Data
public class FileConfigurationOptions extends ConfigurationOptions {
    
    private String header;
    private boolean copyHeader = true;

    public FileConfigurationOptions(Configuration configuration) {
        super(configuration);
    }

}

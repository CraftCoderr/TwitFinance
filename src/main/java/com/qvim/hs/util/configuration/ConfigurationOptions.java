package com.qvim.hs.util.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by RINES on 21.04.17.
 */
@Data
@RequiredArgsConstructor
public class ConfigurationOptions {
    
    private char pathSeparator = '.';
    private boolean copyDefaults = false;
    private final Configuration configuration;

}

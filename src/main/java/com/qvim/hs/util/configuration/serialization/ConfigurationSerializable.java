package com.qvim.hs.util.configuration.serialization;

import java.util.Map;

/**
 * Created by RINES on 21.04.17.
 */
public interface ConfigurationSerializable {

    Map<String, Object> serialize();
    
}

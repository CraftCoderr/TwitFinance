package com.qvim.hs.util.configuration.yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import com.qvim.hs.util.configuration.serialization.ConfigurationSerialization;

/**
 * Created by RINES on 21.04.17.
 */
public class YamlConstructor extends SafeConstructor {

    public YamlConstructor() {
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }
    
    private class ConstructCustomObject extends ConstructYamlMap {
        @Override
        public Object construct(Node node) {
            if(node.isTwoStepsConstruction())
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            Map<?, ?> raw = (Map<?, ?>) super.construct(node);
            if(raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                Map<String, Object> typed = new LinkedHashMap<>(raw.size());
                raw.entrySet().forEach(e -> typed.put(e.getKey().toString(), e.getValue()));
                try {
                    return ConfigurationSerialization.deserializeObject(typed);
                }catch (IllegalArgumentException ex) {
                    throw new YAMLException("Could not deserialize object", ex);
                }
            }
            return raw;
        }

        @Override
        public void construct2ndStep(Node node, Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }
    }
    
}

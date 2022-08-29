package com.pipeline.runtime

import com.pipeline.runtime.interfaces.ICasc
import groovy.transform.ToString
import org.yaml.snakeyaml.Yaml
import java.util.concurrent.ConcurrentHashMap

//import groovy.transform.CompileStatic

//@CompileStatic
@ToString
class Casc extends ConcurrentHashMap implements ICasc {

    Casc(String configFile) {
        super()
        Objects.requireNonNull(configFile, "Configuration file cannot be null.")
        loadConfig(configFile as File)
    }

    @Override
    void loadConfig(File configFile) {
        Yaml parser = new Yaml()
        this.putAll(parser.load((configFile).text))
    }

}

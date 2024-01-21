package me.monkey_cat.velocityct.config;

import com.github.smuddgge.squishyconfiguration.implementation.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

public class CTYamlConfiguration extends YamlConfiguration {
    public CTYamlConfiguration(@NotNull File file) {
        super(file);
    }

    @Override
    @SuppressWarnings("all")
    public boolean save() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(BLOCK);

        Yaml yaml = new Yaml(dumperOptions);
        try {
            // set uft-8 charset
            FileWriter writer = new FileWriter(getFile(), StandardCharsets.UTF_8);
            yaml.dump(this.data, writer);
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }
}

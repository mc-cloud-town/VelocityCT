package me.monkey_cat.velocityct.config;

import me.monkey_cat.velocityct.utils.BaseConfig;

import java.nio.file.Path;

public class Config extends BaseConfig {
    public Config(Path configPath) {
        super(configPath);
    }

    public boolean isLocationsEnable() {
        tryLoad();
        return configuration.getBoolean("locationsEnable");
    }

    @Override
    public void parse() {
    }
}

package me.monkey_cat.velocityct.config;

import me.monkey_cat.velocityct.utils.BaseConfig;

import java.nio.file.Path;

public class Config extends BaseConfig {
    public Config(Path configPath) {
        super(configPath);
    }

    public boolean isEnable() {
        tryLoad();
        return configuration.getBoolean("enable");
    }

    @Override
    public void parse() {
    }
}

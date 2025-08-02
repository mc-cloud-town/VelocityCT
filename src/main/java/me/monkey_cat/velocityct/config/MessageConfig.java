package me.monkey_cat.velocityct.config;

import me.monkey_cat.velocityct.utils.BaseConfig;

import java.nio.file.Path;

public class MessageConfig extends BaseConfig {
    public MessageConfig(Path configPath) {
        super(configPath);
    }

    public Boolean getKick() {
        tryLoad();
        return configuration.getBoolean("enable");
    }

    public String getKickMessage() {
        tryLoad();
        return configuration.getString("kickMessage");
    }

    public String getTryMoveKickMessage() {
        tryLoad();
        return configuration.getString("tryMoveKickMessage");
    }

    public String getAutoGuidanceMoveMessage() {
        tryLoad();
        return configuration.getString("autoGuidanceMoveMessage");
    }


    @Override
    public void parse() {
    }
}

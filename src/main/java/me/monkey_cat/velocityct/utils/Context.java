package me.monkey_cat.velocityct.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import me.monkey_cat.velocityct.config.Config;
import me.monkey_cat.velocityct.config.MessageConfig;
import me.monkey_cat.velocityct.config.WhitelistConfig;
import org.slf4j.Logger;

import java.nio.file.Path;

public record Context(ProxyServer server, Logger logger, Config config, MessageConfig messageConfig,
                      WhitelistConfig whitelistConfig, Path dataDirectory) {
}

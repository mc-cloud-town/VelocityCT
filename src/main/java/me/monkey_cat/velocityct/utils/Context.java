package me.monkey_cat.velocityct.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import me.monkey_cat.velocityct.config.Config;
import org.slf4j.Logger;

import java.nio.file.Path;

public class Context {
    private final ProxyServer server;
    private final Logger logger;
    private final Config config;
    private final Path dataDirectory;

    public Context(ProxyServer server, Logger logger, Config config, Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.config = config;
        this.dataDirectory = dataDirectory;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}

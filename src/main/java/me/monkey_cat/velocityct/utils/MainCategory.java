package me.monkey_cat.velocityct.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import me.monkey_cat.velocityct.config.Config;
import me.monkey_cat.velocityct.config.MessageConfig;
import me.monkey_cat.velocityct.config.WhitelistConfig;
import org.slf4j.Logger;

import java.nio.file.Path;

public class MainCategory {
    protected final ProxyServer server;
    protected final Config config;
    protected final MessageConfig messageConfig;
    protected final WhitelistConfig whitelistConfig;
    protected final Logger logger;
    protected final Path dataDirectory;
    protected final Context context;

    public MainCategory(Context context) {
        this.server = context.server();
        this.config = context.config();
        this.messageConfig = context.messageConfig();
        this.whitelistConfig = context.whitelistConfig();
        this.logger = context.logger();
        this.dataDirectory = context.dataDirectory();
        this.context = context;
    }
}

package me.monkey_cat.velocityct.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import me.monkey_cat.velocityct.config.Config;
import org.slf4j.Logger;

import java.nio.file.Path;

public class MainCategory {
    protected final ProxyServer server;
    protected final Config config;
    protected final Logger logger;
    protected final Path dataDirectory;
    protected final Context context;

    public MainCategory(Context context) {
        this.context = context;
        this.server = context.getServer();
        this.config = context.getConfig();
        this.logger = context.getLogger();
        this.dataDirectory = context.getDataDirectory();
    }

}

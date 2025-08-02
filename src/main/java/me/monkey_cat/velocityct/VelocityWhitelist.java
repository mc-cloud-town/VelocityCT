package me.monkey_cat.velocityct;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.monkey_cat.velocityct.commend.WhitelistCommand;
import me.monkey_cat.velocityct.config.Config;
import me.monkey_cat.velocityct.config.MessageConfig;
import me.monkey_cat.velocityct.config.WhitelistConfig;
import me.monkey_cat.velocityct.feature.Locations;
import me.monkey_cat.velocityct.feature.Whitelist;
import me.monkey_cat.velocityct.utils.Context;
import me.monkey_cat.velocityct.utils.MainCategory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;

@Plugin(
        id = VelocityWhitelistMeta.ID,
        name = VelocityWhitelistMeta.NAME,
        description = "Test",
        authors = {"a3510377"}
)
public class VelocityWhitelist extends MainCategory {
    private final Locations locations;
    private final Whitelist whitelist;
    private final TranslationRegistry registry = TranslationRegistry.create(Key.key("velocityct:velocityct"));

    @Inject
    public VelocityWhitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        super(new Context(
                server,
                logger,
                new Config(dataDirectory.resolve("config.yml")),
                new MessageConfig(dataDirectory.resolve("messages.yml")),
                new WhitelistConfig(dataDirectory.resolve("whitelist.yml")),
                dataDirectory
        ));

        registerLanguage(Locale.US);
        registerLanguage(Locale.TRADITIONAL_CHINESE);
        registry.defaultLocale(Locale.US);

        locations = new Locations(this.context, dataDirectory.resolve("locations.json"));
        whitelist = new Whitelist(this.context);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config.tryLoad();
        messageConfig.tryLoad();
        whitelistConfig.tryLoad();

        // locations
        server.getEventManager().register(this, DisconnectEvent.class, this.locations::onDisconnectConnect);
        server.getEventManager().register(this, ServerPreConnectEvent.class, this.locations::onServerPreConnect);

        // whitelist
        server.getEventManager().register(this, ServerPreConnectEvent.class, PostOrder.LAST, this.whitelist::onServerPreConnect);

        new WhitelistCommand(context).register(server.getCommandManager());
    }

    public void registerLanguage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("plugin.i18n.velocityct", locale, UTF8ResourceBundleControl.get());
        registry.registerAll(locale, bundle, true);
        GlobalTranslator.translator().addSource(registry);
    }
}

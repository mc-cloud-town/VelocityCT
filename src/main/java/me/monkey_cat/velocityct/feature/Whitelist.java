package me.monkey_cat.velocityct.feature;

import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.monkey_cat.velocityct.utils.Context;
import me.monkey_cat.velocityct.utils.MainCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Optional;

public class Whitelist extends MainCategory {
    public Whitelist(Context context) {
        super(context);
    }

    public void onServerPreConnect(ServerPreConnectEvent event) {
        ProxyServer proxyServer = server;

        if (!config.isWhitelistEnable()) return;

        Optional<RegisteredServer> server = event.getResult().getServer();
        server.ifPresent(registeredServer -> {
            Player player = event.getPlayer();
            if (!config.hasInWhitelist(registeredServer, player)) {
                RegisteredServer previousServer = event.getPreviousServer();

                if (previousServer == null) {
                    for (String server1Name : proxyServer.getConfiguration().getAttemptConnectionOrder()) {
                        if (config.hasInWhitelist(server1Name, player)) {
                            Optional<RegisteredServer> server1 = proxyServer.getServer(server1Name);
                            if (server1.isPresent()) {
                                event.setResult(ServerPreConnectEvent.ServerResult.allowed(server1.get()));
                                player.sendMessage(MiniMessage.miniMessage().deserialize(
                                        config.getAutoGuidanceMoveMessage(),
                                        Placeholder.component("old_server", Component.text(event.getOriginalServer().getServerInfo().getName())),
                                        Placeholder.component("new_server", Component.text(server1Name))
                                ));
                                return;
                            }
                        }
                    }
                    player.disconnect(MiniMessage.miniMessage().deserialize(config.getKickMessage()));
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(config.getTryMoveKickMessage(),
                            Placeholder.component("old_server", Component.text(previousServer.getServerInfo().getName())),
                            Placeholder.component("new_server", Component.text(event.getOriginalServer().getServerInfo().getName())))
                    );
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                }
            }
        });
    }
}

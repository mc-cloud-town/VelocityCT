package me.monkey_cat.velocityct.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.List;

public class Utils {
    public static List<String> getAllServersName(ProxyServer server) {
        return server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList();
    }

    public static List<String> getAllPlayersName(ProxyServer server) {
        return server.getAllPlayers().stream().map(Player::getUsername).toList();
    }
}

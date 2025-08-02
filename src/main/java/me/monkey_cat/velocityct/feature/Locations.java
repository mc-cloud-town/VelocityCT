package me.monkey_cat.velocityct.feature;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.monkey_cat.velocityct.utils.Context;
import me.monkey_cat.velocityct.utils.MainCategory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Optional;

public class Locations extends MainCategory {
    private final Path path;
    private final Path tempFilePath;
    private final File file;
    private final Object fileLock = new Object();
    private final Type typeObject = new TypeToken<HashMap<String, String>>() {
    }.getType();
    private HashMap<String, String> data;
    private long lastModified;

    public Locations(Context context, Path path) {
        super(context);
        this.path = path;
        this.file = path.toFile();
        this.tempFilePath = path.resolveSibling(path.getFileName().toString() + ".tmp");
    }

    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!whitelistConfig.isLocationsEnable() || event.getPreviousServer() != null) return;

        Player player = event.getPlayer();
        Optional<ServerConnection> currentServer = player.getCurrentServer();
        if (currentServer.isEmpty()) {
            tryLoad();

            String key = summonKey(player);
            if (!data.containsKey(key)) return;

            String nextServerName = data.get(key);
            Optional<RegisteredServer> oldServer = server.getServer(nextServerName);
            oldServer.ifPresent(server -> event.setResult(ServerPreConnectEvent.ServerResult.allowed(server)));
        }
    }

    public void onDisconnectConnect(DisconnectEvent event) {
        if (!whitelistConfig.isLocationsEnable()) return;

        Player player = event.getPlayer();
        Optional<ServerConnection> server = player.getCurrentServer();
        if (server.isEmpty()) return;

        tryLoad();
        data.put(summonKey(player), server.get().getServer().getServerInfo().getName());
        try {
            save();
        } catch (IOException e) {
            logger.info("get error");
        }
    }

    public String summonKey(Player player) {
        String name = player.getUsername();
        String adder = player.getRemoteAddress().getAddress().toString();
        return name + ";" + adder;
    }

    public void save() throws IOException {
        if (data == null) {
            data = new HashMap<>();
        }
        Files.writeString(tempFilePath, new Gson().toJson(data, typeObject), StandardCharsets.UTF_8);
        synchronized (fileLock) {
            Files.move(tempFilePath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean tryLoad() {
        if (file.isFile()) {
            final long fileLastMod = file.lastModified();
            if (fileLastMod == lastModified && data != null) return false;

            synchronized (fileLock) {
                try {
                    data = new Gson().fromJson(Files.readString(path), typeObject);
                    if (data != null) {
                        return true;
                    }
                } catch (IOException ignored) {
                }
            }
            lastModified = fileLastMod;
        }

        try {
            save();
        } catch (Exception ignored) {
        }

        return tryLoad();
    }
}

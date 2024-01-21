package me.monkey_cat.velocityct.config;

import com.github.smuddgge.squishyconfiguration.implementation.YamlConfiguration;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config {
    private final File file;
    private final HashMap<String, HashSet<String>> serverWhitelist = new HashMap<>();
    private final Configuration configuration;
    private long lastModified;

    public Config(Path configPath) {
        this.file = configPath.toFile();
        configuration = new YamlConfiguration(this.file);
        configuration.load();
    }

    public void save() {
        configuration.save();
    }

    public boolean tryLoad() {
        if (file.isFile()) {
            final long fileLastMod = file.lastModified();

            if (fileLastMod == lastModified) return false;
            if (configuration.load()) {
                try {
                    parse();
                    lastModified = fileLastMod;
                    return true;
                } catch (Exception ignored) {
                }
            }
        }

        writeDefault();
        return tryLoad();
    }

    public void writeDefault() {
        file.getParentFile().mkdirs();
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
            Files.delete(file.toPath());
            Files.copy(Objects.requireNonNull(in), file.toPath());
        } catch (Exception ignored) {
        }
    }

    public void parse() {
        serverWhitelist.clear();
        Map<String, Set<String>> groups = getItem("groups", false);
        Map<String, Set<String>> whitelist = getItem("whitelist", false);
        Map<String, Set<String>> specialWhitelist = getItem("specialWhitelist", false);

        for (Map.Entry<String, Set<String>> groupEntry : groups.entrySet()) {
            String label = groupEntry.getKey();
            for (String serverName : groupEntry.getValue()) {
                serverWhitelist.computeIfAbsent(serverName, k -> new HashSet<>())
                        .addAll(whitelist.getOrDefault(label, new HashSet<>()));
            }
        }

        for (Map.Entry<String, Set<String>> specialEntry : specialWhitelist.entrySet()) {
            String username = specialEntry.getKey();
            for (String serverName : specialEntry.getValue()) {
                if (serverName.startsWith("!")) {
                    serverWhitelist.computeIfPresent(serverName.substring(1), (k, v) -> {
                        v.remove(username);
                        return v;
                    });
                } else {
                    serverWhitelist.computeIfPresent(serverName, (k, v) -> {
                        v.add(username);
                        return v;
                    });
                }
            }
        }
    }

    private Map<String, Set<String>> getItem(String path, boolean reload) {
        if (reload) tryLoad();

        Map<String, Set<String>> groups = new HashMap<>();
        configuration.getMap(path).forEach((s, o) -> {
            if (o instanceof List<?>) {
                List<String> names = ((List<?>) o).stream().map(Object::toString).toList();
                groups.put(s, new HashSet<>(names));
            }
        });

        return groups;
    }

    private Map<String, Set<String>> getItem(String path) {
        return getItem(path, true);
    }

    public boolean isEnable() {
        tryLoad();
        return configuration.getBoolean("enable");
    }

    public boolean isLocationsEnable() {
        tryLoad();
        return isEnable() && configuration.getBoolean("locationsEnable");
    }

    public boolean isWhitelistEnable() {
        tryLoad();
        return isEnable() && configuration.getBoolean("whitelistEnable");
    }

    public boolean hasInWhitelist(RegisteredServer server, Player player) {
        return hasInWhitelist(server.getServerInfo().getName(), player);
    }

    public boolean hasInWhitelist(String serverName, Player player) {
        tryLoad();
        return serverWhitelist.containsKey(serverName) && serverWhitelist.get(serverName).contains(player.getUsername());
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

    public Map<String, Set<String>> getGroups() {
        return getItem("groups");
    }

    public Map<String, Set<String>> getWhitelist() {
        return getItem("whitelist");
    }

    public Map<String, Set<String>> getSpecialWhitelist() {
        return getItem("specialWhitelist");
    }
}

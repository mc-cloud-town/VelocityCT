package me.monkey_cat.velocityct.config;

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
    private final CTYamlConfiguration configuration;
    private long lastModified;

    public Config(Path configPath) {
        this.file = configPath.toFile();
        configuration = new CTYamlConfiguration(this.file);
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
        //noinspection ResultOfMethodCallIgnored
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
                    HashSet<String> tmp = serverWhitelist.getOrDefault(serverName, new HashSet<>());
                    tmp.add(username);
                    serverWhitelist.put(serverName, tmp);
                }
            }
        }
    }

    private Map<String, Set<String>> getItem(String path, boolean reload) {
        if (reload) tryLoad();

        Map<String, Set<String>> data = new HashMap<>();
        configuration.getMap(path).forEach((s, o) -> {
            if (o instanceof List<?>) {
                List<String> names = ((List<?>) o).stream().map(Object::toString).toList();
                data.put(s, new HashSet<>(names));
            }
        });

        return data;
    }

    private Map<String, List<String>> setToListType(Map<String, Set<String>> old) {
        Map<String, List<String>> data = new HashMap<>();
        old.forEach((s, o) -> data.put(s, o.stream().map(Object::toString).toList()));
        return data;
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

    public void setWhitelistEnable(Boolean enable) {
        configuration.set("enable", enable);
    }

    public boolean hasInWhitelist(RegisteredServer server, Player player) {
        return hasInWhitelist(server.getServerInfo().getName(), player);
    }

    public boolean hasInWhitelist(String serverName, Player player) {
        return hasInWhitelist(serverName, player.getUsername());
    }

    public boolean hasInWhitelist(String serverName, String playerName) {
        tryLoad();
        return serverWhitelist.containsKey(serverName) && serverWhitelist.get(serverName).contains(playerName);
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

    public void setGroups(Map<String, Set<String>> groups) {
        configuration.set("groups", setToListType(groups));
        save();
    }

    public Map<String, Set<String>> getWhitelist() {
        return getItem("whitelist");
    }

    public void setWhitelist(Map<String, Set<String>> whitelist) {
        configuration.set("whitelist", setToListType(whitelist));
        save();
    }

    public Map<String, Set<String>> getSpecialWhitelist() {
        return getItem("specialWhitelist");
    }

    public void setSpecialWhitelist(Map<String, Set<String>> specialWhitelist) {
        configuration.set("specialWhitelist", setToListType(specialWhitelist));
        save();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}

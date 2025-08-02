package me.monkey_cat.velocityct.utils;

import com.github.smuddgge.squishyconfiguration.implementation.YamlConfiguration;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

public abstract class BaseConfig {
    protected final File configFile;
    protected final CTYamlConfiguration configuration;
    protected long lastModified;

    public BaseConfig(Path configPath) {
        this.configFile = configPath.toFile();

        configuration = new CTYamlConfiguration(this.configFile);
        configuration.load();
    }

    public static void backupFileWithLimit(File file, int maxBackupCount, String backupPrefix) {
        try {
            Path originalPath = file.toPath();
            if (!Files.exists(originalPath) || Files.isSymbolicLink(originalPath)) return;

            if (!backupPrefix.isEmpty() && !backupPrefix.startsWith(".")) backupPrefix = "." + backupPrefix;

            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String backupName = originalPath.getFileName().toString().replaceAll("\\.yml$", "") +
                    backupPrefix + "." + timestamp + ".yml.old";
            Path backupPath = originalPath.resolveSibling(backupName);

            Files.copy(originalPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            File parentDir = file.getParentFile();
            String finalBackupPrefix = backupPrefix;
            File[] backups = parentDir.listFiles((dir, name) ->
                    name.startsWith(file.getName()) && name.contains(finalBackupPrefix) && name.endsWith(".old")
            );

            if (backups != null && backups.length > maxBackupCount) {
                Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());
                for (int i = maxBackupCount; i < backups.length; i++) {
                    //noinspection ResultOfMethodCallIgnored
                    backups[i].delete();
                }
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void save() {
        configuration.save();
    }

    public boolean tryLoad() {
        if (configFile.isFile()) {
            final long fileLastMod = configFile.lastModified();

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

        if (!configFile.exists()) {
            writeDefault();
            return tryLoad();
        }

        return false;
    }

    public void writeDefault() {
        //noinspection ResultOfMethodCallIgnored
        configFile.getParentFile().mkdirs();
        Path configPath = configFile.toPath();

        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(this.configFile.getName())) {
            if (in == null) return;

            BaseConfig.backupFileWithLimit(configFile, 2, "writeDefault");

            // for symbolic link, only overwrite its content, do not delete the symlink itself
            if (Files.isSymbolicLink(configPath)) {
                Files.writeString(configPath, new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } else {
                Files.deleteIfExists(configPath);
                Files.copy(in, configPath);
            }
        } catch (Exception ignored) {
        }
    }

    public abstract void parse();

    protected Map<String, Set<String>> getItem(String path, boolean reload) {
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

    protected Map<String, Set<String>> getItem(String path) {
        return getItem(path, true);
    }

    protected Map<String, List<String>> setToListType(Map<String, Set<String>> old) {
        Map<String, List<String>> data = new HashMap<>();
        old.forEach((s, o) -> data.put(s, o.stream().map(Object::toString).toList()));
        return data;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public static class CTYamlConfiguration extends YamlConfiguration {
        public CTYamlConfiguration(@NotNull File file) {
            super(file);
        }

        @Override
        public boolean save() {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setPrettyFlow(true);
            dumperOptions.setDefaultFlowStyle(BLOCK);

            Yaml yaml = new Yaml(dumperOptions);
            try {
                File file = getFile();
                BaseConfig.backupFileWithLimit(file, 5, "save");

                String output = yaml.dump(this.data);
                Files.writeString(file.toPath(), output, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                //noinspection CallToPrintStackTrace
                exception.printStackTrace();
                return false;
            }

            return true;
        }
    }
}

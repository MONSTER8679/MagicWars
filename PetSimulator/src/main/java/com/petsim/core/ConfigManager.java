package com.petsim.core;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Phase 6: central config service.
 * - defaults from bundled resources
 * - validation with clear errors
 * - schema version + migration hook
 * - safe reload that never resets player data
 */
public final class ConfigManager {

    private final JavaPlugin plugin;
    private YamlConfiguration main;
    private int schemaVersion;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IllegalStateException("Cannot create plugin data folder");
            }
            plugin.saveResource("config.yml", false);
        }
        this.main = YamlConfiguration.loadConfiguration(file);

        int bundled = bundledSchemaVersion();
        int onDisk = main.getInt("config_schema_version", bundled);
        if (onDisk < bundled) {
            migrate(onDisk, bundled);
        } else if (onDisk > bundled) {
            throw new IllegalStateException(
                "config.yml schema v" + onDisk + " is newer than plugin (v" + bundled + "). Downgrade refused.");
        }
        this.schemaVersion = bundled;

        validate();
    }

    private int bundledSchemaVersion() {
        YamlConfiguration bundled = YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(
                plugin.getClass().getResourceAsStream("/config.yml"), java.nio.charset.StandardCharsets.UTF_8));
        return bundled.getInt("config_schema_version", 1);
    }

    /** Phase 6: migration chain. Only forwards, one step at a time. */
    private void migrate(int from, int to) {
        int v = from;
        while (v < to) {
            switch (v) {
                case 1 -> { /* future: v1 -> v2 renames go here */ }
                default -> throw new IllegalStateException("No migration from schema v" + v);
            }
            v++;
        }
        main.set("config_schema_version", to);
        try {
            main.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Config migration save failed: " + e.getMessage(), e);
        }
    }

    /** Phase 6: validation with precise, actionable messages. */
    private void validate() {
        requirePositive("performance.pet_update_interval_ticks",
            main.getInt("performance.pet_update_interval_ticks", 2));
        requirePositive("performance.max_follow_pets",
            main.getInt("performance.max_follow_pets", 6));
        String storage = main.getString("storage.type", "sqlite");
        if (!List.of("sqlite").contains(storage)) {
            throw new IllegalStateException("storage.type '" + storage
                + "' is not supported yet (mysql/mariadb planned). Use 'sqlite'.");
        }
    }

    private void requirePositive(String path, int value) {
        if (value <= 0) {
            throw new IllegalStateException("config.yml: '" + path + "' must be > 0, got " + value);
        }
    }

    public void reload() { load(); }

    public int schemaVersion() { return schemaVersion; }

    public boolean moduleEnabled(String id) {
        return main.getBoolean("modules." + id, true);
    }

    public int performance(String key, int def) {
        return main.getInt("performance." + key, def);
    }

    public String world(String key, String def) {
        return main.getString("worlds." + key, def);
    }

    public String resourcePackUrl() { return main.getString("resource_pack.url", ""); }
    public String resourcePackSha1() { return main.getString("resource_pack.sha1", ""); }
    public String resourcePackPrompt() { return main.getString("resource_pack.prompt", ""); }

    public YamlConfiguration raw() { return main; }
}

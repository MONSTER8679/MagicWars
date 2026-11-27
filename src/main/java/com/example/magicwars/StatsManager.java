package com.example.magicwars;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

/** إحصائيات اللاعبين: عملات، خبرة، مستحضرات — v6.1: حماية من المفاتيح التالفة */
public class StatsManager {

    private final MagicWarsPlugin plugin;
    private final YamlConfiguration data;
    private final File file;

    public StatsManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        this.data = file.exists() ? YamlConfiguration.loadConfiguration(file)
                                  : new YamlConfiguration();
    }

    public int coins(UUID id) {
        return data.getInt(id + ".coins", 0);
    }

    public int xp(UUID id) {
        return data.getInt(id + ".xp", 0);
    }

    public boolean spendCoins(UUID id, int amount) {
        if (coins(id) < amount) return false;
        data.set(id + ".coins", coins(id) - amount);
        save();
        return true;
    }

    public void addCoins(UUID id, int amount) {
        data.set(id + ".coins", coins(id) + amount);
        save();
    }

    public void addXp(UUID id, int amount) {
        data.set(id + ".xp", xp(id) + amount);
        save();
    }

    // المستحضرات التجميلية
    public boolean cosmeticOwned(UUID id, String cat, String key) {
        return data.getBoolean(id + ".cos." + cat + ".owned." + key, false);
    }

    public void setCosmeticOwned(UUID id, String cat, String key) {
        data.set(id + ".cos." + cat + ".owned." + key, true);
        save();
    }

    public String selectedCosmetic(UUID id, String cat) {
        return data.getString(id + ".cos." + cat + ".selected", "");
    }

    public void selectCosmetic(UUID id, String cat, String key) {
        data.set(id + ".cos." + cat + ".selected", key);
        save();
    }

    public void save() {
        try { data.save(file); }
        catch (Exception e) {
            plugin.getLogger().warning("تعذر حفظ stats.yml: " + e.getMessage());
        }
    }

    /** v6.1: تنظيف المفاتيح التالفة (مثل إدخالات فارغة أو غير صالحة) */
    public void cleanup() {
        ConfigurationSection root = data.getConfigurationSection("");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            try { UUID.fromString(key); }
            catch (IllegalArgumentException ex) { data.set(key, null); }
        }
        save();
    }
}

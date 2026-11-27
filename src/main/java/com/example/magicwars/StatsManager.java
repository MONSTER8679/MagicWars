package com.example.magicwars;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/** إحصائيات اللاعبين: عملات، XP، قتلات، انتصارات + المستحضرات التجميلية */
public class StatsManager {

    private final MagicWarsPlugin plugin;
    private final YamlConfiguration data;
    private final File file;

    public StatsManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.data = file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();
    }

    public void save() {
        try { data.save(file); }
        catch (IOException e) { plugin.getLogger().warning("تعذر حفظ stats.yml"); }
    }

    private int getInt(UUID id, String key, int def) {
        try { return data.getInt(id + "." + key, def); }      // ✅ إصلاح v6.1
        catch (Exception e) { return def; }
    }

    public int coins(UUID id) { return getInt(id, "coins", 0); }
    public int xp(UUID id)    { return getInt(id, "xp", 0); }
    public int kills(UUID id) { return getInt(id, "kills", 0); }
    public int deaths(UUID id){ return getInt(id, "deaths", 0); }
    public int wins(UUID id)  { return getInt(id, "wins", 0); }
    public int games(UUID id) { return getInt(id, "games", 0); }

    public void set(UUID id, String key, int value) {
        data.set(id + "." + key, value);
        save();
    }

    public void addCoins(UUID id, int amount) {
        data.set(id + ".coins", coins(id) + amount);
        save();
    }

    public void addXp(UUID id, int amount) {
        data.set(id + ".xp", xp(id) + amount);
        save();
    }

    public void addKill(UUID id) {
        data.set(id + ".kills", kills(id) + 1);
        data.set(id + ".coins", coins(id)
                + plugin.getConfig().getInt("economy.coins-per-kill", 5));
        data.set(id + ".xp", xp(id)
                + plugin.getConfig().getInt("economy.xp-per-kill", 10));
        save();
    }

    public void addDeath(UUID id) {
        data.set(id + ".deaths", deaths(id) + 1);
        save();
    }

    public void addWin(UUID id) {
        data.set(id + ".wins", wins(id) + 1);
        data.set(id + ".coins", coins(id)
                + plugin.getConfig().getInt("economy.coins-per-win", 50));
        data.set(id + ".xp", xp(id)
                + plugin.getConfig().getInt("economy.xp-per-win", 30));
        save();
    }

    public void addGame(UUID id) {
        data.set(id + ".games", games(id) + 1);
        save();
    }

    public boolean spendCoins(UUID id, int amount) {
        if (coins(id) < amount) return false;
        addCoins(id, -amount);
        return true;
    }

    // ===== المستحضرات التجميلية =====
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
}

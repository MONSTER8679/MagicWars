package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** إدارة الملاعب: إنشاء العالم وتوليد الملاعب وحفظها */
public class ArenaManager {

    private final MagicWarsPlugin plugin;
    private final List<Arena> arenas = new ArrayList<>();
    private final File file;

    public ArenaManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public List<Arena> all() { return arenas; }

    public Arena freeArena(GameMode mode) {
        for (Arena a : arenas)
            if (a.mode == mode && !a.inUse) return a;
        return null;
    }

    public void markUsed(Arena a, boolean used) { a.inUse = used; }

    /** إنشاء عالم ملاعب جديد (flat) */
    public boolean createWorld(String name, boolean generate) {
        if (plugin.getServer().getWorld(name) != null) return true;
        org.bukkit.WorldCreator wc = new org.bukkit.WorldCreator(name)
                .type(org.bukkit.WorldType.FLAT)
                .generateStructures(false);
        org.bukkit.World w = plugin.getServer().createWorld(wc);
        if (w == null) return false;
        w.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(org.bukkit.GameRule.KEEP_INVENTORY, true);
        w.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
        w.setTime(6000);
        if (generate) generateMaps(name, GameMode.SOLO, 2);
        return true;
    }

    /** توليد عدد من الملاعب لنمط في عالم */
    public void generateMaps(String worldName, GameMode mode, int count) {
        org.bukkit.World w = plugin.getServer().getWorld(worldName);
        if (w == null) w = plugin.getServer().getWorlds().get(0);
        int startIndex = arenas.size();
        for (int i = 0; i < count; i++) {
            Arena a = MapGenerator.generate(w, startIndex + i, mode);
            arenas.add(a);
        }
        save();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = cfg.getConfigurationSection("arenas");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            String worldName = sec.getString(key + ".world", "");
            int ox = sec.getInt(key + ".ox", 0);
            int y = sec.getInt(key + ".y", 80);
            String modeName = sec.getString(key + ".mode", "SOLO");
            GameMode mode = GameMode.byName(modeName);
            if (mode == null) mode = GameMode.SOLO;
            org.bukkit.World w = plugin.getServer().getWorld(worldName);
            if (w == null) continue;
            arenas.add(new Arena(mode, w, ox, y));
        }
    }

    private void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        int i = 0;
        for (Arena a : arenas) {
            cfg.set("arenas.a" + i + ".world", a.world.getName());
            cfg.set("arenas.a" + i + ".ox", a.ox);
            cfg.set("arenas.a" + i + ".y", a.baseY);
            cfg.set("arenas.a" + i + ".mode", a.mode.name());
            i++;
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("تعذر حفظ arenas.yml: " + e.getMessage());
        }
    }
}

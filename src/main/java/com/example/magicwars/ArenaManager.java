package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** إدارة الملاعب: توليد وحفظ وتحميل */
public class ArenaManager {

    private final MagicWarsPlugin plugin;
    private final List<Arena> arenas = new ArrayList<>();
    private final File file;

    public ArenaManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public List<Arena> all() { return arenas; }

    /** إنشاء عالم الفراغ للملاعب (مرة واحدة) */
    public void createWorld(String name, boolean loadIfExists) {
        if (Bukkit.getWorld(name) != null) return;
        WorldCreatorOptions options = new WorldCreatorOptions(name);
        Bukkit.createWorld(new org.bukkit.WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .generator(new VoidGenerator()));
        plugin.getLogger().info("تم إنشاء عالم الملاعب: " + name);
    }

    /** توليد عدد من الملاعب لنمط معين */
    public void generateMaps(String worldName, GameMode mode, int count) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            plugin.getLogger().warning("العالم غير موجود: " + worldName);
            return;
        }
        int index = arenas.size() + 1;
        for (int i = 0; i < count; i++) {
            Arena a = MapGenerator.generate(w, index + i, mode);
            arenas.add(a);
            plugin.getLogger().info("توليد ملعب #" + (index + i) + " لنمط " + mode);
        }
        save();
    }

    public void save() {
        YamlConfiguration data = new YamlConfiguration();
        int i = 0;
        for (Arena a : arenas) {
            data.set(i + ".mode", a.mode.name());
            data.set(i + ".world", a.world.getName());
            data.set(i + ".ox", a.ox);
            data.set(i + ".baseY", a.baseY);
            i++;
        }
        try { data.save(file); }
        catch (Exception e) { plugin.getLogger().warning("تعذر حفظ arenas.yml"); }
    }

    /** تحميل الملاعب المحفوظة (لا تُعاد القياسات — للإشارة فقط) */
    public void load() {
        if (!file.exists()) return;
        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            String worldName = data.getString(key + ".world", "");
            World w = Bukkit.getWorld(worldName);
            if (w == null) continue;
            GameMode mode;
            try { mode = GameMode.valueOf(data.getString(key + ".mode", "SOLO")); }
            catch (IllegalArgumentException ex) { continue; }
            int ox = data.getInt(key + ".ox");
            int baseY = data.getInt(key + ".baseY");
            arenas.add(new Arena(mode, w, ox, baseY));
        }
        plugin.getLogger().info("تم تحميل " + arenas.size() + " ملعبًا");
    }

    public boolean hasFree(GameMode mode) {
        return arenas.stream().anyMatch(a -> a.mode == mode && !a.inUse);
    }

    public Arena claim(GameMode mode) {
        for (Arena a : arenas)
            if (a.mode == mode && !a.inUse) { a.inUse = true; return a; }
        return null;
    }

    public void release(Arena a) { a.inUse = false; }
}

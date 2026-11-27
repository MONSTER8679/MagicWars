package com.example.magicwars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** نظام الطاقة السحرية Mana — سعة قصوى وشحن دوري قابل للضبط */
public class ManaManager {

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Integer> mana = new HashMap<>();

    public ManaManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int amount = plugin.getConfig().getInt("mana.regen-amount", 5);
        int period = plugin.getConfig().getInt("mana.regen-period-ticks", 40);
        new BukkitRunnable() {
            @Override public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    int cur = mana.getOrDefault(p.getUniqueId(), max());
                    mana.put(p.getUniqueId(), Math.min(cur + amount, max()));
                    updateDisplay(p);
                }
            }
        }.runTaskTimer(plugin, 0L, period);
    }

    public int max() {
        return plugin.getConfig().getInt("mana.max", 100);
    }

    public int current(Player p) {
        return mana.getOrDefault(p.getUniqueId(), max());
    }

    public boolean consume(Player p, int amount) {
        int cur = current(p);
        if (cur < amount) return false;
        mana.put(p.getUniqueId(), cur - amount);
        updateDisplay(p);
        return true;
    }

    public void refill(Player p) {
        mana.put(p.getUniqueId(), max());
        updateDisplay(p);
    }

    public void clear(UUID id) {
        mana.remove(id);
    }

    private void updateDisplay(Player p) {
        p.sendActionBar(net.kyori.adventure.text.Component.text(
                "✦ طاقة " + current(p) + "/" + max(),
                net.kyori.adventure.text.format.NamedTextColor.AQUA));
    }
}

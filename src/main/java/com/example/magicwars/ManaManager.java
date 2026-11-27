package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** نظام الطاقة السحرية: حد أقصى 100، تجديد تلقائي كل فترة */
public class ManaManager {

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Integer> mana = new HashMap<>();

    public ManaManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    public int max() {
        return plugin.getConfig().getInt("mana.max", 100);
    }

    public int mana(Player p) {
        return mana.getOrDefault(p.getUniqueId(), max());
    }

    public void setMana(Player p, int value) {
        mana.put(p.getUniqueId(), Math.max(0, Math.min(max(), value)));
    }

    public boolean spendMana(Player p, int cost) {
        if (mana(p) < cost) return false;
        setMana(p, mana(p) - cost);
        return true;
    }

    public void addMana(Player p, int amount) {
        setMana(p, mana(p) + amount);
    }

    public void clear(UUID id) {
        mana.remove(id);
    }

    private void startTask() {
        long period = plugin.getConfig().getLong("mana.regen-period-ticks", 40L);
        int amount = plugin.getConfig().getInt("mana.regen-amount", 5);
        new BukkitRunnable() {
            @Override public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (mana(p) < max()) addMana(p, amount);
                }
            }
        }.runTaskTimer(plugin, period, period);
    }
}

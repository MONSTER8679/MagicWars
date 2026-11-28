package com.petsim.combat;

import com.petsim.core.LogService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Phase 35: breakable resource objects (coins, chests, crystal piles).
 * BlockDisplay-based, per-area spawn, HP and rewards tracked server-side.
 */
public final class BreakableManager {

    public record Breakable(int id, Location loc, long hp, long maxHp, long coinReward, long diamondReward, BlockDisplay display) {}

    private final JavaPlugin plugin;
    private final LogService logs;
    private final List<Breakable> breakables = new ArrayList<>();
    private final Deque<Integer> freeIds = new ArrayDeque<>();
    private int nextId = 1;

    public BreakableManager(JavaPlugin plugin, LogService logs) {
        this.plugin = plugin;
        this.logs = logs;
    }

    /** Spawns a coin/chest breakable for an area. */
    public Breakable spawn(Location loc, long hp, long coins, long diamonds, Material look) {
        int id = freeIds.isEmpty() ? nextId++ : freeIds.pop();
        BlockDisplay display = loc.getWorld().spawn(loc, BlockDisplay.class, d -> {
            d.setBlock(look.createBlockData());
            d.setPersistent(false);
        });
        Breakable b = new Breakable(id, loc, hp, hp, coins, diamonds, display);
        breakables.add(b);
        return b;
    }

    /** Phase 36: applies damage from a player's pet team. Returns reward record when destroyed. */
    public Breakable damage(Breakable b, long amount) {
        if (b.hp() - amount <= 0) {
            destroy(b);
            logs.info("economy", "Breakable " + b.id() + " destroyed");
            return b;
        }
        breakables.set(breakables.indexOf(b), new Breakable(b.id(), b.loc(),
            b.hp() - amount, b.maxHp(), b.coinReward(), b.diamondReward(), b.display()));
        return null;
    }

    private void destroy(Breakable b) {
        b.display().remove();
        breakables.remove(b);
        freeIds.push(b.id());
    }

    public List<Breakable> all() { return List.copyOf(breakables); }
}

package com.petsim.pet;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;

import com.petsim.core.LogService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 30/31: pet follow system with ItemDisplay entities.
 * - formations (single, pairs, hex, ring)
 * - shared tick scheduler (no per-pet every-tick updates)
 * - floating/hover animations
 * - hide on demand, cleanup on quit
 */
public final class PetFollowService {

    private final JavaPlugin plugin;
    private final PetRegistry registry;
    private final LogService logs;
    private final Map<UUID, List<ItemDisplay>> active = new HashMap<>();
    private int task = -1;

    public PetFollowService(JavaPlugin plugin, PetRegistry registry, LogService logs) {
        this.plugin = plugin;
        this.registry = registry;
        this.logs = logs;
    }

    public void start(int intervalTicks) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, intervalTicks, intervalTicks).getTaskId();
    }

    public void stop() {
        if (task != -1) Bukkit.getScheduler().cancelTask(task);
        despawnAll();
    }

    /** Spawns follow displays for a player's equipped pets. */
    public void spawnFor(Player player) {
        despawn(player.getUniqueId());
        List<PetRecord> equipped = registry.instancesOf(player.getUniqueId())
            .stream().filter(PetRecord::equipped).toList();
        if (equipped.isEmpty()) return;

        List<ItemDisplay> displays = new ArrayList<>();
        Location base = player.getLocation();
        for (int i = 0; i < equipped.size(); i++) {
            ItemDisplay d = player.getWorld().spawn(base, ItemDisplay.class, display -> {
                display.setItemTransform(ItemDisplay.ItemTransform.HEAD);
                display.setPersistent(false); // Phase 70: no orphan entities
            });
            displays.add(d);
        }
        active.put(player.getUniqueId(), displays);
    }

    /** Phase 30: formation offsets. Positions pets behind and around the player. */
    private Location formationOffset(Player player, int index, int count) {
        double angle = (Math.PI * 2 / Math.max(count, 1)) * index + Math.toRadians(player.getLocation().getYaw());
        double radius = 1.2 + (count > 4 ? 0.8 : 0);
        return player.getLocation().add(
            -Math.sin(angle) * radius,
            1.0 + Math.sin((System.currentTimeMillis() / 700.0) + index) * 0.15, // Phase 31 floating
            Math.cos(angle) * radius);
    }

    private void tick() {
        for (Map.Entry<UUID, List<ItemDisplay>> entry : active.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) {
                entry.getValue().forEach(org.bukkit.entity.Display::remove);
                active.remove(entry.getKey());
                continue;
            }
            List<ItemDisplay> displays = entry.getValue();
            for (int i = 0; i < displays.size(); i++) {
                ItemDisplay d = displays.get(i);
                if (!d.isValid()) continue;
                d.teleport(formationOffset(p, i, displays.size()));
            }
        }
    }

    public void despawn(UUID playerId) {
        List<ItemDisplay> list = active.remove(playerId);
        if (list != null) list.forEach(Display::remove);
    }

    public void despawnAll() {
        active.values().forEach(l -> l.forEach(Display::remove));
        active.clear();
        logs.info("core", "All pet displays cleaned up");
    }
}

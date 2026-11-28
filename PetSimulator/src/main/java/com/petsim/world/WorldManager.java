package com.petsim.world;

import com.petsim.core.LogService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 8: world manager.
 * - creates/loads the game world with safe generator settings
 * - per-world spawn point and border
 * - never touches unrelated worlds (MagicWars etc.)
 */
public final class WorldManager {

    private final LogService logs;
    private final Map<String, World> loaded = new ConcurrentHashMap<>();

    public WorldManager(LogService logs) {
        this.logs = logs;
    }

    /** Creates or loads the game world. Safe to call on enable. */
    public World loadOrCreate(String worldName, int borderSize) {
        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            loaded.put(worldName, existing);
            return existing;
        }
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);
        World world = creator.createWorld();
        if (world == null) {
            throw new IllegalStateException("World creation failed for " + worldName);
        }
        world.getWorldBorder().setSize(borderSize);
        world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setTime(6000);
        loaded.put(worldName, world);
        logs.info("world_ops", "World ready: " + worldName + " border=" + borderSize);
        return world;
    }

    public World world(String name) {
        return loaded.get(name);
    }

    public List<World> all() {
        return List.copyOf(loaded.values());
    }

    /** Phase 15: soft protection checks are enforced by AreaProtection listener. */
    public boolean isGameWorld(World world) {
        return loaded.containsValue(world);
    }

    public void unloadAll() {
        for (Map.Entry<String, World> e : loaded.entrySet()) {
            Bukkit.unloadWorld(e.getValue(), true);
        }
        loaded.clear();
    }
}

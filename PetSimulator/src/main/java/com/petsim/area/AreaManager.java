package com.petsim.area;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;
import com.petsim.world.AreaDefinition;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 13/14: area manager + server-side unlock purchases.
 * Purchase is validated server-side: required area, price, no client input trusted.
 */
public final class AreaManager {

    private final DatabaseService db;
    private final LogService logs;
    private final Map<String, AreaDefinition> areas = new ConcurrentHashMap<>();

    public AreaManager(DatabaseService db, LogService logs) {
        this.db = db;
        this.logs = logs;
    }

    public void register(AreaDefinition area) {
        area.validate();
        areas.put(area.id(), area);
    }

    public AreaDefinition area(String id) { return areas.get(id); }
    public List<AreaDefinition> all() { return List.copyOf(areas.values()); }
    public List<AreaDefinition> inWorld(String worldId) {
        List<AreaDefinition> out = new ArrayList<>();
        for (AreaDefinition a : areas.values()) {
            if (a.worldId().equals(worldId)) out.add(a);
        }
        return out;
    }

    public boolean isUnlocked(UUID player, String worldId, String areaId) {
        try {
            return db.queryLong(
                "SELECT COUNT(*) FROM player_areas WHERE uuid=? AND world_id=? AND area_id=?",
                player.toString(), worldId, areaId) > 0;
        } catch (SQLException e) {
            logs.severe("database", "isUnlocked failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Phase 14: server-side unlock. Caller must already have verified funds
     * via EconomyManager.withdraw; this registers the unlock only when the
     * requirement chain is satisfied. Returns a failure reason or null on success.
     */
    public String tryUnlock(UUID player, String worldId, String areaId) {
        AreaDefinition def = areas.get(areaId);
        if (def == null) return "unknown-area";
        if (!def.worldId().equals(worldId)) return "area-not-in-world";
        if (isUnlocked(player, worldId, areaId)) return "already-unlocked";
        if (def.requiredAreaId() != null && !isUnlocked(player, worldId, def.requiredAreaId())) {
            return "requires-previous-area";
        }
        try {
            db.execute(
                "INSERT OR IGNORE INTO player_areas(uuid, world_id, area_id, unlocked_at) VALUES(?,?,?,?)",
                player.toString(), worldId, areaId, System.currentTimeMillis());
            logs.info("player_actions", "Area unlocked: " + areaId);
            return null;
        } catch (SQLException e) {
            logs.severe("database", "tryUnlock failed: " + e.getMessage());
            return "database-error";
        }
    }
}

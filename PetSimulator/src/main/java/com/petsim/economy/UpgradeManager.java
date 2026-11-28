package com.petsim.economy;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Phase 39/40: economy upgrades. Prices follow a quadratic curve
 * (not too fast, not impossible — Phase 40 balancing rule).
 */
public final class UpgradeManager {

    public enum UpgradeType {
        COIN_BOOST, DIAMOND_BOOST, PET_DAMAGE, HATCH_LUCK,
        HATCH_SPEED, STORAGE_CAPACITY, EQUIP_SLOTS, MOVEMENT_SPEED, COLLECTION_RADIUS
    }

    public static final int MAX_LEVEL = 20;

    private final DatabaseService db;
    private final LogService logs;

    public UpgradeManager(DatabaseService db, LogService logs) {
        this.db = db;
        this.logs = logs;
    }

    /** Quadratic cost curve (Phase 40). */
    public long price(UpgradeType type, int nextLevel) {
        double base = switch (type) {
            case COIN_BOOST, DIAMOND_BOOST -> 500;
            case PET_DAMAGE -> 750;
            case HATCH_LUCK, HATCH_SPEED -> 1200;
            case STORAGE_CAPACITY, EQUIP_SLOTS -> 2000;
            case MOVEMENT_SPEED, COLLECTION_RADIUS -> 900;
        };
        return (long) (base * Math.pow(nextLevel, 2));
    }

    public int level(UUID player, UpgradeType type) {
        try {
            return (int) db.queryLong(
                "SELECT COUNT(*) FROM player_upgrades WHERE uuid=? AND upgrade_id=?",
                player.toString(), type.name());
        } catch (SQLException e) {
            logs.severe("database", "level query failed: " + e.getMessage());
            return 0;
        }
    }

    /** Purchases one level. Funds must be withdrawn by the caller first. */
    public String buy(UUID player, UpgradeType type) {
        int current = level(player, type);
        if (current >= MAX_LEVEL) return "max-level";
        try {
            db.execute(
                "INSERT INTO player_upgrades(uuid, upgrade_id, purchased_at) VALUES(?,?,?)",
                player.toString(), type.name(), System.currentTimeMillis());
            logs.info("economy", "Upgrade purchased: " + type + " level " + (current + 1));
            return null;
        } catch (SQLException e) {
            logs.severe("database", "buy failed: " + e.getMessage());
            return "database-error";
        }
    }

    /** Bonus multiplier granted by a level. */
    public double multiplier(UpgradeType type, int level) {
        double step = switch (type) {
            case COIN_BOOST, DIAMOND_BOOST -> 0.10;
            default -> 0.05;
        };
        return 1.0 + (level * step);
    }
}

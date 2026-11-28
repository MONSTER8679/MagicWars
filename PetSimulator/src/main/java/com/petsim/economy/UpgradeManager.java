package com.petsim.economy;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Phase 39/40: economy upgrades. Prices and bonuses live in code constants
 * mapped later to upgrades.yml; curve is quadratic to prevent fast maxing.
 */
public final class UpgradeManager {

    public enum UpgradeType {
        COIN_BOOST, DIAMOND_BOOST, PET_DAMAGE, HATCH_LUCK,
        HATCH_SPEED, STORAGE_CAPACITY, EQUIP_SLOTS, MOVEMENT_SPEED, COLLECTION_RADIUS
    }

    private final DatabaseService db;
    private final LogService logs;

    public UpgradeManager(DatabaseService db, LogService logs) {
        this.db = db;
        this.logs = logs;
    }

    /** Quadratic cost curve (Phase 40: not too fast, not impossible). */
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
            return 0;
        }
    }

    public String buy(UUID player, UpgradeType type, int maxLevel) {
        int current = level(player, type);
        if (current >= maxLevel) return "max-level";
        try {
            db.execute(
                "INSERT INTO player_upgrades(uuid, upgrade_id, purchased_at) VALUES(?,?,?)",
                player.toString(), type.name(), System.currentTimeMillis());
            logs.info("economy", "Upgrade purchased: " + type);
            return null;
        } catch (SQLException e) {
            logs.severe("database", "buy failed: " + e.getMessage());
            return "database-error";
        }
    }

    public double multiplier(UpgradeType type, int level) {
        return 1.0 + (level * switch (type) {
            case COIN_BOINTS_PLACEHOLDER -> 0.10;
            default -> 0.05;
        });
    }
}

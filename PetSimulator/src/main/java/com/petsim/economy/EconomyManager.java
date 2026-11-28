package com.petsim.economy;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Phase 34: central economy, multi-currency, server-authoritative.
 * All mutations go through the DB so balances survive restarts and
 * cannot be double-spent (updates are conditional and checked).
 */
public final class EconomyManager {

    public enum Currency { COINS, DIAMONDS, GEMS }

    private final DatabaseService db;
    private final LogService logs;

    public EconomyManager(DatabaseService db, LogService logs) {
        this.db = db;
        this.logs = logs;
    }

    public void ensurePlayer(UUID player) {
        try {
            int rows = db.execute(
                "INSERT OR IGNORE INTO players(uuid, name, last_seen) VALUES (?, ?, ?)",
                player.toString(), "unknown", System.currentTimeMillis());
            if (rows > 0) logs.info("economy", "Wallet created for new player");
        } catch (SQLException e) {
            logs.severe("economy", "ensurePlayer failed: " + e.getMessage());
        }
    }

    public long balance(UUID player, Currency cur) {
        String col = switch (cur) {
            case COINS -> "coins";
            case DIAMONDS -> "diamonds";
            case GEMS -> "gems";
        };
        try {
            return db.queryLong("SELECT " + col + " FROM players WHERE uuid = ?", player.toString());
        } catch (SQLException e) {
            logs.severe("economy", "balance query failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Adds currency. Negative amounts rejected. Returns new balance,
     * or -1 on failure (never throws to callers — economy must not crash the tick).
     */
    public long add(UUID player, Currency cur, long amount) {
        if (amount < 0) {
            logs.security("Rejected negative add for " + player);
            return -1;
        }
        String col = column(cur);
        try {
            db.execute("UPDATE players SET " + col + " = " + col + " + ? WHERE uuid = ?",
                amount, player.toString());
            long updated = balance(player, cur);
            logs.info("economy", "Added " + amount + " " + cur + ", new balance " + updated);
            return updated;
        } catch (SQLException e) {
            logs.severe("economy", "add failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Atomically withdraws if and only if the balance covers it.
     * Returns true on success, false if funds insufficient or DB error.
     */
    public boolean withdraw(UUID player, Currency cur, long amount) {
        if (amount < 0) {
            logs.security("Rejected negative withdraw for " + player);
            return false;
        }
        String col = column(cur);
        try {
            int rows = db.execute(
                "UPDATE players SET " + col + " = " + col + " - ? WHERE uuid = ? AND " + col + " >= ?",
                amount, player.toString(), amount);
            if (rows == 0) return false;
            logs.info("economy", "Withdrew " + amount + " " + cur);
            return true;
        } catch (SQLException e) {
            logs.severe("economy", "withdraw failed: " + e.getMessage());
            return false;
        }
    }

    private String column(Currency cur) {
        return switch (cur) {
            case COINS -> "coins";
            case DIAMONDS -> "diamonds";
            case GEMS -> "gems";
        };
    }

    /** Phase 33: server-side pet valuation (base by rarity, scaled by level). */
    public BigDecimal petTradeValue(int rarityWeight, long basePower, int level) {
        BigDecimal rarity = BigDecimal.valueOf(rarityWeight).max(BigDecimal.ONE);
        BigDecimal power = BigDecimal.valueOf(basePower).max(BigDecimal.ONE);
        BigDecimal lvl = BigDecimal.valueOf(1 + (level - 1) * 0.05);
        return rarity.multiply(power).multiply(lvl);
    }
}

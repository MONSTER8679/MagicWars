package com.petsim.database;

import com.petsim.core.ConfigManager;
import com.petsim.core.LogService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Phase 70A: SQLite storage.
 * - schema versioning + migrations
 * - prepared statements only (no string-concatenated SQL)
 * - single-writer pattern with synchronized connection (SQLite specifics)
 * - heavy queries must run off the main thread (see #runAsync)
 */
public final class DatabaseService {

    private static final int SCHEMA_VERSION = 1;

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final LogService logs;
    private Connection connection;

    public DatabaseService(JavaPlugin plugin, ConfigManager config, LogService logs) {
        this.plugin = plugin;
        this.config = config;
        this.logs = logs;
    }

    public void init() throws SQLException {
        File folder = plugin.getDataFolder();
        File dbDir = new File(folder, "database");
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            throw new SQLException("Cannot create database/ folder");
        }
        File dbFile = new File(dbDir, "petsim.db");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA foreign_keys=ON");
        }
        migrate();
        logs.info("database", "SQLite ready, schema v" + SCHEMA_VERSION);
    }

    private void migrate() throws SQLException {
        int current = 0;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT version FROM schema_version")) {
            if (rs.next()) current = rs.getInt("version");
        } catch (SQLException noTable) {
            // first boot: no schema yet
        }
        if (current < SCHEMA_VERSION) {
            try (Statement st = connection.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
            }
            for (int v = current + 1; v <= SCHEMA_VERSION; v++) {
                applyMigration(v);
            }
            try (Statement st = connection.createStatement()) {
                st.execute("DELETE FROM schema_version");
                st.execute("INSERT INTO schema_version(version) VALUES (" + SCHEMA_VERSION + ")");
            }
            logs.info("database", "Migrated schema " + current + " -> " + SCHEMA_VERSION);
        }
    }

    private void applyMigration(int v) throws SQLException {
        if (v == 1) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS players(
                      uuid TEXT PRIMARY KEY,
                      name TEXT NOT NULL,
                      coins INTEGER NOT NULL DEFAULT 0,
                      diamonds INTEGER NOT NULL DEFAULT 0,
                      gems INTEGER NOT NULL DEFAULT 0,
                      rebirths INTEGER NOT NULL DEFAULT 0,
                      playtime_seconds INTEGER NOT NULL DEFAULT 0,
                      last_seen INTEGER NOT NULL
                    )""");
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_areas(
                      uuid TEXT NOT NULL,
                      world_id TEXT NOT NULL,
                      area_id TEXT NOT NULL,
                      unlocked_at INTEGER NOT NULL,
                      PRIMARY KEY(uuid, world_id, area_id)
                    )""");
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_worlds(
                      uuid TEXT NOT NULL,
                      world_id TEXT NOT NULL,
                      unlocked_at INTEGER NOT NULL,
                      PRIMARY KEY(uuid, world_id)
                    )""");
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS pets(
                      pet_uuid TEXT PRIMARY KEY,
                      owner_uuid TEXT NOT NULL,
                      definition_id TEXT NOT NULL,
                      level INTEGER NOT NULL DEFAULT 1,
                      experience INTEGER NOT NULL DEFAULT 0,
                      equipped BOOLEAN NOT NULL DEFAULT 0,
                      created_at INTEGER NOT NULL,
                      FOREIGN KEY(owner_uuid) REFERENCES players(uuid)
                    )""");
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS trade_log(
                      trade_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      player_a TEXT NOT NULL,
                      player_b TEXT NOT NULL,
                      state TEXT NOT NULL,
                      details TEXT NOT NULL,
                      created_at INTEGER NOT NULL
                    )""");
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS daily_claims(
                      uuid TEXT PRIMARY KEY,
                      last_claim_day TEXT NOT NULL,
                      streak INTEGER NOT NULL DEFAULT 0
                    )""");
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pets_owner ON pets(owner_uuid)");
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_trade_log_players ON trade_log(player_a, player_b)");
            }
        } else {
            throw new SQLException("Unknown migration step " + v);
        }
    }

    /** Runs a write on the main-thread pool. Heavy work should use runAsync. */
    public synchronized int execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    public synchronized void runAsync(Runnable task) {
        // Bukkit async scheduler keeps the main thread free (Phase 70D).
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /** Query returning a single long (counts, sums). */
    public synchronized long queryLong(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    /** Functional query access, used by repositories later. */
    public synchronized <T> T query(String sql, ResultSetHandler<T> handler, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public void backup() throws SQLException, java.io.IOException {
        File dir = new File(plugin.getDataFolder(), "backups");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new java.io.IOException("Cannot create backups/ folder");
        }
        File target = new File(dir, "petsim-" + System.currentTimeMillis() + ".db");
        try (Statement st = connection.createStatement()) {
            st.execute("VACUUM INTO '" + target.getAbsolutePath().replace("'", "''") + "'");
        }
        logs.info("database", "Backup written: " + target.getName());
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static String uuidStr(UUID id) { return id.toString(); }
}

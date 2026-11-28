package com.petsim.core;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Phase 7: separate logging channels.
 * Channels map to files under logs/ later; for now they tag every record.
 * Never logs sensitive data (no IPs, no tokens).
 */
public final class LogService {

    public static final String[] CHANNELS = {
        "core", "player_actions", "economy", "trading", "hatching",
        "admin", "database", "security", "world_ops", "performance"
    };

    private final Logger base;

    public LogService(JavaPlugin plugin, ConfigManager config) {
        this.base = plugin.getLogger();
    }

    public void info(String channel, String message) {
        base.log(Level.INFO, "[" + channel + "] " + message);
    }

    public void warn(String channel, String message) {
        base.log(Level.WARNING, "[" + channel + "] " + message);
    }

    public void severe(String channel, String message) {
        base.log(Level.SEVERE, "[" + channel + "] " + message);
    }

    /** Security events always logged at warning or above. */
    public void security(String message) { warn("security", message); }

    public void reload(ConfigManager config) {
        info("core", "Log channels reloaded");
    }

    public void flush() {
        // File handlers (if any) flush; console logger needs nothing.
    }
}

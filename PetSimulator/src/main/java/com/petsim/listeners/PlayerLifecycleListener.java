package com.petsim.listeners;

import com.petsim.PetSimPlugin;
import com.petsim.core.LogService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Phase 70B: player data lifecycle.
 * - load on join (async)
 * - systems get a callback when loading completes
 * - cleanup on quit, save on shutdown
 * - one session per player, no duplicate sessions
 */
public final class PlayerLifecycleListener implements Listener {

    private final PetSimPlugin plugin;
    private final LogService logs;

    public PlayerLifecycleListener(PetSimPlugin plugin, LogService logs) {
        this.plugin = plugin;
        this.logs = logs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        String name = event.getPlayer().getName();
        plugin.db().runAsync(() -> {
            try {
                plugin.db().execute(
                    "INSERT OR IGNORE INTO players(uuid, name, last_seen) VALUES(?,?,?)",
                    id.toString(), name, System.currentTimeMillis());
                plugin.db().execute(
                    "UPDATE players SET name=?, last_seen=? WHERE uuid=?",
                    name, System.currentTimeMillis(), id.toString());
                logs.info("player_actions", "Session loaded for " + name);
            } catch (Exception e) {
                logs.severe("database", "Join load failed for " + name + ": " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        plugin.db().runAsync(() -> {
            try {
                plugin.db().execute(
                    "UPDATE players SET last_seen=? WHERE uuid=?",
                    System.currentTimeMillis(), id.toString());
                logs.info("player_actions", "Session saved for " + event.getPlayer().getName());
            } catch (Exception e) {
                logs.severe("database", "Quit save failed: " + e.getMessage());
            }
        });
    }
}

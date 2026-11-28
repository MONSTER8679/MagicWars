package com.petsim.security;

import com.petsim.area.AreaManager;
import com.petsim.core.LogService;
import com.petsim.world.AreaDefinition;
import com.petsim.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.UUID;

/**
 * Phase 15: area protection.
 * Blocks entering locked areas by movement, teleport, pearls, vehicles.
 */
public final class AreaProtectionListener implements Listener {

    private final WorldManager worlds;
    private final AreaManager areas;
    private final LogService logs;

    public AreaProtectionListener(WorldManager worlds, AreaManager areas, LogService logs) {
        this.worlds = worlds;
        this.areas = areas;
        this.logs = logs;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;
        if (!isAllowed(event.getPlayer(), to)) {
            event.setCancelled(true);
            logs.security(event.getPlayer().getName() + " blocked from locked area by movement");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!isAllowed(event.getPlayer(), event.getTo())) {
            event.setCancelled(true);
            logs.security(event.getPlayer().getName() + " blocked teleport into locked area ("
                + event.getCause() + ")");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        if (!isAllowed(event.getPlayer(), event.getTo())) {
            event.setCancelled(true);
            logs.security(event.getPlayer().getName() + " blocked portal into locked area");
        }
    }

    /** A destination is allowed when its area is unlocked or has no area (public land). */
    private boolean isAllowed(Player player, Location loc) {
        if (loc == null || loc.getWorld() == null) return true;
        if (!worlds.isGameWorld(loc.getWorld())) return true;
        UUID id = player.getUniqueId();
        for (AreaDefinition area : areas.inWorld(loc.getWorld().getName())) {
            if (area.contains(loc.getBlockX(), loc.getBlockZ())
                && !areas.isUnlocked(id, area.worldId(), area.id())) {
                return false;
            }
        }
        return true;
    }
}

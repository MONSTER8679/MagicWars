package com.example.magicwars;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** ربط الدخول/الخروج بأنظمة البلوقن */
public class PlayerListener implements Listener {

    private final MagicWarsPlugin plugin;

    public PlayerListener(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.manaManager().refill(e.getPlayer());
        if (plugin.lobby().lobby() != null && !e.getPlayer().hasPlayedBefore())
            e.getPlayer().teleport(plugin.lobby().lobby());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.manaManager().clear(e.getPlayer().getUniqueId());
    }
}

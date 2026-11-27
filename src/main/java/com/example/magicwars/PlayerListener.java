package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** مراقب اللاعبين: دخول وخروج */
public class PlayerListener implements Listener {

    private final MagicWarsPlugin plugin;

    public PlayerListener(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.hasPlayedBefore()) return;
        p.sendMessage(net.kyori.adventure.text.Component.text(
                "مرحبًا بك في MagicWars! ✦ جرّب /play solo أو اضغط NPC الدخول",
                net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.gameManager().quit(e.getPlayer());
        plugin.manaManager().clear(e.getPlayer().getUniqueId());
    }
}

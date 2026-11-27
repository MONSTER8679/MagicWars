package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** نقاط إحياء داخل الملاعب: يعود اللاعب بعد ثانية إلى منصة فريقه */
public class SpawnPointManager implements Listener {

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Long> respawnLock = new HashMap<>();

    public SpawnPointManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        GameManager.Match m = plugin.gameManager().matchOf(p);
        if (m == null) return;
        // (موضع مستقبلي لتخصيص منصات إحياء فريدة)
    }
}

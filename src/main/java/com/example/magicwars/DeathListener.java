package com.example.magicwars;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/** معالجة القتل: عملات + خبرة + تأثير تجميلي */
public class DeathListener implements Listener {

    private final MagicWarsPlugin plugin;

    public DeathListener(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        var victim = e.getPlayer();
        var killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        int coins = plugin.getConfig().getInt("economy.coins-per-kill", 5);
        int xp = plugin.getConfig().getInt("economy.xp-per-kill", 10);
        plugin.stats().addCoins(killer.getUniqueId(), coins);
        plugin.stats().addXp(killer.getUniqueId(), xp);

        plugin.killEffects().play(killer, victim.getLocation());
    }
}

package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/** مراقب الوفيات: تسجيل القتلات وتوزيع المكافآت */
public class DeathListener implements Listener {

    private final MagicWarsPlugin plugin;

    public DeathListener(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) {
            // موت بلا قاتل (سقوط/حمم) — إعادة ظهور فقط
            plugin.gameManager().handleKill(victim, victim);   // لا يوجد خصم هنا
            return;
        }
        plugin.gameManager().handleKill(victim, killer);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        // منع الضرر بين أعضاء نفس الفريق
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        var m = plugin.gameManager().findMatch(victim.getUniqueId());
        if (m == null) return;
        if (m.players.get(victim.getUniqueId()) != null
                && m.players.get(victim.getUniqueId())
                   .equals(m.players.get(damager.getUniqueId()))) {
            e.setCancelled(true);
        }
    }
}

package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/** القدرة القصوى (Ultimate): تُشحن بالقتلات وتُطلق بضغطة */
public class UltimateManager implements org.bukkit.event.Listener {

    private final MagicWarsPlugin plugin;

    public UltimateManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    /** شحن القدرة القصوى: كل 3 قتلات تفتح ضغطة واحدة */
    public void start() {
        new BukkitRunnable() {
            @Override public void run() {
                // إشعار بالحالة في شريط الحركة
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    int kills = plugin.stats().getKillsSinceUltimate(p.getUniqueId());
                    if (kills >= 3) {
                        p.sendActionBar(net.kyori.adventure.text.Component.text(
                                "⭐ القدرة القصوى جاهزة! (Sneak + Right-click)",
                                net.kyori.adventure.text.format.NamedTextColor.GOLD));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }

    /** إطلاق القدرة: انفجار هائل حول اللاعب + شفاء */
    public void fireUltimate(Player p) {
        if (plugin.stats().getKillsSinceUltimate(p.getUniqueId()) < 3) {
            p.sendActionBar(net.kyori.adventure.text.Component.text(
                    "⭐ تحتاج 3 قتلات لشحن القدرة",
                    net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }
        plugin.stats().resetKillsSinceUltimate(p.getUniqueId());

        org.bukkit.Location c = p.getLocation();
        SpellEffects.expandingRings(plugin, c, org.bukkit.Particle.FIREWORK, 5, 8);
        SpellEffects.beamPillar(c, 15, org.bukkit.Particle.END_ROD, 4);
        SpellEffects.dome(c, 6, org.bukkit.Particle.ENCHANT);
        c.getWorld().playSound(c, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.8f);

        for (org.bukkit.entity.Entity e : c.getWorld().getNearbyEntities(c, 8, 4, 8)) {
            if (e instanceof org.bukkit.entity.LivingEntity le && !e.equals(p)) {
                le.damage(15.0, p);
                org.bukkit.util.Vector v = le.getLocation().toVector()
                        .subtract(c.toVector()).normalize().multiply(1.5).setY(0.7);
                le.setVelocity(v);
            }
        }
        p.setHealth(Math.min(p.getHealth() + 10,
                p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
    }
}

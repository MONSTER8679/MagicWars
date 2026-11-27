package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * جزيئات جو دائمة أثناء المباراة:
 * - رونات عائمة حول المسلة المركزية
 * - هالات تتبع اللاعبين بلون الفريق
 */
public class ArenaAmbience extends BukkitRunnable {

    private final MagicWarsPlugin plugin;
    private final GameManager.Match match;
    private double tick = 0;
    private int life = 0;
    private static final int MAX_LIFE = 20 * 60 * 10; // أمان: توقف بعد 10 دقائق

    public ArenaAmbience(MagicWarsPlugin plugin, GameManager.Match match) {
        this.plugin = plugin;
        this.match = match;
    }

    @Override
    public void run() {
        if (!plugin.gameManager().matches().contains(match) || ++life > MAX_LIFE) {
            cancel();
            return;
        }
        tick += 0.1;
        World w = match.arena.world;
        Location c = match.arena.center;

        // رونات تدور حول المسلة المركزية (3 حلقات بارتفاعات مختلفة)
        for (int ring = 0; ring < 3; ring++) {
            double r = 4 + ring * 2.5;
            double a = tick * (1 + ring * 0.4) + ring;
            w.spawnParticle(Particle.ENCHANT,
                    c.getX() + r * Math.cos(a), c.getY() + 6 + ring * 2,
                    c.getZ() + r * Math.sin(a), 1, 0, 0, 0, 0.3);
        }

        // شرارات من المسلة
        w.spawnParticle(Particle.END_ROD, c.getX(), c.getY() + 10, c.getZ(),
                2, 0.2, 0.2, 0.2, 0.02);

        // هالة خفيفة تحت كل لاعب حي
        for (java.util.UUID id : match.players.keySet()) {
            Player p = plugin.getServer().getPlayer(id);
            if (p == null || !p.isOnline() || p.isDead()) continue;
            boolean red = match.players.get(id) == GameManager.Team.RED;
            SpellEffects.circle(p.getLocation(), 0.8, 6,
                    red ? Particle.CRIT : Particle.SPLASH, 0.1, 0);
        }
    }
}

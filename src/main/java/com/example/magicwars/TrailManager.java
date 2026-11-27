package com.example.magicwars;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/** آثار جزيئية خلف خطوات اللاعب — تُشترى بالعملات وتُختار */
public class TrailManager implements Listener {

    public enum Trail {
        FLAME("flame", "لهيب", Particle.FLAME),
        HEARTS("hearts", "قلوب", Particle.HEART),
        NOTES("notes", "نغمات", Particle.NOTE),
        MAGIC("magic", "تعويذة", Particle.ENCHANT),
        SOULS("souls", "أرواح", Particle.SOUL);

        public final String id, name;
        public final Particle particle;
        Trail(String id, String name, Particle particle) {
            this.id = id; this.name = name; this.particle = particle;
        }
        public static Trail byId(String s) {
            for (Trail t : values()) if (t.id.equalsIgnoreCase(s)) return t;
            return null;
        }
    }

    private final MagicWarsPlugin plugin;

    public TrailManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        Trail t = Trail.byId(plugin.stats().selectedCosmetic(p.getUniqueId(), "trail"));
        if (t == null) return;
        p.getWorld().spawnParticle(t.particle,
                p.getLocation().getX(), p.getLocation().getY() + 0.15,
                p.getLocation().getZ(), 2, 0.15, 0.05, 0.15, 0.01);
    }
}

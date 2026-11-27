package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.UUID;

/** مستحضرات تجميلية تظهر عند كل قتلة — تُشترى بالعملات */
public class KillEffectsManager {

    public enum KillEffect {
        SOUL_BURST("soul", "انفجار الأرواح"),
        FLAME_BURST("flame", "انفجار اللهب"),
        FROST_NOVA("frost", "صدمة جليدية"),
        VOID_IMPLODE("void", "انهيار العدم"),
        GOLD_RAIN("gold", "مطر ذهبي");

        public final String id, name;
        KillEffect(String id, String name) { this.id = id; this.name = name; }
        public static KillEffect byId(String s) {
            for (KillEffect k : values()) if (k.id.equalsIgnoreCase(s)) return k;
            return null;
        }
    }

    private final MagicWarsPlugin plugin;

    public KillEffectsManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void play(Player killer, Location at) {
        String sel = plugin.stats().selectedCosmetic(killer.getUniqueId(), "kill");
        KillEffect e = KillEffect.byId(sel);
        if (e == null) return;
        switch (e) {
            case SOUL_BURST -> {
                SpellEffects.burst(at, Particle.SOUL, 25, 0.15);
                SpellEffects.burst(at, Particle.WITCH, 15, 0.1);
                at.getWorld().playSound(at, Sound.PARTICLE_SOUL_ESCAPE, 1.5f, 0.6f);
            }
            case FLAME_BURST -> {
                SpellEffects.burst(at, Particle.FLAME, 30, 0.2);
                SpellEffects.circle(at, 1.5, 20, Particle.LAVA, 0.2, 0.02);
                at.getWorld().playSound(at, Sound.ENTITY_BLAZE_DEATH, 1.5f, 0.8f);
            }
            case FROST_NOVA -> {
                SpellEffects.expandingRings(plugin, at, Particle.SNOWFLAKE, 3, 3.5);
                SpellEffects.burst(at, Particle.SNOWFLAKE, 20, 0.1);
                at.getWorld().playSound(at, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
            }
            case VOID_IMPLODE -> {
                SpellEffects.vortex(plugin, at, Particle.PORTAL, 20);
                at.getWorld().playSound(at, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.8f);
            }
            case GOLD_RAIN -> {
                SpellEffects.rain(at, Particle.END_ROD, 2, 40);
                SpellEffects.burst(at, Particle.WAX_ON, 20, 0.1);
                at.getWorld().playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.8f);
            }
        }
    }
}

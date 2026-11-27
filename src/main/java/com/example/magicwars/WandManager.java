package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** منطق إطلاق العصي السحرية: كل عصا لها تعويذة خاصة */
public class WandManager {

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public WandManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    /** نقطة الاستهداف: حيث ينظر اللاعب (حتى maxDist) */
    private Location rayTarget(Player p, double maxDist) {
        var ray = p.getWorld().rayTraceEntities(p.getEyeLocation(),
                p.getEyeLocation().getDirection(), maxDist, 1.0,
                e -> e instanceof LivingEntity && !e.equals(p));
        if (ray != null && ray.getHitEntity() != null)
            return ray.getHitEntity().getLocation();
        var blockHit = p.getWorld().rayTraceBlocks(p.getEyeLocation(),
                p.getEyeLocation().getDirection(), maxDist);
        if (blockHit != null && blockHit.getHitBlock() != null)
            return blockHit.getHitBlock().getLocation().add(0.5, 1, 0.5);
        return p.getEyeLocation().add(p.getEyeLocation().getDirection()
                .multiply(maxDist));
    }

    public boolean cast(Player p, WandType wand) {
        Map<String, Long> cd = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>());
        long now = System.currentTimeMillis();
        long left = cd.getOrDefault(wand.id, 0L) - now;
        if (left > 0) {
            p.sendActionBar(Component.text("⏳ " + String.format("%.1f", left / 1000.0) + " ث",
                    NamedTextColor.GRAY));
            return false;
        }
        if (!plugin.manaManager().spendMana(p, wand.manaCost)) {
            p.sendActionBar(Component.text("❌ طاقة غير كافية (" + wand.manaCost + ")",
                    NamedTextColor.RED));
            return false;
        }
        cd.put(wand.id, now + (long) (wand.cooldownSec * 1000));

        switch (wand) {
            case FIRE_STAFF     -> castFire(p);
            case ICE_WAND      -> castIce(p);
            case LIGHTNING_WAND-> castLightning(p);
            case HEAL_WAND     -> castHeal(p);
            case BLINK_WAND    -> castBlink(p);
            case WIND_WAND     -> castWind(p);
            case VOID_WAND     -> castVoid(p);
            case STORM_WAND    -> castStorm(p);
            case DRAGON_WAND   -> castDragon(p);
            case TIME_WAND     -> castTime(p);
            case METEOR_WAND   -> castMeteor(p);
        }
        return true;
    }

    private void castFire(Player p) {
        Location t = rayTarget(p, 20);
        p.launchProjectile(org.bukkit.entity.Fireball.class);
        SpellEffects.burst(t, Particle.FLAME, 12, 0.2);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
    }

    private void castIce(Player p) {
        Location t = rayTarget(p, 16);
        SpellEffects.circle(t, 2, 16, Particle.SNOWFLAKE, 0.1, 0.1);
        p.getWorld().playSound(t, Sound.BLOCK_GLASS_BREAK, 1f, 1.4f);
        for (Entity e : p.getWorld().getNearbyEntities(t, 3, 2, 3)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                le.damage(4.0, p);
            }
        }
    }

    private void castLightning(Player p) {
        Location t = rayTarget(p, 30);
        p.getWorld().strikeLightning(t);
        SpellEffects.beamPillar(t, 3, Particle.ELECTRIC_SPARK, 2);
    }

    private void castHeal(Player p) {
        p.setHealth(Math.min(p.getHealth() + 8.0, 20.0));
        SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.HEART, 10, 0.3);
        SpellEffects.dome(p.getLocation(), 2, Particle.END_ROD);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.6f);
    }

    private void castBlink(Player p) {
        Location t = rayTarget(p, 12);
        SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.PORTAL, 20, 0.2);
        p.teleport(t);
        SpellEffects.burst(t.clone().add(0, 1, 0), Particle.PORTAL, 20, 0.2);
        p.getWorld().playSound(t, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
    }

    private void castWind(Player p) {
        Location t = rayTarget(p, 12);
        SpellEffects.groundWave(plugin, p.getLocation().add(0, 0.2, 0),
                p.getLocation().getDirection(), Particle.CLOUD, 8);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BREEZE_IDLE_GROUND, 1.5f, 0.8f);
        for (Entity e : p.getWorld().getNearbyEntities(t, 4, 3, 4)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                var dir = e.getLocation().toVector()
                        .subtract(p.getLocation().toVector()).normalize().multiply(1.5).setY(0.6);
                le.setVelocity(dir);
                le.damage(3.0, p);
            }
        }
    }

    private void castVoid(Player p) {
        Location t = rayTarget(p, 16);
        SpellEffects.blackHole(t, 3, Particle.PORTAL, 40);
        SpellEffects.vortex(plugin, t, Particle.SQUID_INK, 25);
        p.getWorld().playSound(t, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.4f);
        for (Entity e : p.getWorld().getNearbyEntities(t, 6, 3, 6)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                var pull = t.toVector().subtract(e.getLocation().toVector())
                        .normalize().multiply(0.8);
                le.setVelocity(pull);
                le.damage(7.0, p);
            }
        }
    }

    private void castStorm(Player p) {
        Location target = rayTarget(p, 14);
        SpellEffects.tornado(target, 6, 2.5, Particle.CLOUD);
        SpellEffects.beamPillar(target, 6, Particle.ELECTRIC_SPARK, 2);
        p.getWorld().playSound(target, Sound.ENTITY_BREEZE_IDLE_GROUND, 2f, 0.7f);
        p.getWorld().playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.6f);
        for (Entity e : p.getWorld().getNearbyEntities(target, 3, 5, 3)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                le.setVelocity(le.getVelocity().setY(1.1));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                        60, 3, false, true, true));
                le.damage(6.0, p);
            }
        }
    }

    private void castDragon(Player p) {
        Location eye = p.getEyeLocation();
        var dir = eye.getDirection().normalize();
        SpellEffects.cone(eye, dir, 9, 3.0, Particle.FLAME, 20);
        SpellEffects.cone(eye, dir, 9, 2.5, Particle.DRAGON_BREATH, 14);
        SpellEffects.cone(eye, dir, 7, 1.5, Particle.LAVA, 10);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 1.5f);
        for (double d = 1; d <= 9; d += 1.5) {
            Location step = eye.clone().add(dir.clone().multiply(d));
            double spread = 3.0 * (d / 9);
            for (Entity e : p.getWorld().getNearbyEntities(step, spread, spread, spread)) {
                if (e instanceof LivingEntity le && !e.equals(p)) {
                    le.damage(9.0, p);
                    le.setFireTicks(80);
                    SpellEffects.burst(le.getLocation().add(0, 1, 0), Particle.FLAME, 10, 0.1);
                }
            }
        }
    }

    private void castTime(Player p) {
        Location t = rayTarget(p, 14);
        SpellEffects.vortex(plugin, t, Particle.WITCH, 30);
        SpellEffects.glyph(t);
        SpellEffects.circle(t, 5, 40, Particle.SNOWFLAKE, 0.15, 0);
        p.getWorld().playSound(t, Sound.BLOCK_BEACON_ACTIVATE, 2f, 0.5f);
        for (Entity e : p.getWorld().getNearbyEntities(t, 6, 3, 6)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                        100, 4, false, true, true));
                le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE,
                        100, 2, false, true, true));
                le.damage(4.0, p);
                SpellEffects.cage(le.getLocation().add(0, 0.5, 0), Particle.SNOWFLAKE);
            }
        }
    }

    private void castMeteor(Player p) {
        Location t = rayTarget(p, 24);
        p.sendActionBar(Component.text("☄ النيزك في الطريق!", NamedTextColor.GOLD));
        SpellEffects.meteorStrike(plugin, t);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Entity e : t.getWorld().getNearbyEntities(t, 5, 3, 5)) {
                if (e instanceof LivingEntity le && !e.equals(p)) {
                    le.damage(11.0, p);
                    le.setFireTicks(60);
                    le.setVelocity(le.getLocation().toVector()
                            .subtract(t.toVector()).normalize().multiply(0.8).setY(0.5));
                }
            }
        }, 20L);
    }
}

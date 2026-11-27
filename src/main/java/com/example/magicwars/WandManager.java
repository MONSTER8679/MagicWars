package com.example.magicwars;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** إدارة العصي: كشف نوع العصا من المادة، تبريد، وتنفيذ التعاويذ */
public class WandManager {

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public WandManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public boolean isWandItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        for (WandType t : WandType.values())
            if (t.material == item.getType()) return true;
        return false;
    }

    public WandType match(ItemStack item) {
        if (item == null) return null;
        for (WandType t : WandType.values())
            if (t.material == item.getType()) return t;
        return null;
    }

    /** يُستدعى عند النقر الأيمن بعصا */
    public void handleInteract(Player p, WandType type) {
        if (onCooldown(p, type)) {
            long left = remaining(p, type);
            p.sendActionBar(net.kyori.adventure.text.Component.text(
                    "⏳ " + type.displayName + " بعد " + String.format("%.1f", left) + " ث",
                    net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }
        if (!plugin.manaManager().consume(p, type.manaCost)) {
            p.sendActionBar(net.kyori.adventure.text.Component.text(
                    "✦ لا توجد طاقة كافية! (" + type.manaCost + ")",
                    net.kyori.adventure.text.format.NamedTextColor.RED));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.6f);
            return;
        }
        cast(p, type);
        setCooldown(p, type);
    }

    private boolean onCooldown(Player p, WandType t) {
        return remaining(p, t) > 0;
    }

    private double remaining(Player p, WandType t) {
        long until = cooldowns
                .getOrDefault(p.getUniqueId(), Map.of())
                .getOrDefault(t.id, 0L);
        double left = (until - System.currentTimeMillis()) / 1000.0;
        return Math.max(0, left);
    }

    private void setCooldown(Player p, WandType t) {
        cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .put(t.id, System.currentTimeMillis() + (long) (t.cooldownSec * 1000));
    }

    /** تنفيذ التعويذة حسب نوع العصا */
    public void cast(Player p, WandType type) {
        switch (type) {
            case FIRE_STAFF     -> castFire(p);
            case ICE_WAND       -> castIce(p);
            case LIGHTNING_WAND -> castLightning(p);
            case HEAL_WAND      -> castHeal(p);
            case BLINK_WAND     -> castBlink(p);
            case WIND_WAND      -> castWind(p);
            case VOID_WAND      -> castVoid(p);
            case STORM_WAND     -> castStorm(p);
            case DRAGON_WAND    -> castDragon(p);
            case TIME_WAND      -> castTime(p);
            case METEOR_WAND    -> castMeteor(p);
        }
    }

    private void castFire(Player p) {
        Location t = rayTarget(p, 18);
        SpellEffects.burst(t, Particle.FLAME, 25, 0.3);
        SpellEffects.beamPillar(t, 3, Particle.LAVA, 2);
        t.getWorld().playSound(t, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1f);
        damageNear(p, t, 3, 7.0, 40);
    }

    private void castIce(Player p) {
        Location t = rayTarget(p, 16);
        SpellEffects.circle(t, 2.5, 20, Particle.SNOWFLAKE, 0.2, 0.05);
        SpellEffects.burst(t, Particle.SNOWFLAKE, 20, 0.4);
        t.getWorld().playSound(t, Sound.BLOCK_GLASS_PLACE, 1.5f, 1.4f);
        for (Entity e : t.getWorld().getNearbyEntities(t, 3, 2, 3)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                le.damage(5.0, p);
                le.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS, 80, 2, false, true, true));
            }
        }
    }

    private void castLightning(Player p) {
        Location t = rayTarget(p, 22);
        t.getWorld().strikeLightning(t);
        SpellEffects.burst(t, Particle.ELECTRIC_SPARK, 40, 0.5);
        damageNear(p, t, 2.5, 9.0, 0);
    }

    private void castHeal(Player p) {
        p.setHealth(Math.min(p.getHealth() + 8.0,
                p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
        SpellEffects.dome(p.getLocation().add(0, 0.5, 0), 1.6, Particle.HEART);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 1.6f);
        p.sendActionBar(net.kyori.adventure.text.Component.text(
                "✚ شفاء +8", net.kyori.adventure.text.format.NamedTextColor.GREEN));
    }

    private void castBlink(Player p) {
        Location eye = p.getEyeLocation();
        Location target = eye.clone().add(eye.getDirection().multiply(10));
        SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.PORTAL, 30, 0.3);
        p.teleport(target);
        SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.PORTAL, 30, 0.3);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1f);
    }

    private void castWind(Player p) {
        Location t = rayTarget(p, 10);
        SpellEffects.groundWave(plugin, p.getLocation(),
                p.getEyeLocation().getDirection().setY(0).normalize(),
                Particle.CLOUD, 10);
        t.getWorld().playSound(t, Sound.ENTITY_BREEZE_SHOOT, 2f, 1f);
        for (Entity e : t.getWorld().getNearbyEntities(t, 3, 2, 3)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                org.bukkit.util.Vector v = le.getLocation().toVector()
                        .subtract(p.getLocation().toVector()).normalize()
                        .multiply(1.8).setY(0.6);
                le.setVelocity(v);
                le.damage(3.0, p);
            }
        }
    }

    private void castVoid(Player p) {
        Location t = rayTarget(p, 14);
        SpellEffects.blackHole(t, 2.5, Particle.PORTAL, 30);
        SpellEffects.vortex(plugin, t, Particle.SQUID_INK, 25);
        t.getWorld().playSound(t, Sound.ENTITY_WARDEN_HEARTBEAT, 2f, 0.5f);
        for (Entity e : t.getWorld().getNearbyEntities(t, 4, 3, 4)) {
            if (e instanceof LivingEntity le && !e.equals(p)) {
                org.bukkit.util.Vector v = t.toVector()
                        .subtract(le.getLocation().toVector()).normalize().multiply(0.8);
                le.setVelocity(v);
                le.damage(6.0, p);
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
        org.bukkit.util.Vector dir = eye.getDirection().normalize();
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
        p.sendActionBar(net.kyori.adventure.text.Component.text(
                "☄ النيزك في الطريق!", net.kyori.adventure.text.format.NamedTextColor.GOLD));
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

    /** نقطة يستهدفها شعاع النظر (حد أقصى المدى) */
    public Location rayTarget(Player p, double range) {
        return p.getEyeLocation().add(
                p.getEyeLocation().getDirection().normalize().multiply(range));
    }

    private void damageNear(Player caster, Location t, double r, double dmg, int fire) {
        for (Entity e : t.getWorld().getNearbyEntities(t, r, r, r)) {
            if (e instanceof LivingEntity le && !e.equals(caster)) {
                le.damage(dmg, caster);
                if (fire > 0) le.setFireTicks(fire);
            }
        }
    }
}

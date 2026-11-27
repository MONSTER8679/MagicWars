package com.example.magicwars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** الجواهر السحرية: تظهر دوريًا في الملاعب المشغولة — التقاطها يمنح مكافأة */
public class PowerupManager implements Listener {

    public record Powerup(ArmorStand display, String kind) {}

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Powerup> live = new HashMap<>();

    public PowerupManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    private org.bukkit.NamespacedKey key() {
        return new org.bukkit.NamespacedKey(plugin, "powerup");
    }

    public void startTask() {
        long period = plugin.getConfig().getLong("powerups.interval-ticks", 300L);
        new BukkitRunnable() {
            @Override public void run() {
                for (GameManager.Match m : plugin.gameManager().matches()) {
                    if (live.size() >= 4) break;               // حد أقصى عالمي
                    spawnOne(m);
                }
            }
        }.runTaskTimer(plugin, period, period);
    }

    private void spawnOne(GameManager.Match m) {
        // ✅ إصلاح v6.1: ولادة الجوهرة على الحلقة الصالحة للمشي (r=36..48)
        // بعيدًا عن الزقورة والخنادق
        double a = Math.random() * 2 * Math.PI;
        double r = 36 + Math.random() * 12;
        Location loc = m.arena.center.clone().add(
                r * Math.cos(a), 1, r * Math.sin(a));

        String[] kinds = {"heal", "mana", "speed", "strength"};
        String kind = kinds[(int) (Math.random() * kinds.length)];

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.getPersistentDataContainer().set(key(),
                PersistentDataType.STRING, kind);
        switch (kind) {
            case "heal"     -> stand.customName(net.kyori.adventure.text.Component.text(
                    "❤ الجوهرة الحمراء", net.kyori.adventure.text.format.NamedTextColor.RED));
            case "mana"     -> stand.customName(net.kyori.adventure.text.Component.text(
                    "⚡ جوهرة الطاقة", net.kyori.adventure.text.format.NamedTextColor.AQUA));
            case "speed"    -> stand.customName(net.kyori.adventure.text.Component.text(
                    "💨 جوهرة السرعة", net.kyori.adventure.text.format.NamedTextColor.GREEN));
            case "strength" -> stand.customName(net.kyori.adventure.text.Component.text(
                    "💎 جوهرة القوة", net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
        }
        stand.setCustomNameVisible(true);

        live.put(stand.getUniqueId(), new Powerup(stand, kind));

        // جزيئات دوّارة حول الجوهرة
        new BukkitRunnable() {
            @Override public void run() {
                if (!live.containsKey(stand.getUniqueId())) { cancel(); return; }
                Location l = stand.getLocation();
                double t = (System.currentTimeMillis() / 300.0) % (2 * Math.PI);
                for (int i = 0; i < 2; i++) {
                    double ang = t + Math.PI * i;
                    l.getWorld().spawnParticle(Particle.END_ROD,
                            l.getX() + 0.8 * Math.cos(ang), l.getY() + 1,
                            l.getZ() + 0.8 * Math.sin(ang), 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 20L, 4L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand stand)) return;
        String kind = stand.getPersistentDataContainer().get(key(),
                PersistentDataType.STRING);
        if (kind == null) return;
        Player p = e.getPlayer();
        e.setCancelled(true);
        despawn(stand.getUniqueId());

        switch (kind) {
            case "heal" -> {
                p.setHealth(Math.min(p.getHealth() + 10, 20));
                SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.HEART, 15, 0.3);
            }
            case "mana" -> {
                plugin.manaManager().addMana(p, 50);
                SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.WITCH, 15, 0.3);
            }
            case "speed" -> {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SPEED, 200, 1));
                SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.CLOUD, 15, 0.3);
            }
            case "strength" -> {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.STRENGTH, 200, 0));
                SpellEffects.burst(p.getLocation().add(0, 1, 0), Particle.CRIT, 15, 0.3);
            }
        }
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 1.2f);
    }

    private void despawn(UUID id) {
        Powerup p = live.remove(id);
        if (p != null && p.display().isValid()) p.display().remove();
    }

    /** حذف الجواهر داخل نطاق ملعب واحد فقط (لا تلمس ملاعب أخرى بنفس العالم) */
    public void despawnNear(Location center, double range) {
        for (UUID id : new java.util.ArrayList<>(live.keySet())) {
            Powerup p = live.get(id);
            Location l = p.display().getLocation();
            if (l.getWorld() == center.getWorld() && l.distance(center) <= range)
                despawn(id);
        }
    }
}

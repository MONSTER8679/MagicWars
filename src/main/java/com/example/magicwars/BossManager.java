package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** الوحش البلوري — بوس بـ 300 صحة، ينتقم من مهاجمه، يوزع مكافآت على من شارك بقتله */
public class BossManager implements Listener {

    private final MagicWarsPlugin plugin;
    private UUID bossId;
    private final Set<UUID> damagers = new HashSet<>();

    public BossManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public boolean alive() { return bossAlive() != null; }

    public void spawn(Location loc) {
        if (alive()) {
            Bukkit.broadcast(Component.text("الوحش موجود بالفعل!", NamedTextColor.RED));
            return;
        }
        IronGolem boss = (IronGolem) loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
        boss.setPlayerCreated(false);
        boss.setRemoveWhenFarAway(false);
        boss.customName(Component.text("👹 الوحش البلوري",
                NamedTextColor.DARK_PURPLE, net.kyori.adventure.text.format.TextDecoration.BOLD));
        boss.setCustomNameVisible(true);
        boss.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(300.0);
        boss.setHealth(300.0);

        bossId = boss.getUniqueId();
        damagers.clear();
        Bukkit.broadcast(Component.text(
                "👹 استيقظ الوحش البلوري! اهزموه للفوز بالمكافآت!", NamedTextColor.DARK_PURPLE));
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_ROAR, 2f, 0.7f);
        SpellEffects.beamPillar(loc, 20, Particle.SOUL, 3);

        // هالة دائمة أثناء حياته
        new BukkitRunnable() {
            @Override public void run() {
                IronGolem b = bossAlive();
                if (b == null) { cancel(); return; }
                SpellEffects.orbit(plugin, b.getLocation(), Particle.SOUL, 3, 2.2);
                b.getWorld().spawnParticle(Particle.WITCH,
                        b.getLocation().add(0, 2.5, 0), 4, 0.5, 0.3, 0.5, 0.02);
            }
        }.runTaskTimer(plugin, 20L, 6L);
    }

    private IronGolem bossAlive() {
        Entity e = bossId == null ? null : Bukkit.getEntity(bossId);
        return (e instanceof IronGolem g && e.isValid()) ? g : null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent ev) {
        IronGolem boss = bossAlive();
        if (boss == null || !ev.getEntity().getUniqueId().equals(bossId)) return;
        Player damager = resolvePlayer(ev.getDamager());
        if (damager != null) {
            damagers.add(damager.getUniqueId());
            boss.setTarget(damager);                                  // ينتقم فورًا
            SpellEffects.burst(ev.getDamager().getLocation(), Particle.CRIT, 5, 0.1);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent ev) {
        if (!ev.getEntity().getUniqueId().equals(bossId)) return;
        bossId = null;
        int coins = plugin.getConfig().getInt("boss.coins-per-killer", 50);
        int xp = plugin.getConfig().getInt("boss.xp-per-killer", 30);

        Bukkit.broadcast(Component.text("🏆 سقط الوحش البلوري! المجاهدون ينالون "
                + coins + " 💰", NamedTextColor.GOLD));
        SpellEffects.victory(ev.getEntity().getLocation());
        SpellEffects.expandingRings(plugin, ev.getEntity().getLocation(), Particle.SOUL, 4, 6);

        for (UUID id : damagers) {
            plugin.stats().addCoins(id, coins);
            plugin.stats().addXp(id, xp);
            Player p = Bukkit.getPlayer(id);
            if (p != null)
                p.sendMessage(Component.text("+" + coins + " 💰  +" + xp + " XP — هدية الوحش!",
                        NamedTextColor.GOLD));
        }
        damagers.clear();
    }

    private Player resolvePlayer(Entity d) {
        if (d instanceof Player pl) return pl;
        if (d instanceof Projectile pr && pr.getShooter() instanceof Player pl) return pl;
        return null;
    }
}

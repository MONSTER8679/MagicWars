package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** جواهر قوة تسقط في الملاعب دوريًا — v6.1: توليد على الحلقة الصالحة للمشي (r=36..48) */
public class PowerupManager {

    public record Powerup(ArmorStand display, WandType gift, long expireAt) {}

    private final MagicWarsPlugin plugin;
    private final Map<UUID, Powerup> live = new HashMap<>();

    public PowerupManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void startTask() {
        int interval = plugin.getConfig().getInt("powerups.interval-ticks", 300);
        long lifetime = plugin.getConfig().getLong("powerups.lifetime-ticks", 6000);
        new BukkitRunnable() {
            @Override public void run() {
                // يظهر فقط في الملاعب الجارية
                for (GameManager.Match m : plugin.gameManager().matches()) {
                    if (live.size() >= 8) return;
                    spawnIn(m, System.currentTimeMillis() + lifetime * 50);
                }
            }
        }.runTaskTimer(plugin, 100L, interval);
    }

    /** v6.1: التوليد على الحلقة r=36..48 (خارج الزقورة، قابل للمشي) */
    private void spawnIn(GameManager.Match m, long expireAt) {
        double ang = Math.random() * 2 * Math.PI;
        double r = 36 + Math.random() * 12;              // الحلقة الصالحة للمشي
        Location loc = m.arena.center.clone()
                .add(r * Math.cos(ang), 1, r * Math.sin(ang));
        WandType[] gifts = WandType.values();
        WandType gift = gifts[(int) (Math.random() * gifts.length)];
        spawn(loc, gift, expireAt);
    }

    public void spawn(Location loc, WandType gift, long expireAt) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(gift.material));
        live.put(stand.getUniqueId(), new Powerup(stand, gift, expireAt));
    }

    public void despawn(UUID id) {
        Powerup p = live.remove(id);
        if (p != null) p.display().remove();
    }

    /** v6.1: حذف الجواهر داخل نطاق ملعب واحد فقط (لا تلمس ملاعب أخرى بنفس العالم) */
    public void despawnNear(Location center, double range) {
        for (UUID id : new java.util.ArrayList<>(live.keySet())) {
            Powerup p = live.get(id);
            Location l = p.display().getLocation();
            if (l.getWorld() == center.getWorld() && l.distance(center) <= range)
                despawn(id);
        }
    }

    public Map<UUID, Powerup> liveMap() { return live; }
}

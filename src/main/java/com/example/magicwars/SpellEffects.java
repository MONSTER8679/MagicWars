package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import org.bukkit.scheduler.BukkitRunnable;

/** مكتبة تأثيرات الجزيئات — ثابتة ومتحركة (v6.1: إضافة استيراد Sound) */
public final class SpellEffects {
    private SpellEffects() {}

    /** دائرة أفقية على الأرض */
    public static void circle(Location c, double radius, int points,
                              Particle p, double yOffset, double speed) {
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            c.getWorld().spawnParticle(p,
                    c.getX() + radius * Math.cos(a),
                    c.getY() + yOffset,
                    c.getZ() + radius * Math.sin(a),
                    1, 0, 0, 0, speed);
        }
    }

    /** شعاع بين نقطتين */
    public static void beam(Location from, Location to, Particle p, double density) {
        org.bukkit.util.Vector v = to.toVector().subtract(from.toVector());
        double len = v.length();
        if (len == 0) return;
        v.normalize();
        for (double d = 0; d <= len; d += density) {
            from.getWorld().spawnParticle(p,
                    from.getX() + v.getX() * d,
                    from.getY() + v.getY() * d,
                    from.getZ() + v.getZ() * d, 1, 0, 0, 0, 0);
        }
    }

    /** انفجار كروي من جزيئات */
    public static void burst(Location c, Particle p, int count, double spread) {
        c.getWorld().spawnParticle(p, c, count, spread, spread, spread, 0.1);
    }

    /** حلقات تتوسع تدريجيًا من نقطة */
    public static void expandingRings(MagicWarsPlugin plugin, Location c,
                                      Particle p, int rings, double maxRadius) {
        new BukkitRunnable() {
            int r = 0;
            @Override public void run() {
                if (r >= rings) { cancel(); return; }
                double radius = maxRadius * (r + 1) / rings;
                circle(c, radius, 30, p, 0.1, 0.05);
                r++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /** تأثير النصر */
    public static void victory(Location c) {
        World w = c.getWorld();
        w.playSound(c, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 1f);
        burst(c.clone().add(0, 1, 0), Particle.FIREWORK, 60, 0.6);
        expandingRings(plugin0(), c, Particle.END_ROD, 4, 4);
    }

    private static MagicWarsPlugin plugin0() {
        // مرجع مؤقت للبلوقن (يُستخدم في victory فقط)
        return MagicWarsPlugin.getInstance();
    }

    /** إعصار دوّار يرتفع (تأثير عاصفة) */
    public static void tornado(Location base, double height, double maxRadius, Particle p) {
        for (double h = 0; h <= height; h += 0.4) {
            double t = h / height;
            double radius = maxRadius * (1 - t);       // واسع بالأسفل، ضيق بالأعلى
            double a = h * 3;
            for (int k = 0; k < 3; k++) {
                double ang = a + 2 * Math.PI * k / 3;
                base.getWorld().spawnParticle(p,
                    base.getX() + radius * Math.cos(ang),
                    base.getY() + h,
                    base.getZ() + radius * Math.sin(ang),
                    1, 0, 0, 0, 0);
            }
        }
    }

    /** ثقب أسود: جزيئات تُسحب نحو المركز */
    public static void blackHole(Location c, double radius, Particle p, int points) {
        for (int i = 0; i < points; i++) {
            double a = Math.random() * 2 * Math.PI;
            double r = radius * (0.3 + 0.7 * Math.random());
            c.getWorld().spawnParticle(p,
                c.getX() + r * Math.cos(a),
                c.getY() + (Math.random() - 0.5),
                c.getZ() + r * Math.sin(a),
                1, 0, 0, 0, 0);
        }
        c.getWorld().spawnParticle(Particle.SQUID_INK, c, 5, 0.1, 0.1, 0.1, 0.02);
    }

    /** مطر نجوم: جزيئات تهبط من السماء حول نقطة */
    public static void starRain(Location c, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double a = Math.random() * 2 * Math.PI;
            double r = Math.random() * radius;
            c.getWorld().spawnParticle(Particle.END_ROD,
                c.getX() + r * Math.cos(a),
                c.getY() + 6 + Math.random() * 4,
                c.getZ() + r * Math.sin(a),
                1, 0, -0.15, 0, 0);   // تهبط للأسفل
        }
    }

    /** قبة حماية: نصف كرة فوق اللاعب */
    public static void dome(Location c, double radius, Particle p) {
        for (double phi = 0; phi <= Math.PI / 2; phi += Math.PI / 12) {
            int ringPoints = Math.max(6, (int) (24 * Math.sin(phi)));
            for (int i = 0; i < ringPoints; i++) {
                double a = 2 * Math.PI * i / ringPoints;
                c.getWorld().spawnParticle(p,
                    c.getX() + radius * Math.sin(phi) * Math.cos(a),
                    c.getY() + radius * Math.cos(phi),
                    c.getZ() + radius * Math.sin(phi) * Math.sin(a),
                    1, 0, 0, 0, 0);
            }
        }
    }

    /** عمود ضوء من السماء إلى الأرض (ضربة/إشارة) */
    public static void beamPillar(Location c, double height, Particle p, int density) {
        for (double h = 0; h <= height; h += 0.5) {
            c.getWorld().spawnParticle(p, c.getX(), c.getY() + h, c.getZ(),
                    density, 0.15, 0, 0.15, 0);
        }
    }

    /** مخروط أمام اللاعب (نَفَس التنين) */
    public static void cone(Location origin, org.bukkit.util.Vector dir,
                            double length, double endSpread, Particle p, int steps) {
        for (double d = 0.5; d <= length; d += length / steps) {
            double spread = endSpread * (d / length);
            for (int i = 0; i < 8; i++) {
                org.bukkit.util.Vector offset = new org.bukkit.util.Vector(
                        (Math.random() - 0.5), (Math.random() - 0.5) * 0.6,
                        (Math.random() - 0.5)).multiply(spread);
                org.bukkit.util.Vector v = dir.clone().multiply(d).add(offset);
                origin.getWorld().spawnParticle(p,
                        origin.getX() + v.getX(), origin.getY() + v.getY(),
                        origin.getZ() + v.getZ(), 1, 0, 0, 0, 0.01);
            }
        }
    }

    /** دوامة: جزيئات تلتف حلزونيًا نحو الداخل */
    public static void vortex(MagicWarsPlugin plugin, Location c, Particle p, int ticks) {
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t++ > ticks) { cancel(); return; }
                double prog = (double) t / ticks;
                for (int i = 0; i < 6; i++) {
                    double a = 2 * Math.PI * i / 6.0 + prog * 6;
                    double r = 4 * (1 - prog) + 0.5;
                    c.getWorld().spawnParticle(p,
                        c.getX() + r * Math.cos(a), c.getY() + prog * 2,
                        c.getZ() + r * Math.sin(a), 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** مدار: جزيئات تدور حول نقطة طوال المدة */
    public static void orbit(MagicWarsPlugin plugin, Location c, Particle p,
                             int ticks, double radius) {
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t++ > ticks) { cancel(); return; }
                double a = t * 0.3;
                for (int i = 0; i < 4; i++) {
                    double ang = a + Math.PI * i / 2;
                    c.getWorld().spawnParticle(p,
                        c.getX() + radius * Math.cos(ang), c.getY() + 1,
                        c.getZ() + radius * Math.sin(ang), 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** قفص: أعمدة جزيئات حول نقطة */
    public static void cage(Location c, Particle p) {
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * i / 4;
            double x = c.getX() + 1.8 * Math.cos(a);
            double z = c.getZ() + 1.8 * Math.sin(a);
            for (double h = 0; h <= 3; h += 0.3)
                c.getWorld().spawnParticle(p, x, c.getY() + h, z, 1, 0, 0, 0, 0);
        }
    }

    /** موجة أرضية تتقدم أمام اللاعب */
    public static void groundWave(MagicWarsPlugin plugin, Location c,
                                  org.bukkit.util.Vector dir, Particle p, double len) {
        new BukkitRunnable() {
            double d = 0;
            @Override public void run() {
                if (d >= len) { cancel(); return; }
                d += 0.8;
                for (int i = -3; i <= 3; i++) {
                    org.bukkit.util.Vector off =
                            new org.bukkit.util.Vector(-dir.getZ(), 0, dir.getX())
                                    .multiply(i * 0.6);
                    org.bukkit.util.Vector v = dir.clone().multiply(d).add(off);
                    c.getWorld().spawnParticle(p,
                        c.getX() + v.getX(), c.getY() + 0.2, c.getZ() + v.getZ(),
                        1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** رمز سحري على الأرض: دائرتان + نجمة داخلية */
    public static void glyph(Location c) {
        circle(c, 3.5, 36, Particle.WITCH, 0.05, 0);
        circle(c, 2.2, 24, Particle.ENCHANT, 0.05, 0);
        for (int i = 0; i < 6; i++) {
            double a1 = Math.PI * i / 3, a2 = a1 + Math.PI * 2 / 3 * 2;
            beam(c.clone().add(2.2 * Math.cos(a1), 0.05, 2.2 * Math.sin(a1)),
                 c.clone().add(2.2 * Math.cos(a2), 0.05, 2.2 * Math.sin(a2)),
                 Particle.WITCH, 0.15);
        }
    }

    /** نافورة: رشاش جزيئات صاعد */
    public static void fountain(MagicWarsPlugin plugin, Location c, Particle p, int bursts) {
        new BukkitRunnable() {
            int b = 0;
            @Override public void run() {
                if (b++ >= bursts) { cancel(); return; }
                for (int i = 0; i < 12; i++) {
                    double a = Math.random() * 2 * Math.PI;
                    double s = 0.3 + Math.random() * 0.4;
                    c.getWorld().spawnParticle(p, c.getX(), c.getY() + 0.2, c.getZ(),
                            1, Math.cos(a) * s, 0.5 + Math.random() * 0.4, Math.sin(a) * s, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /** مطر جزيئات فوق منطقة */
    public static void rain(Location c, Particle p, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double a = Math.random() * 2 * Math.PI;
            double r = Math.random() * radius;
            c.getWorld().spawnParticle(p,
                c.getX() + r * Math.cos(a), c.getY() + 7 + Math.random() * 3,
                c.getZ() + r * Math.sin(a), 1, 0, -0.12, 0, 0);
        }
    }

    /** نيزك يسقط من السماء ثم يتفجر */
    public static void meteorStrike(MagicWarsPlugin plugin, Location target) {
        Location sky = target.clone().add(0, 25, 0);
        target.getWorld().playSound(sky, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
        new BukkitRunnable() {
            double d = 0;
            @Override public void run() {
                if (d >= 25) {
                    cancel();
                    burst(target, Particle.EXPLOSION, 50, 0.3);
                    target.getWorld().spawnParticle(Particle.LAVA, target, 25, 1, 0.5, 1, 0);
                    target.getWorld().playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.8f);
                    return;
                }
                d += 1.5;
                Location l = sky.clone().add(0, -d, 0);
                l.getWorld().spawnParticle(Particle.FLAME, l, 6, 0.15, 0.15, 0.15, 0.02);
                l.getWorld().spawnParticle(Particle.SMOKE, l, 3, 0.1, 0.1, 0.1, 0);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}

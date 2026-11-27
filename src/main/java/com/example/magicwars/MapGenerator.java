package com.example.magicwars;

import org.bukkit.Material;
import org.bukkit.World;

/**
 * الماندالا العظمى — جزيرة دائرية 145×145 بتناظر 16 ضلعًا
 * + 4 جزر قمرية عائمة (قمران لكل فريق)
 */
public final class MapGenerator {
    private MapGenerator() {}

    public static final int R = 72;       // نصف قطر الجزيرة
    public static final int BASE_Y = 80;
    public static final int SAT_D = 95;   // بُعد الجزر القمرية
    public static final int SAT_R = 10;   // نصف قطر القمر

    private static boolean in(double x, double z) {
        return Math.sqrt(x * (double) x + z * (double) z) <= R;
    }

    /** المسافة الزاوية لأقرب مضاعف للخطوة */
    private static double angDist(double a, double step) {
        return Math.abs(a - Math.round(a / step) * step);
    }

    public static Arena generate(World w, int index, GameMode mode) {
        int ox = index * 1200;           // ملاعب متباعدة 1200 بلوك
        int y = BASE_Y;

        // 0) الجزر الأقمار: الأزرق شرق وشمال / الأحمر غرب وجنوب
        int[][] sat = {{SAT_D, 0}, {-SAT_D, 0}, {0, SAT_D}, {0, -SAT_D}};

        // 1) تفريغ الجزيرة الأم
        for (int x = -R - 8; x <= R + 8; x++)
            for (int z = -R - 8; z <= R + 8; z++)
                for (int dy = -14; dy < 42; dy++)
                    w.getBlockAt(ox + x, y + dy, z).setType(Material.AIR);

        // تفريغ الجزر القمرية ومسارات الجسور
        for (int[] s : sat)
            for (int dx = -SAT_R - 6; dx <= SAT_R + 6; dx++)
                for (int dz = -SAT_R - 6; dz <= SAT_R + 6; dz++)
                    for (int dy = -14; dy < 42; dy++)
                        w.getBlockAt(ox + s[0] + dx, y + dy, s[1] + dz).setType(Material.AIR);

        // 2) الأرضية الماندالا: 16 قطاعًا + خنادق + حلقات
        for (int x = -R; x <= R; x++)
            for (int z = -R; z <= R; z++) {
                double r = Math.sqrt(x * (double) x + z * (double) z);
                if (r > R) continue;
                double ang = Math.atan2(z, x) + Math.PI;            // 0..2π
                int sector = (int) (ang / (Math.PI / 8));           // 16 قطاعًا
                Material m = (sector % 2 == 0) ? Material.QUARTZ_BLOCK
                                                : Material.POLISHED_DEEPSLATE;

                if (r <= 29) {
                    // المنطقة الداخلية (الزنبقة) — تبقى بالشطرنج
                } else if (r <= 30)      m = Material.CRYING_OBSIDIAN;   // حافة الحمم
                else if (r <= 33)        m = Material.LAVA;               // خندق الحمم
                else if (r <= 34)        m = Material.CRYING_OBSIDIAN;
                else if (r <= 50) {                                          // سيربنسكي
                    int u = Math.abs(x), v = Math.abs(z);
                    m = ((v & ~(u + v)) == 0)
                            ? Material.AMETHYST_BLOCK : Material.POLISHED_DEEPSLATE;
                }
                else if (r <= 51)        m = Material.SEA_LANTERN;        // حافة الماء
                else if (r <= 55)        m = Material.WATER;              // خندق الماء
                else if (r <= 56)        m = Material.SEA_LANTERN;
                else if (r <= 69) {                                          // الممشى الخارجي
                    m = Material.POLISHED_BLACKSTONE_BRICKS;
                    if (angDist(ang, Math.PI / 8) < 0.045)
                        m = Material.AMETHYST_BLOCK;                    // أضلاع نصف قطرية
                }
                else                    m = Material.CRYING_OBSIDIAN;    // الحافة

                w.getBlockAt(ox + x, y - 1, z).setType(m);
                w.getBlockAt(ox + x, y - 2, z).setType(Material.CRYING_OBSIDIAN); // قاع
            }

        // 3) زنبقة الـ16 بتلة: r ≤ 26·cos(8Δ) — مرتفعة بلوك واحد
        for (int x = -26; x <= 26; x++)
            for (int z = -26; z <= 26; z++) {
                double r = Math.sqrt(x * (double) x + z * (double) z);
                if (r < 7 || r > 26) continue;
                double ang = Math.atan2(z, x) + Math.PI;
                double petal = 26 * Math.cos(8 * angDist(ang, Math.PI / 8));
                if (r > petal) continue;
                w.getBlockAt(ox + x, y, z).setType(
                        r > petal - 2 ? Material.SEA_LANTERN   // طرف متوهج
                                     : Material.AMETHYST_BLOCK);
            }

        // 4) الزقورة: 7 طبقات، نصف القطر 20-3t
        for (int t = 0; t <= 6; t++) {
            int rad = 20 - 3 * t;
            for (int dx = -rad; dx <= rad; dx++)
                for (int dz = -rad; dz <= rad; dz++)
                    if (dx * dx + dz * dz <= rad * rad)
                        w.getBlockAt(ox + dx, y + t, dz).setType(
                                t == 6 ? Material.GLOWSTONE
                              : (t % 2 == 0) ? Material.POLISHED_DEEPSLATE
                                             : Material.AMETHYST_BLOCK);
        }
        // عمود الضوء
        for (int h = 7; h <= 16; h++)
            w.getBlockAt(ox, y + h, 0).setType(
                    h == 16 ? Material.AMETHYST_CLUSTER : Material.SEA_LANTERN);

        // 5) السلم اللولبي حول الزقورة (3.5 دورة صعودًا)
        for (double th = 0; th <= 7 * Math.PI; th += 0.02) {
            int h = y + 1 + (int) Math.round(6 * th / (7 * Math.PI));
            for (int rr = 21; rr <= 22; rr++) {
                int sx = (int) Math.round(rr * Math.cos(th));
                int sz = (int) Math.round(rr * Math.sin(th));
                w.getBlockAt(ox + sx, h, sz).setType(Material.QUARTZ_BLOCK);
            }
        }

        // 6) الجسور الـ16 فوق الخندقين (زجاج + جلوستون كل 3)
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI / 8;
            for (double t = 29; t <= 35; t += 0.5) bridgeBlock(w, ox, y, a, t);
            for (double t = 50; t <= 57; t += 0.5) bridgeBlock(w, ox, y, a, t);
        }

        // 7) المسلات الخارجية الـ16 (بين الجسور) عند r=64
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI / 8 + Math.PI / 16;
            int tx = (int) Math.round(64 * Math.cos(a));
            int tz = (int) Math.round(64 * Math.sin(a));
            for (int h = 0; h <= 6; h++)
                w.getBlockAt(ox + tx, y + h, tz).setType(
                        h == 6 ? Material.GLOWSTONE : Material.OBSIDIAN);
            w.getBlockAt(ox + tx, y + 7, tz).setType(Material.AMETHYST_CLUSTER);
        }

        // 8) أسنان الحافة الدائرية
        for (int x = -R; x <= R; x++)
            for (int z = -R; z <= R; z++) {
                double r = Math.sqrt(x * (double) x + z * (double) z);
                if (r < 69.5) continue;
                boolean edge = !in(x + 1, z) || !in(x - 1, z)
                            || !in(x, z + 1) || !in(x, z - 1);
                if (!edge) continue;
                for (int h = 0; h < 2; h++)
                    w.getBlockAt(ox + x, y + h, z)
                            .setType(Material.POLISHED_BLACKSTONE_BRICKS);
                if (((x + z) & 1) == 0)
                    w.getBlockAt(ox + x, y + 2, z).setType(Material.CRYING_OBSIDIAN);
            }

        // 9) الجزر القمرية الأربعة + الجسور الرابطة
        for (int i = 0; i < 4; i++) {
            int[] s = sat[i];                                          // ✅ إصلاح v6.1
            boolean redTeam = (i == 1 || i == 3);   // غرب وجنوب = أحمر
            Material teamColor = redTeam ? Material.RED_CONCRETE
                                         : Material.BLUE_CONCRETE;

            // جسم القمر
            for (int dx = -SAT_R; dx <= SAT_R; dx++)
                for (int dz = -SAT_R; dz <= SAT_R; dz++) {
                    int rr = dx * dx + dz * dz;
                    if (rr > SAT_R * SAT_R) continue;
                    Material m = ((dx + dz) & 1) == 0
                            ? Material.QUARTZ_BLOCK : Material.POLISHED_DEEPSLATE;
                    if (rr >= (SAT_R - 2) * (SAT_R - 2)) m = Material.CRYING_OBSIDIAN;
                    if (rr <= 9 && rr >= 4)              m = teamColor;   // حلقة الفريق
                    if (dx == 0 && dz == 0)              m = Material.SEA_LANTERN;
                    w.getBlockAt(ox + s[0] + dx, y - 1, s[1] + dz).setType(m);
                    w.getBlockAt(ox + s[0] + dx, y - 2, s[1] + dz)
                            .setType(Material.CRYING_OBSIDIAN);
                }
            // مسلة القمر
            for (int h = 0; h < 3; h++)
                w.getBlockAt(ox + s[0] + 4, y + h, s[1] + 4).setType(Material.OBSIDIAN);
            w.getBlockAt(ox + s[0] + 4, y + 3, s[1] + 4).setType(Material.GLOWSTONE);

            // الجسر الرابط نحو الجزيرة الأم (بدرابزين أوبسيديان باكٍ)
            int dirX = Integer.signum(-s[0]) * (s[0] != 0 ? 1 : 0);
            int dirZ = Integer.signum(-s[1]) * (s[1] != 0 ? 1 : 0);
            int perpX = (dirZ != 0) ? 1 : 0;
            int perpZ = (dirX != 0) ? 1 : 0;
            for (int t = R; t <= SAT_D - SAT_R; t++) {
                int bx = s[0] != 0 ? Integer.signum(s[0]) * t : 0;
                int bz = s[1] != 0 ? Integer.signum(s[1]) * t : 0;
                w.getBlockAt(ox + bx, y - 1, bz).setType(
                        t % 4 == 0 ? Material.GLOWSTONE : Material.GLASS);
                w.getBlockAt(ox + bx + perpX, y, bz + perpZ)
                        .setType(Material.CRYING_OBSIDIAN);
                w.getBlockAt(ox + bx - perpX, y, bz - perpZ)
                        .setType(Material.CRYING_OBSIDIAN);
            }
        }
        return new Arena(mode, w, ox, y);
    }

    private static void bridgeBlock(World w, int ox, int y, double a, double t) {
        int bx = (int) Math.round(t * Math.cos(a));
        int bz = (int) Math.round(t * Math.sin(a));
        w.getBlockAt(ox + bx, y - 1, bz).setType(
                ((int) t) % 3 == 0 ? Material.GLOWSTONE : Material.GLASS);
    }
}

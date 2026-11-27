package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * /mwadmin setup — التركيب الكامل بضغطة واحدة:
 * 1) منصة لوبي دائرية عند موقعك + NPC الدخول
 * 2) منصة الأنماط (Hub) على بعد 60 بلوك + 4 NPCs
 * 3) إنشاء عالم المابات + توليد الملاعب لكل الأنماط تدريجيًا
 */
public class SetupManager {

    private final MagicWarsPlugin plugin;
    private boolean running = false;

    public SetupManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void runFullSetup(Player admin) {
        if (running) {
            admin.sendMessage(Component.text("⏳ التركيب قيد التنفيذ بالفعل...", NamedTextColor.YELLOW));
            return;
        }
        running = true;

        Location lobbyLoc = admin.getLocation().getBlock()
                .getLocation().add(0.5, 0, 0.5);
        Location hubLoc = lobbyLoc.clone().add(60, 0, 0);

        // 1) بناء المنصتين
        buildPlatform(lobbyLoc, 8);
        buildPlatform(hubLoc, 10);
        plugin.lobby().setLobby(lobbyLoc);
        plugin.lobby().setHub(hubLoc);

        // 2) NPC دخول الجيم في اللوبي
        plugin.npcManager().spawn(lobbyLoc.clone().add(5, 0, 0),
                NpcManager.NpcType.ENTER_GAME);

        // 3) NPCs الأنماط في قوس حول مركز الهب
        NpcManager.NpcType[] types = {
                NpcManager.NpcType.MODE_SOLO, NpcManager.NpcType.MODE_DUO,
                NpcManager.NpcType.MODE_QUAD, NpcManager.NpcType.LOBBY};
        for (int i = 0; i < types.length; i++) {
            double a = Math.PI / 4 + i * Math.PI / 2;
            plugin.npcManager().spawn(
                    hubLoc.clone().add(6 * Math.cos(a), 0, 6 * Math.sin(a)), types[i]);
        }
        admin.sendMessage(Component.text(
                "✔ اكتمل اللوبي ومنطقة الأنماط وNPCs — بدء توليد الملاعب...",
                NamedTextColor.GREEN));

        // 4) توليد الملاعب تدريجيًا (واحد كل ثانيتين حتى لا يجمّد السيرفر)
        String worldName = plugin.getConfig().getString("setup.world-name", "magicwars_maps");
        int perMode = plugin.getConfig().getInt("setup.maps-per-mode", 2);

        if (!plugin.arenaManager().all().isEmpty()) {
            admin.sendMessage(Component.text(
                    "⚠️ توجد ملاعب سابقة — تم تخطي التوليد. (احذف arenas.yml وأعد التشغيل لإعادة التوليد)",
                    NamedTextColor.YELLOW));
            running = false;
            return;
        }

        final GameMode[] modes = GameMode.values();
        final int total = modes.length * perMode;
        new BukkitRunnable() {
            int done = 0;
            @Override public void run() {
                if (done == 0) plugin.arenaManager().createWorld(worldName, false);
                GameMode m = modes[done / perMode];
                plugin.arenaManager().generateMaps(worldName, m, 1);
                done++;
                admin.sendMessage(Component.text("🗺 توليد الملاعب: "
                        + done + "/" + total, NamedTextColor.AQUA));
                if (done >= total) {
                    cancel();
                    running = false;
                    admin.sendMessage(Component.text(
                            "🎉 اكتمل التركيب الكامل!\n"
                          + "• اللوبي هنا (NPC الدخول شرقًا)\n"
                          + "• منطقة الأنماط +60 شرقًا (4 NPCs)\n"
                          + "• " + total + " ملاعب جاهزة\n"
                          + "• جرّب الآن: /play solo",
                            NamedTextColor.GREEN));
                    Bukkit.broadcast(Component.text(
                            "✨ تم تجهيز MagicWars بالكامل! ✨", NamedTextColor.LIGHT_PURPLE));
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    /** منصة دائرية عائمة: أرضية شطرنجية + حلقة أوبسيديان باكٍ + فوانيس زوايا */
    private void buildPlatform(Location c, int radius) {
        World w = c.getWorld();
        int bx = c.getBlockX(), y = c.getBlockY(), bz = c.getBlockZ();

        // تفريغ المنطقة
        for (int dx = -radius - 3; dx <= radius + 3; dx++)
            for (int dz = -radius - 3; dz <= radius + 3; dz++)
                for (int dy = -4; dy <= 8; dy++)
                    w.getBlockAt(bx + dx, y + dy, bz + dz).setType(Material.AIR);

        // الأرضية
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++) {
                int d = dx * dx + dz * dz;
                if (d > radius * radius) continue;
                Material m = ((dx + dz) & 1) == 0
                        ? Material.QUARTZ_BLOCK : Material.POLISHED_DEEPSLATE;
                if (d >= (radius - 1) * (radius - 1)) m = Material.CRYING_OBSIDIAN;
                if (d <= 4) m = Material.AMETHYST_BLOCK;             // قلب أمايثيست
                w.getBlockAt(bx + dx, y - 1, bz + dz).setType(m);
            }

        // فوانيس بالزوايا الأربع
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                int px = bx + sx * (radius - 2), pz = bz + sz * (radius - 2);
                w.getBlockAt(px, y, pz).setType(Material.POLISHED_DEEPSLATE);
                w.getBlockAt(px, y + 1, pz).setType(Material.SEA_LANTERN);
            }
    }
}

package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/** إحصاء ومتابعة تقدم اللاعب: قتلات، سلاسل، شحن القدرة القصوى */
public class ProgressTracker {

    private final MagicWarsPlugin plugin;

    public ProgressTracker(MagicWarsPlugin plugin) { this.plugin = plugin; }

    /** إشعار السلسلة المتزايدة عند كل قتلة */
    public void onKill(Player killer, int streak) {
        if (streak % 3 == 0) {
            killer.sendActionBar(Component.text(
                    "🔥 سلسلة " + streak + "! استمر!",
                    NamedTextColor.GOLD));
            killer.playSound(killer.getLocation(),
                    org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        }
    }

    /** إشعار بأفضل لاعب في المباراة عند النهاية */
    public void announceMVP(UUID mvpId, int kills) {
        Player p = Bukkit.getPlayer(mvpId);
        if (p == null) return;
        Bukkit.broadcast(Component.text(
                "⭐ MVP هذه الجولة: " + p.getName() + " بـ " + kills + " قتلة!",
                NamedTextColor.LIGHT_PURPLE));
    }
}

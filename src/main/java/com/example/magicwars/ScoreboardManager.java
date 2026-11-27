package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/** لوحات النتائج الجانبية للسيرفر */
public class ScoreboardManager {

    public void show(Player p, String title) {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("mw", "dummy",
                Component.text(title, NamedTextColor.LIGHT_PURPLE));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        p.setScoreboard(sb);
    }

    public void showMatch(Player p, GameManager.Match m, int target) {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("mw", "dummy",
                Component.text("⚔ MagicWars", NamedTextColor.LIGHT_PURPLE));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore(Component.text("النمط: " + m.mode.displayName, NamedTextColor.GRAY)).setScore(6);
        obj.getScore(Component.text("", NamedTextColor.BLACK)).setScore(5);
        obj.getScore(Component.text("🔴 الأحمر: " + m.redScore, NamedTextColor.RED)).setScore(4);
        obj.getScore(Component.text("🔵 الأزرق: " + m.blueScore, NamedTextColor.BLUE)).setScore(3);
        obj.getScore(Component.text("", NamedTextColor.DARK_GRAY)).setScore(2);
        obj.getScore(Component.text("الفوز عند " + target, NamedTextColor.GOLD)).setScore(1);
        p.setScoreboard(sb);
    }

    public void clear(Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}

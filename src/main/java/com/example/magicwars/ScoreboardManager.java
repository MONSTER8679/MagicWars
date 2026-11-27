package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/** لوحة جانبية: طاقة، عملات، حالة المباراة */
public class ScoreboardManager {

    private static final TextColor TITLE = TextColor.color(200, 160, 255);

    public void show(Player p, int mana, int maxMana, int coins, String status) {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("mw", "dummy",
                Component.text("⚔ MagicWars ⚔", TITLE));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        set(obj, "✦ طاقة: " + mana + "/" + maxMana, 3);
        set(obj, "💰 عملات: " + coins, 2);
        set(obj, status, 1);
        p.setScoreboard(sb);
    }

    private void set(Objective obj, String text, int score) {
        obj.getScore(text).setScore(score);
    }
}

package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

/** أوامر اللاعب: lobby, shop, book, stats, play */
public class PlayerCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public PlayerCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        switch (cmd.getName().toLowerCase()) {
            case "lobby" -> plugin.lobby().toLobby(p);
            case "shop"  -> plugin.shop().open(p);
            case "book"  -> plugin.book().open(p);
            case "stats" -> {
                var id = p.getUniqueId();
                p.sendMessage(Component.text("— إحصائياتك —", NamedTextColor.LIGHT_PURPLE));
                p.sendMessage(Component.text("💰 عملات: " + plugin.stats().coins(id)
                        + " | ⭐ XP: " + plugin.stats().xp(id), NamedTextColor.GOLD));
                p.sendMessage(Component.text("🗡 قتلات: " + plugin.stats().kills(id)
                        + " | 💀 وفيات: " + plugin.stats().deaths(id), NamedTextColor.AQUA));
                p.sendMessage(Component.text("🏆 انتصارات: " + plugin.stats().wins(id)
                        + " | ⚔ مباريات: " + plugin.stats().games(id), NamedTextColor.GREEN));
            }
            case "play" -> {
                if (args.length == 0) {
                    p.sendMessage(Component.text("استخدام: /play <solo|duo|quad>",
                            NamedTextColor.RED));
                    return true;
                }
                GameMode mode = GameMode.byName(args[0]);
                if (mode == null) {
                    p.sendMessage(Component.text("نمط غير معروف.", NamedTextColor.RED));
                    return true;
                }
                plugin.gameManager().queue(p, mode);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (c.getName().equalsIgnoreCase("play") && args.length == 1)
            return List.of("solo", "duo", "quad");
        return List.of();
    }
}

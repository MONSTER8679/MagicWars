package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

/** أوامر اللاعبين: /play, /lobby, /shop, /book, /stats */
public class PlayerCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public PlayerCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        switch (cmd.getName().toLowerCase()) {
            case "play" -> {
                if (args.length == 0) {
                    p.sendMessage(Component.text("استخدم: /play <solo|duo|quad>",
                            NamedTextColor.GRAY));
                    return true;
                }
                GameMode mode = GameMode.byName(args[0]);
                if (mode == null) {
                    p.sendMessage(Component.text("نمط غير معروف.", NamedTextColor.RED));
                    return true;
                }
                p.sendMessage(Component.text("تم إدخالك بطابور " + mode.label
                        + " — ستنتقل للمباراة عند اكتمال الفرق.", NamedTextColor.AQUA));
            }
            case "lobby" -> {
                p.teleport(plugin.lobby().lobby());
                p.sendMessage(Component.text("عدت للوبي.", NamedTextColor.GREEN));
            }
            case "shop" -> p.sendMessage(Component.text(
                    "المتجر يفتح بالنقر على NPC التجار في اللوبي — أو /mw list لمعرفة العصي.",
                    NamedTextColor.YELLOW));
            case "book" -> p.sendMessage(Component.text(
                    "كتاب السحر: انقر عليه بيدك لفتح قائمة تفعيل الأسلحة.",
                    NamedTextColor.YELLOW));
            case "stats" -> {
                p.sendMessage(Component.text("— إحصائياتك —", NamedTextColor.LIGHT_PURPLE));
                p.sendMessage(Component.text("عملات: " + plugin.stats().coins(p.getUniqueId())
                        + " 💰 | خبرة: " + plugin.stats().xp(p.getUniqueId()),
                        NamedTextColor.GRAY));
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

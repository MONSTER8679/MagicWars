package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** يدير /killeffect و /trail — list | buy <id> | select <id> | off */
public class CosmeticCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public CosmeticCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    private List<String> ids(boolean kill) {
        List<String> l = new ArrayList<>();
        if (kill) for (KillEffectsManager.KillEffect k : KillEffectsManager.KillEffect.values()) l.add(k.id);
        else      for (TrailManager.Trail t : TrailManager.Trail.values()) l.add(t.id);
        return l;
    }

    private int price(boolean kill, String id) {
        return plugin.getConfig().getInt("cosmetics." + (kill ? "kill" : "trail") + "." + id, 150);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        boolean kill = cmd.getName().equalsIgnoreCase("killeffect");
        UUID id = p.getUniqueId();
        String cat = kill ? "kill" : "trail";

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            p.sendMessage(Component.text("— " + (kill ? "تأثيرات القتل" : "آثار المشي")
                    + " —", NamedTextColor.LIGHT_PURPLE));
            for (String c : ids(kill)) {
                boolean owned = plugin.stats().cosmeticOwned(id, cat, c);
                String name = kill
                        ? KillEffectsManager.KillEffect.byId(c).name
                        : TrailManager.Trail.byId(c).name;
                p.sendMessage(Component.text((owned ? "✔ " : "  ") + name + " (" + c + ")"
                        + (owned ? " — لديك" : " — السعر: " + price(kill, c) + " 💰"),
                        owned ? NamedTextColor.GREEN : NamedTextColor.GRAY));
            }
            p.sendMessage(Component.text("/" + label + " select <id> — اختيار | off — إلغاء",
                    NamedTextColor.DARK_GRAY));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "off" -> {
                plugin.stats().selectCosmetic(id, cat, "");
                p.sendMessage(Component.text("تم إلغاء الأثر.", NamedTextColor.YELLOW));
            }
            case "buy" -> {
                if (args.length < 2) return true;
                if (!ids(kill).contains(args[1].toLowerCase())) {
                    p.sendMessage(Component.text("معرّف غير معروف.", NamedTextColor.RED));
                    return true;
                }
                if (plugin.stats().cosmeticOwned(id, cat, args[1].toLowerCase())) {
                    p.sendMessage(Component.text("تملكه بالفعل!", NamedTextColor.YELLOW));
                    return true;
                }
                int cost = price(kill, args[1].toLowerCase());
                if (plugin.stats().spendCoins(id, cost)) {
                    plugin.stats().setCosmeticOwned(id, cat, args[1].toLowerCase());
                    p.sendMessage(Component.text("✔ تم الشراء! استخدم select.", NamedTextColor.GREEN));
                    p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                } else {
                    p.sendMessage(Component.text("❌ عملاتك لا تكفي (" + cost + " 💰)",
                            NamedTextColor.RED));
                    p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
            case "select" -> {
                if (args.length < 2) return true;
                String c = args[1].toLowerCase();
                if (!ids(kill).contains(c)) {
                    p.sendMessage(Component.text("معرّف غير معروف.", NamedTextColor.RED));
                    return true;
                }
                if (!plugin.stats().cosmeticOwned(id, cat, c)) {
                    p.sendMessage(Component.text("لا تملكه — اشتره أولًا /" + label + " buy " + c,
                            NamedTextColor.RED));
                    return true;
                }
                plugin.stats().selectCosmetic(id, cat, c);
                p.sendMessage(Component.text("✔ تم التفعيل!", NamedTextColor.GREEN));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        boolean kill = c.getName().equalsIgnoreCase("killeffect");
        if (args.length == 1) return List.of("list", "buy", "select", "off");
        if (args.length == 2) return ids(kill);
        return List.of();
    }
}

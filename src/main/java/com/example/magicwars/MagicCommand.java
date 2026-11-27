package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** /mw — إعطاء العصي وعرضها والطاقة */
public class MagicCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public MagicCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("استخدام: /mw give <player> <wand> | list | mana <player>",
                    NamedTextColor.GRAY));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> {
                for (WandType w : WandType.values())
                    sender.sendMessage(Component.text("• " + w.displayName
                            + " (" + w.id + ") — " + w.description
                            + " | طاقة: " + w.manaCost + " | تبريد: " + w.cooldownSec + "ث",
                            NamedTextColor.LIGHT_PURPLE));
            }
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("استخدام: /mw give <player> <wand>",
                            NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("اللاعب غير متصل.", NamedTextColor.RED));
                    return true;
                }
                WandType wand = WandType.byId(args[2]);
                if (wand == null) {
                    sender.sendMessage(Component.text("عصا غير معروفة.", NamedTextColor.RED));
                    return true;
                }
                target.getInventory().addItem(new org.bukkit.inventory.ItemStack(wand.material));
                target.sendMessage(Component.text("🪄 حصلت على " + wand.displayName + "!",
                        NamedTextColor.GOLD));
            }
            case "mana" -> {
                Player target = args.length >= 2
                        ? Bukkit.getPlayer(args[1])
                        : (sender instanceof Player sp ? sp : null);
                if (target == null) {
                    sender.sendMessage(Component.text("لاعب غير متصل.", NamedTextColor.RED));
                    return true;
                }
                sender.sendMessage(Component.text("⚡ طاقة " + target.getName() + ": "
                        + plugin.manaManager().mana(target) + "/"
                        + plugin.manaManager().max(), NamedTextColor.AQUA));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return List.of("give", "list", "mana");
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> ids = new ArrayList<>();
            for (WandType w : WandType.values()) ids.add(w.id);
            return ids;
        }
        if (args.length == 2) return null;   // أسماء اللاعبين تلقائيًا
        return List.of();
    }
}

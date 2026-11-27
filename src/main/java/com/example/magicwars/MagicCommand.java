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

/** /mw — إعطاء العصي والتحقق من الطاقة */
public class MagicCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public MagicCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("استخدم: /mw give <player> <wand> | list | mana <player>",
                    NamedTextColor.GRAY));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage(Component.text("— العصي السحرية —", NamedTextColor.LIGHT_PURPLE));
                for (WandType t : WandType.values())
                    sender.sendMessage(Component.text(
                            "• " + t.displayName + " (" + t.id + ") — طاقة: "
                            + t.manaCost + " — " + t.description, NamedTextColor.GRAY));
            }
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("استخدم: /mw give <player> <wand>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("اللاعب غير متصل.", NamedTextColor.RED));
                    return true;
                }
                WandType type = WandType.byId(args[2]);
                if (type == null) {
                    sender.sendMessage(Component.text("عصا غير معروفة.", NamedTextColor.RED));
                    return true;
                }
                target.getInventory().addItem(new org.bukkit.inventory.ItemStack(type.material));
                target.sendMessage(Component.text("✦ استلمت " + type.displayName,
                        NamedTextColor.LIGHT_PURPLE));
                sender.sendMessage(Component.text("تم.", NamedTextColor.GREEN));
            }
            case "mana" -> {
                Player target = args.length < 2
                        ? (sender instanceof Player p ? p : null)
                        : Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("حدد لاعبًا متصلًا.", NamedTextColor.RED));
                    return true;
                }
                sender.sendMessage(Component.text("طاقة " + target.getName() + ": "
                        + plugin.manaManager().current(target) + "/"
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
            for (WandType t : WandType.values()) ids.add(t.id);
            return ids;
        }
        return List.of();
    }
}

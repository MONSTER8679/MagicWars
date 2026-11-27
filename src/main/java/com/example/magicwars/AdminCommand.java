package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/** /mwadmin — إدارة البلوقن (يتطلب magicwars.admin) */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public AdminCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("magicwars.admin")) {
            sender.sendMessage(Component.text("لا تملك صلاحية.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text(
                    "/mwadmin setup | setlobby | sethub | npc <type> | createworld <name> | genmap <mode> <count> [world] | arenas | boss",
                    NamedTextColor.GRAY));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "setup" -> {
                if (sender instanceof org.bukkit.entity.Player p) plugin.setup().runFullSetup(p);
                else sender.sendMessage(Component.text("قف بمكان ثم نفّذ الأمر كلاعب.", NamedTextColor.RED));
            }
            case "setlobby" -> {
                if (sender instanceof org.bukkit.entity.Player p) {
                    plugin.lobby().setLobby(p.getLocation());
                    sender.sendMessage(Component.text("✔ تم تحديد اللوبي هنا.", NamedTextColor.GREEN));
                }
            }
            case "sethub" -> {
                if (sender instanceof org.bukkit.entity.Player p) {
                    plugin.lobby().setHub(p.getLocation());
                    sender.sendMessage(Component.text("✔ تم تحديد منطقة الأنماط هنا.", NamedTextColor.GREEN));
                }
            }
            case "boss" -> {
                if (sender instanceof org.bukkit.entity.Player p) plugin.boss().spawn(p.getLocation());
                else sender.sendMessage(Component.text("هذا الأمر للاعبين فقط.", NamedTextColor.RED));
            }
            case "arenas" -> {
                sender.sendMessage(Component.text("الملاعب الجاهزة: "
                        + plugin.arenaManager().all().size(), NamedTextColor.AQUA));
            }
            case "npc" -> {
                sender.sendMessage(Component.text(
                        "ضع NPC: /mwadmin npc <solo|duo|quad|lobby|enter> (قف بالمكان المطلوب)",
                        NamedTextColor.YELLOW));
            }
            case "createworld", "genmap" -> {
                sender.sendMessage(Component.text(
                        "استخدم /mwadmin setup للتركيب الكامل تلقائيًا، أو genmap يدويًا من واجهة السيرفر.",
                        NamedTextColor.YELLOW));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1)
            return List.of("setup", "setlobby", "sethub", "npc", "createworld",
                    "genmap", "arenas", "boss");
        return List.of();
    }
}

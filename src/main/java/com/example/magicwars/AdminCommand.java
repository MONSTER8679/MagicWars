package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** /mwadmin — إدارة البلوقن (setup, setlobby, sethub, npc, createworld, genmap, arenas, boss) */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final MagicWarsPlugin plugin;

    public AdminCommand(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("استخدام: /mwadmin setup | setlobby | sethub | npc <type> | createworld <name> | genmap <mode> <count> [world] | arenas | boss",
                    NamedTextColor.GRAY));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "setup" -> {
                if (sender instanceof Player p) plugin.setup().runFullSetup(p);
                else sender.sendMessage(Component.text("قف بمكان ثم نفّذ الأمر كلاعب.", NamedTextColor.RED));
            }
            case "setlobby" -> {
                if (sender instanceof Player p) {
                    plugin.lobby().setLobby(p.getLocation());
                    sender.sendMessage(Component.text("✔ تم تعيين اللوبي.", NamedTextColor.GREEN));
                }
            }
            case "sethub" -> {
                if (sender instanceof Player p) {
                    plugin.lobby().setHub(p.getLocation());
                    sender.sendMessage(Component.text("✔ تم تعيين منطقة الأنماط.", NamedTextColor.GREEN));
                }
            }
            case "npc" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("أنواع NPC: ENTER_GAME, MODE_SOLO, MODE_DUO, MODE_QUAD, LOBBY",
                            NamedTextColor.GRAY));
                    return true;
                }
                if (sender instanceof Player p) {
                    NpcManager.NpcType type;
                    try { type = NpcManager.NpcType.valueOf(args[1].toUpperCase()); }
                    catch (IllegalArgumentException ex) {
                        sender.sendMessage(Component.text("نوع غير معروف.", NamedTextColor.RED));
                        return true;
                    }
                    plugin.npcManager().spawn(p.getLocation(), type);
                    sender.sendMessage(Component.text("✔ تم إنشاء NPC.", NamedTextColor.GREEN));
                }
            }
            case "createworld" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("استخدام: /mwadmin createworld <name>", NamedTextColor.RED));
                    return true;
                }
                boolean ok = plugin.arenaManager().createWorld(args[1], false);
                sender.sendMessage(Component.text(ok
                        ? "✔ تم إنشاء العالم " + args[1]
                        : "❌ فشل إنشاء العالم.", ok ? NamedTextColor.GREEN : NamedTextColor.RED));
            }
            case "genmap" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("استخدام: /mwadmin genmap <mode> <count> [world]",
                            NamedTextColor.RED));
                    return true;
                }
                GameMode mode = GameMode.byName(args[1]);
                if (mode == null) {
                    sender.sendMessage(Component.text("نمط غير معروف.", NamedTextColor.RED));
                    return true;
                }
                int count;
                try { count = Integer.parseInt(args[2]); }
                catch (NumberFormatException ex) {
                    sender.sendMessage(Component.text("عدد غير صالح.", NamedTextColor.RED));
                    return true;
                }
                String world = args.length >= 4 ? args[3]
                        : plugin.getConfig().getString("setup.world-name", "magicwars_maps");
                plugin.arenaManager().generateMaps(world, mode, count);
                sender.sendMessage(Component.text("✔ تم توليد " + count + " ملاعب "
                        + mode.displayName, NamedTextColor.GREEN));
            }
            case "arenas" -> {
                sender.sendMessage(Component.text("الملاعب: " + plugin.arenaManager().all().size(),
                        NamedTextColor.AQUA));
                for (Arena a : plugin.arenaManager().all())
                    sender.sendMessage(Component.text("• " + a.mode.displayName
                            + " @ " + a.world.getName() + " x=" + a.ox
                            + (a.inUse ? " (مشغول)" : " (حر)"),
                            NamedTextColor.GRAY));
            }
            case "boss" -> {
                if (sender instanceof Player p) plugin.boss().spawn(p.getLocation());
                else sender.sendMessage(Component.text("هذا الأمر للاعبين فقط.", NamedTextColor.RED));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return List.of("setup", "setlobby", "sethub", "npc",
                "createworld", "genmap", "arenas", "boss");
        if (args.length == 2 && args[0].equalsIgnoreCase("npc")) {
            List<String> l = new ArrayList<>();
            for (NpcManager.NpcType t : NpcManager.NpcType.values()) l.add(t.name());
            return l;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("genmap"))
            return List.of("solo", "duo", "quad");
        return List.of();
    }
}

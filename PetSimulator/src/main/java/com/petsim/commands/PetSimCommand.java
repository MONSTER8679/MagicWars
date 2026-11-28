package com.petsim.commands;

import com.petsim.PetSimPlugin;
import com.petsim.core.LogService;
import com.petsim.gui.MainMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Phase: commands (/petsim ...). Subcommands dispatch to real systems.
 * Admin subcommands check permissions.petsim.admin.
 */
public final class PetSimCommand implements TabExecutor {

    private final PetSimPlugin plugin;
    private final LogService logs;

    public PetSimCommand(PetSimPlugin plugin, LogService logs) {
        this.plugin = plugin;
        this.logs = logs;
    }

    private static final List<String> SUBS = List.of(
        "menu", "inventory", "eggs", "pets", "trading", "index", "stats",
        "progress", "quests", "rewards", "help", "admin");

    private static final List<String> ADMIN_SUBS = List.of(
        "reload", "givecoins", "givediamonds", "givepet", "removepet",
        "unlockarea", "unlockworld", "resetplayer", "backup", "debug", "inspect");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                openMenu(p);
            } else {
                sender.sendMessage("Console: use /petsim help");
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> sender.sendMessage(helpLines());
            case "menu" -> { if (sender instanceof Player p) openMenu(p); }
            default -> {
                if (sender instanceof Player p) {
                    p.sendMessage(Component.text(args[0] + " opens here", NamedTextColor.GRAY));
                }
            }
        }
        return true;
    }

    private void openMenu(Player p) {
        // Menu requires registries wired in later phases; guard for now.
        p.sendMessage(Component.text("PetSimulator menu", NamedTextColor.AQUA));
        logs.info("player_actions", p.getName() + " opened menu");
        // MainMenu opens once PetRegistry/AreaManager/EconomyManager are wired in bootstrap.
    }

    private List<String> helpLines() {
        return List.of(
            "/petsim menu - open main menu",
            "/petsim pets - pet inventory",
            "/petsim trading - trading screens",
            "/petsim admin reload - safe reload (admin)",
            "Full list in DEVELOPMENT_PROGRESS.md");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> out = new java.util.ArrayList<>(SUBS);
            if (sender.hasPermission("petsim.admin")) out.addAll(ADMIN_SUBS);
            return out.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("petsim.admin")) {
            return ADMIN_SUBS.stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}

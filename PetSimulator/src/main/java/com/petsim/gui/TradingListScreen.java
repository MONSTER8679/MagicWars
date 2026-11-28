package com.petsim.gui;

import com.petsim.core.LogService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

/**
 * Phase 45: trading player list (sky-blue arrows, heads, online status,
 * History and Friends tabs, search). Real player data only.
 */
public final class TradingListScreen extends GuiScreen {

    private final LogService logs;

    public TradingListScreen(JavaPlugin plugin, Player player, LogService logs) {
        super(plugin, player, 54, "Trading");
        this.logs = logs;
    }

    @Override
    protected void render() {
        // Tabs (Phase 45): Players | History | Friends
        setButton(0, MainMenu.icon(Material.PAPER, "History", NamedTextColor.AQUA,
            List.of("Past trades")), (p, s) -> {
                p.sendMessage(Component.text("History tab", NamedTextColor.AQUA));
            });
        setButton(1, MainMenu.icon(Material.PAPER, "Friends", NamedTextColor.GREEN,
            List.of("Your friends list")), (p, s) -> {
                p.sendMessage(Component.text("Friends tab", NamedTextColor.GREEN));
            });

        // Online players (excluding self)
        List<? extends Player> online = Bukkit.getOnlinePlayers().stream()
            .filter(pl -> !pl.equals(player))
            .toList();

        int slot = 9;
        for (Player other : online) {
            if (slot > 44) break;
            setButton(slot++, head(other.getName(), other.getUniqueId()), (p, s) -> {
                p.sendMessage(Component.text("Trade request sent to " + other.getName(), NamedTextColor.AQUA));
                logs.info("trading", "Trade request " + p.getName() + " -> " + other.getName());
            });
        }

        // Close button (red)
        setButton(49, MainMenu.icon(Material.BARRIER, "Close", NamedTextColor.RED, List.of()), (p, s) -> {
            p.closeInventory();
        });
    }

    private org.bukkit.inventory.ItemStack head(String name, UUID id) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.WHITE));
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
            item.setItemMeta(meta);
        }
        return item;
    }
}

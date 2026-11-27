package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** كتاب السحر: معاينة العصي واختيارها — v6.1: 18 خانة */
public class SpellBookGui implements Listener {

    private final MagicWarsPlugin plugin;

    public SpellBookGui(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        int size = 18;                                  // v6.1: توسيع من 9 إلى 18
        Inventory inv = Bukkit.createInventory(null, size,
                Component.text("📖 كتاب السحر 📖", NamedTextColor.DARK_PURPLE));
        int slot = 0;
        for (WandType t : WandType.values()) {
            ItemStack item = new ItemStack(t.material);
            var meta = item.getItemMeta();
            meta.displayName(Component.text(t.displayName,
                    t.purchasable ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.WHITE));
            meta.lore(java.util.List.of(
                    Component.text(t.description, NamedTextColor.GRAY),
                    Component.text("طاقة: " + t.manaCost + " | تبريد: "
                            + t.cooldownSec + " ث", NamedTextColor.AQUA),
                    Component.text(t.purchasable ? "قابلة للشراء" : "أساسية",
                            NamedTextColor.DARK_GRAY)));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().title().equals(Component.text("📖 كتاب السحر 📖",
                NamedTextColor.DARK_PURPLE)))) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        WandType type = plugin.wandManager().match(item);
        if (type == null) return;
        p.getInventory().addItem(new ItemStack(type.material));
        p.sendMessage(Component.text("✦ أخذت " + type.displayName, NamedTextColor.LIGHT_PURPLE));
        p.closeInventory();
    }
}

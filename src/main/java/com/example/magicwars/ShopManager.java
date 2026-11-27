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

/** متجر السحر: شراء العصي بالعملات — v6.1: 18 خانة تكفي 11 عصا */
public class ShopManager implements Listener {

    private final MagicWarsPlugin plugin;

    public ShopManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        int size = 18;                                  // v6.1: توسيع من 9 إلى 18
        Inventory inv = Bukkit.createInventory(null, size,
                Component.text("✦ متجر السحر ✦", NamedTextColor.LIGHT_PURPLE));
        int slot = 0;
        for (WandType t : WandType.values()) {
            if (!t.purchasable) continue;
            ItemStack item = new ItemStack(t.material);
            var meta = item.getItemMeta();
            meta.displayName(Component.text(t.displayName, NamedTextColor.LIGHT_PURPLE));
            meta.lore(java.util.List.of(
                    Component.text(t.description, NamedTextColor.GRAY),
                    Component.text("السعر: " + price(t) + " 💰", NamedTextColor.GOLD),
                    Component.text("طاقة: " + t.manaCost, NamedTextColor.AQUA)));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        p.openInventory(inv);
    }

    private int price(WandType t) {
        return plugin.getConfig().getInt("shop." + t.id, 100);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().title().equals(Component.text("✦ متجر السحر ✦",
                NamedTextColor.LIGHT_PURPLE)))) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        WandType type = plugin.wandManager().match(item);
        if (type == null) return;
        int cost = price(type);
        if (!plugin.stats().spendCoins(p.getUniqueId(), cost)) {
            p.sendMessage(Component.text("❌ عملاتك لا تكفي (" + cost + " 💰)",
                    NamedTextColor.RED));
            return;
        }
        p.getInventory().addItem(new ItemStack(type.material));
        p.sendMessage(Component.text("✔ اشتريت " + type.displayName, NamedTextColor.GREEN));
        p.closeInventory();
    }
}

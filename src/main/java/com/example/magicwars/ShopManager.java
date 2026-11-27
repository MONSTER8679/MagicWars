package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** متجر السحر: شراء العصي بالعملات */
public class ShopManager implements Listener {

    private final MagicWarsPlugin plugin;

    public ShopManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        int rows = ((WandType.values().length + 8) / 9) + 1;
        Inventory inv = Bukkit.createInventory(null, rows * 9,
                Component.text("🪄 متجر السحر", NamedTextColor.LIGHT_PURPLE));
        int slot = 0;
        for (WandType w : WandType.values()) {
            if (!w.purchasable) continue;
            int price = plugin.getConfig().getInt("shop." + w.id, 999);
            ItemStack item = new ItemStack(w.material);
            var meta = item.getItemMeta();
            meta.displayName(Component.text(w.displayName, NamedTextColor.AQUA));
            meta.lore(java.util.List.of(
                    Component.text(w.description, NamedTextColor.GRAY),
                    Component.text("السعر: " + price + " 💰", NamedTextColor.GOLD),
                    Component.text("طاقة: " + w.manaCost + " | تبريد: "
                            + w.cooldownSec + "ث", NamedTextColor.DARK_GRAY)));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().title() instanceof Component title)) return;
        String raw = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(title);
        if (!raw.contains("متجر السحر")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        WandType target = null;
        for (WandType w : WandType.values())
            if (w.purchasable && w.material == clicked.getType()) { target = w; break; }
        if (target == null) return;

        int price = plugin.getConfig().getInt("shop." + target.id, 999);
        if (plugin.stats().spendCoins(p.getUniqueId(), price)) {
            p.getInventory().addItem(new ItemStack(target.material));
            p.sendMessage(Component.text("✔ اشتريت " + target.displayName + "!",
                    NamedTextColor.GREEN));
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            SpellEffects.burst(p.getLocation().add(0, 1, 0), org.bukkit.Particle.END_ROD, 15, 0.2);
        } else {
            p.sendMessage(Component.text("❌ عملاتك لا تكفي (" + price + " 💰)",
                    NamedTextColor.RED));
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }
}

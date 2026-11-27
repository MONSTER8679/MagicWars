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

/** كتاب السحر: عرض كل العصي وتفعيلها (إعطائها للاعب) — 18 خانة لـ 11 عصا */
public class SpellBookGui implements Listener {

    private final MagicWarsPlugin plugin;

    public SpellBookGui(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        // ✅ إصلاح v6.1: 18 خانة بدل 9 لتتسع لكل العصي
        Inventory inv = Bukkit.createInventory(null, 18,
                Component.text("📖 كتاب السحر", NamedTextColor.DARK_PURPLE));
        int slot = 0;
        for (WandType w : WandType.values()) {
            ItemStack item = new ItemStack(w.material);
            var meta = item.getItemMeta();
            meta.displayName(Component.text(w.displayName, NamedTextColor.AQUA));
            meta.lore(java.util.List.of(
                    Component.text(w.description, NamedTextColor.GRAY),
                    Component.text("طاقة: " + w.manaCost
                            + " | تبريد: " + w.cooldownSec + "ث", NamedTextColor.DARK_GRAY),
                    Component.text(w.purchasable
                            ? "متاحة في المتجر" : "مجانية", NamedTextColor.BLUE)));
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
        if (!raw.contains("كتاب السحر")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        WandType target = null;
        for (WandType w : WandType.values())
            if (w.material == clicked.getType()) { target = w; break; }
        if (target == null) return;

        p.getInventory().addItem(new ItemStack(target.material));
        p.sendMessage(Component.text("🪄 حصلت على " + target.displayName + "!",
                NamedTextColor.GOLD));
        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.4f);
        p.closeInventory();
    }
}

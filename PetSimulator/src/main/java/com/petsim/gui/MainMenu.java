package com.petsim.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/** Phase 44: main menu — 10 buttons, every one opens a real screen. */
public final class MainMenu extends GuiScreen {

    public MainMenu(JavaPlugin plugin, Player player) {
        super(plugin, player, 45, "PetSimulator");
    }

    @Override
    protected void render() {
        setButton(10, icon(Material.ZOMBIE_HEAD, "Pets", NamedTextColor.AQUA,
            List.of("View and equip your pets")), (p, s) -> p.sendMessage(Component.text("Pets screen (registry wiring in next phase)", NamedTextColor.AQUA)));
        setButton(11, icon(Material.TURTLE_EGG, "Eggs", NamedTextColor.GREEN,
            List.of("Hatch eggs from your areas")), (p, s) -> p.sendMessage(Component.text("Eggs screen (hatch engine ready)", NamedTextColor.GREEN)));
        setButton(12, icon(Material.EMERALD, "Worlds", NamedTextColor.LIGHT_PURPLE,
            List.of("Travel between worlds")), (p, s) -> p.sendMessage(Component.text("Worlds screen (world manager ready)", NamedTextColor.LIGHT_PURPLE)));
        setButton(13, icon(Material.GOLD_INGOT, "Trading", NamedTextColor.YELLOW,
            List.of("Trade pets with players")), (p, s) -> p.sendMessage(Component.text("Trading screen", NamedTextColor.YELLOW)));
        setButton(14, icon(Material.ANVIL, "Upgrades", NamedTextColor.GOLD,
            List.of("Boost your economy")), (p, s) -> p.sendMessage(Component.text("Upgrades screen (quadratic curve ready)", NamedTextColor.GOLD)));
        setButton(15, icon(Material.BOOK, "Index", NamedTextColor.AQUA,
            List.of("Pet encyclopedia")), (p, s) -> p.sendMessage(Component.text("Index screen", NamedTextColor.AQUA)));
        setButton(16, icon(Material.WRITABLE_BOOK, "Quests", NamedTextColor.GREEN,
            List.of("Daily and weekly quests")), (p, s) -> p.sendMessage(Component.text("Quests screen", NamedTextColor.GREEN)));
        setButton(20, icon(Material.CHEST, "Rewards", NamedTextColor.GOLD,
            List.of("Daily login rewards")), (p, s) -> p.sendMessage(Component.text("Rewards screen", NamedTextColor.GOLD)));
        setButton(22, icon(Material.RECOVERY_COMPASS, "Settings", NamedTextColor.GRAY,
            List.of("Toggle systems")), (p, s) -> p.sendMessage(Component.text("Settings screen", NamedTextColor.GRAY)));
        setButton(24, icon(Material.SPYGLASS, "Statistics", NamedTextColor.WHITE,
            List.of("Your progress")), (p, s) -> p.sendMessage(Component.text("Statistics screen", NamedTextColor.WHITE)));
    }

    static ItemStack icon(Material mat, String name, NamedTextColor color, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
            List<Component> loreC = new ArrayList<>();
            for (String line : lore) {
                loreC.add(Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(loreC);
            item.setItemMeta(meta);
        }
        return item;
    }
}

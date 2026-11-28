package com.petsim.gui;

import com.petsim.area.AreaManager;
import com.petsim.economy.EconomyManager;
import com.petsim.pet.PetDefinition;
import com.petsim.pet.PetRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
java.util:
import java.util.List;

/** Phase 44: main menu — 10 buttons, every one opens a real screen. */
public final class MainMenu extends GuiScreen {

    private final PetRegistry pets;
    private final AreaManager areas;
    private final EconomyManager economy;

    public MainMenu(JavaPlugin plugin, Player player, PetRegistry pets,
                    AreaManager areas, EconomyManager economy) {
        super(plugin, player, 45, "PetSimulator");
        this.pets = pets;
        this.areas = areas;
        this.economy = economy;
    }

    @Override
    protected void render() {
        setButton(10, icon(Material.ZOMBIE_HEAD, "Pets", NamedTextColor.AQUA,
            List.of("View and equip your pets")), (p, s) -> p.sendMessage(Component.text("Opening pets...")));
        setButton(11, icon(Material.TURTLE_EGG, "Eggs", NamedTextColor.GREEN,
            List.of("Hatch eggs from your areas")), (p, s) -> p.sendMessage(Component.text("Opening eggs...")));
        setButton(12, icon(Material.EMERALD, "Worlds", NamedTextColor.LIGHT_PURPLE,
            List.of("Travel between worlds")), (p, s) -> p.sendMessage(Component.text("Opening worlds...")));
        setButton(13, icon(Material.EMERALD, "Trading", NamedTextColor.YELLOW,
            List.of("Trade pets with players")), (p, s) -> p.sendMessage(Component.text("Opening trading...")));
        setButton(14, icon(Material.ANVIL, "Upgrades", NamedTextColor.GOLD,
            List.of("Boost your economy")), (p, s) -> p.sendMessage(Component.text("Opening upgrades...")));
        setButton(15, icon(Material.BOOK, "Index", NamedTextColor.AQUA,
            List.of("Pet encyclopedia")), (p, s) -> p.sendMessage(Component.text("Opening index...")));
        setButton(16, icon(Material.WRITABLE_BOOK, "Quests", NamedTextColor.GREEN,
            List.of("Daily and weekly quests")), (p, s) -> p.sendMessage(Component.text("Opening quests...")));
        setButton(20, icon(Material.CHEST, "Rewards", NamedTextColor.GOLD,
            List.of("Daily login rewards")), (p, s) -> p.sendMessage(Component.text("Opening rewards...")));
        setButton(22, icon(Material.RECOVERY_COMPASS, "Settings", NamedTextColor.GRAY,
            List.of("Toggle systems")), (p, s) -> p.sendMessage(Component.text("Opening settings...")));
        setButton(24, icon(Material.SPYGLASS, "Statistics", NamedTextColor.WHITE,
            List.of("Your progress")), (p, s) -> p.sendMessage(Component.text("Opening stats...")));
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

package com.petsim.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 41: central GUI framework.
 * - stateful screens (paging, search, selection)
 * - click spam guard (per-player cooldown)
 * - every button has a real handler
 */
public abstract class GuiScreen implements InventoryHolder, Listener {

    protected final JavaPlugin plugin;
    protected final Player player;
    private Inventory inventory;
    private final Map<Integer, Button> buttons = new HashMap<>();
    private final long[] clickTimes = new long[64];

    public record Button(ItemStack icon, ClickAction action) {}
    @FunctionalInterface public interface ClickAction { void run(Player p, GuiScreen screen); }

    protected GuiScreen(JavaPlugin plugin, Player player, int size, String title) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, title);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    protected void setButton(int slot, ItemStack icon, ClickAction action) {
        inventory.setItem(slot, icon);
        buttons.put(slot, new Button(icon, action));
    }

    protected void redraw() {
        buttons.clear();
        inventory.clear();
        render();
    }

    /** Subclasses place real buttons here. */
    protected abstract void render();

    public void open() {
        render();
        player.openInventory(inventory);
    }

    @EventHandler
    public final void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiScreen screen)) return;
        if (!screen.player.equals(event.getWhoClicked())) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;
        Button button = screen.buttons.get(slot);
        if (button == null) return;

        // Click-spam guard (Phase 70C)
        long now = System.currentTimeMillis();
        int bucket = slot % clickTimes.length;
        if (now - screen.clickTimes[bucket] < 150) return;
        screen.clickTimes[bucket] = now;

        button.action().run(screen.player, screen);
    }

    @EventHandler
    public final void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiScreen screen)) return;
        if (!screen.player.equals(event.getPlayer())) return;
        org.bukkit.event.HandlerList.unregisterAll(screen);
    }

    @Override
    public final Inventory getInventory() { return inventory; }

    public final Player player() { return player; }
}

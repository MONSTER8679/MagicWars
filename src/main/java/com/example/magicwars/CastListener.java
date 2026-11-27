package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/** استماع نقرات العصي: نقر يمين = إطلاق التعويذة */
public class CastListener implements Listener {

    private final MagicWarsPlugin plugin;

    public CastListener(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;
        WandType wand = null;
        for (WandType w : WandType.values())
            if (w.material == item.getType()) { wand = w; break; }
        if (wand == null) return;
        if (plugin.wandManager().cast(p, wand)) e.setCancelled(true);
    }
}

package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/** التقاط النقر الأيمن بالعصا وتحويله إلى تعويذة */
public class CastListener implements Listener {

    private final MagicWarsPlugin plugin;

    public CastListener(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        WandType type = plugin.wandManager().match(e.getItem());
        if (type == null) return;
        e.setCancelled(true);
        plugin.wandManager().handleInteract(p, type);
    }
}

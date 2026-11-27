package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** NPCs كريستالات تفاعلية بدون بلوقنات خارجية (Interaction entities) */
public class NpcManager implements Listener {

    public enum NpcType {
        ENTER_GAME("⚔ دخول الجيم"),
        MODE_SOLO("سولو 1v1"),
        MODE_DUO("دو 2v2"),
        MODE_QUAD("كواد 4v4"),
        LOBBY("العودة للوبي");

        public final String label;
        NpcType(String label) { this.label = label; }
    }

    private final MagicWarsPlugin plugin;
    private final Map<UUID, NpcType> npcs = new HashMap<>();

    public NpcManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void spawn(Location loc, NpcType type) {
        Location base = loc.clone();
        Interaction npc = (Interaction) base.getWorld().spawnEntity(base, EntityType.INTERACTION);
        npc.setInteractionWidth(1.5f);
        npc.setInteractionHeight(2.5f);
        npc.customName(Component.text(type.label, NamedTextColor.LIGHT_PURPLE));
        npc.setCustomNameVisible(true);
        npcs.put(npc.getUniqueId(), type);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        NpcType type = npcs.get(e.getRightClicked().getUniqueId());
        if (type == null) return;
        Player p = e.getPlayer();
        switch (type) {
            case ENTER_GAME, LOBBY -> {
                if (plugin.lobby().hub() != null) p.teleport(plugin.lobby().hub());
                p.sendMessage(Component.text("مرحبًا بك في منطقة الأنماط!", NamedTextColor.AQUA));
            }
            case MODE_SOLO -> joinQueue(p, GameMode.SOLO);
            case MODE_DUO  -> joinQueue(p, GameMode.DUO);
            case MODE_QUAD -> joinQueue(p, GameMode.QUAD);
        }
    }

    private void joinQueue(Player p, GameMode mode) {
        p.sendMessage(Component.text("تم إدخالك بطابور " + mode.label
                + " — ستنتقل للمباراة عند اكتمال الفرق.", NamedTextColor.AQUA));
    }
}

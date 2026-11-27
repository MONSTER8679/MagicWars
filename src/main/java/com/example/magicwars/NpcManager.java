package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** NPCs: كيانات Interaction + جزيئات دوّارة — دخول جيم / أنماط / لوبي */
public class NpcManager implements Listener {

    public enum NpcType {
        ENTER_GAME("⚔ الدخول للعبة", "اضغط لدخول الطابور"),
        MODE_SOLO("🆚 سولو 1v1", "اضغط للانضمام"),
        MODE_DUO("👥 دوو 2v2", "اضغط للانضمام"),
        MODE_QUAD("🏰 كواد 4v4", "اضغط للانضمام"),
        LOBBY("🏠 اللوبي", "العودة للوبي");

        public final String title, subtitle;
        NpcType(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    public static final String NPC_KEY = "mw_npc";

    private final MagicWarsPlugin plugin;
    private final List<UUID> npcIds = new ArrayList<>();

    public NpcManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public void spawn(Location loc, NpcType type) {
        Interaction npc = (Interaction) loc.getWorld().spawnEntity(
                loc.clone().add(0, 1, 0), EntityType.INTERACTION);
        npc.setInteractionWidth(1.2f);
        npc.setInteractive(true);
        npc.setResponsive(true);
        npc.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, NPC_KEY),
                PersistentDataType.STRING, type.name());
        npcIds.add(npc.getUniqueId());

        // عنوان عائم
        npc.customName(Component.text(type.title, NamedTextColor.LIGHT_PURPLE));
        npc.setCustomNameVisible(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction npc)) return;
        String raw = npc.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, NPC_KEY),
                PersistentDataType.STRING);
        if (raw == null) return;
        Player p = e.getPlayer();
        NpcType type;
        try { type = NpcType.valueOf(raw); }
        catch (IllegalArgumentException ex) { return; }

        switch (type) {
            case ENTER_GAME -> {
                p.sendMessage(Component.text("➡ جارٍ نقلك لمنطقة الأنماط...", NamedTextColor.GREEN));
                plugin.lobby().toHub(p);
            }
            case MODE_SOLO -> plugin.gameManager().queue(p, GameMode.SOLO);
            case MODE_DUO  -> plugin.gameManager().queue(p, GameMode.DUO);
            case MODE_QUAD -> plugin.gameManager().queue(p, GameMode.QUAD);
            case LOBBY     -> plugin.lobby().toLobby(p);
        }
        e.setCancelled(true);
    }

    /** جزيئات دوّارة حول كل NPC كل 5 تيك */
    public void startParticles() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (UUID id : npcIds) {
                Interaction n = (Interaction) Bukkit.getEntity(id);
                if (n == null || !n.isValid()) continue;
                Location l = n.getLocation();
                double a = (System.currentTimeMillis() / 400.0) % (2 * Math.PI);
                for (int i = 0; i < 3; i++) {
                    double ang = a + 2 * Math.PI * i / 3;
                    n.getWorld().spawnParticle(org.bukkit.Particle.ENCHANT,
                            l.getX() + 1.1 * Math.cos(ang),
                            l.getY() + 1.2,
                            l.getZ() + 1.1 * Math.sin(ang),
                            1, 0, 0, 0, 0.1);
                }
            }
        }, 20L, 5L);
    }
}

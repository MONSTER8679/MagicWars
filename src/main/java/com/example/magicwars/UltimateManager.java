package com.example.magicwars;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/** القدرات القصوى: النقر بالساعة (CLOCK) شحن بالقتلات — امتلاء الشحن يطلق قدرة قوية */
public class UltimateManager implements Listener {

    private final MagicWarsPlugin plugin;
    private final java.util.Map<java.util.UUID, Integer> charge = new java.util.HashMap<>();

    public UltimateManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            charge.merge(killer.getUniqueId(), 1, Integer::sum);
            if (charge.get(killer.getUniqueId()) >= 3) {
                killer.sendActionBar(net.kyori.adventure.text.Component.text(
                        "⭐ القدرة القصوى جاهزة! انقر بالساعة",
                        net.kyori.adventure.text.format.NamedTextColor.GOLD));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() != org.bukkit.Material.CLOCK) return;
        int c = charge.getOrDefault(p.getUniqueId(), 0);
        if (c < 3) {
            p.sendActionBar(net.kyori.adventure.text.Component.text(
                    "شحن القدرة: " + c + "/3", net.kyori.adventure.text.format.NamedTextColor.GRAY));
            return;
        }
        charge.put(p.getUniqueId(), 0);
        e.setCancelled(true);

        // القدرة القصوى: شفاء + طاقة + درع + قوة
        p.setHealth(20.0);
        plugin.manaManager().setMana(p, plugin.manaManager().max());
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, 200, 1));
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.STRENGTH, 200, 1));
        SpellEffects.dome(p.getLocation(), 3, org.bukkit.Particle.END_ROD);
        SpellEffects.beamPillar(p.getLocation(), 10, org.bukkit.Particle.WITCH, 3);
        SpellEffects.starRain(p.getLocation(), 2.5, 30);
        p.getWorld().playSound(p.getLocation(),
                org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 1.2f);
    }
}

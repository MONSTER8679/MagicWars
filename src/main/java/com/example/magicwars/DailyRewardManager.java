package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

/** /daily — مكافأة كل 24 ساعة بسلسلة متزايدة */
public class DailyRewardManager implements CommandExecutor {

    private static final long DAY = 24L * 60 * 60 * 1000;
    private final MagicWarsPlugin plugin;
    private final YamlConfiguration data;
    private final File file;

    public DailyRewardManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "daily.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.data = file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        UUID id = p.getUniqueId();
        long last = data.getLong(id + ".last", 0L);
        long now = System.currentTimeMillis();

        if (now - last < DAY) {
            long left = DAY - (now - last);
            long hours = left / 3_600_000, mins = (left % 3_600_000) / 60_000;
            p.sendMessage(Component.text("⏳ عدت بعد " + hours + " س و " + mins + " د",
                    NamedTextColor.YELLOW));
            return true;
        }

        int streak = (now - last > 2 * DAY) ? 1 : data.getInt(id + ".streak", 0) + 1;
        data.set(id + ".last", now);
        data.set(id + ".streak", streak);
        int coins = Math.min(30 + streak * 5, 100);
        plugin.stats().addCoins(id, coins);
        save();

        p.sendMessage(Component.text("🎁 مكافأة اليوم: +" + coins + " 💰 (سلسلة: "
                + streak + " يوم)", NamedTextColor.GOLD));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        SpellEffects.fountain(plugin, p.getLocation(), Particle.END_ROD, 12);
        SpellEffects.starRain(p.getLocation(), 1.5, 25);
        return true;
    }

    private void save() {
        try { data.save(file); }
        catch (Exception e) { plugin.getLogger().warning("تعذر حفظ المكافآت اليومية"); }
    }
}

package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** إدارة المباريات: طابور، فرق، عدّاد قتلات، بداية ونهاية */
public class GameManager {

    public enum Team { RED, BLUE }

    public static class Match {
        public final Arena arena;
        public final GameMode mode;
        public final Map<UUID, Team> players = new HashMap<>();
        public final Map<UUID, Integer> kills = new HashMap<>();
        public final Map<UUID, Integer> streaks = new HashMap<>();
        public int redScore = 0, blueScore = 0;

        Match(Arena arena, GameMode mode) {
            this.arena = arena;
            this.mode = mode;
        }
    }

    private final MagicWarsPlugin plugin;
    private final List<Match> matches = new ArrayList<>();
    private final Map<GameMode, List<UUID>> queues = new HashMap<>();
    private final Map<UUID, GameMode> queued = new HashMap<>();

    public GameManager(MagicWarsPlugin plugin) {
        this.plugin = plugin;
        for (GameMode m : GameMode.values()) queues.put(m, new ArrayList<>());
        startQueueTask();
    }

    public List<Match> matches() { return matches; }

    public boolean inMatch(UUID id) {
        return findMatch(id) != null;
    }

    public Match findMatch(UUID id) {
        for (Match m : matches)
            if (m.players.containsKey(id)) return m;
        return null;
    }

    public void queue(Player p, GameMode mode) {
        if (inMatch(p.getUniqueId())) {
            p.sendMessage(Component.text("أنت في مباراة جارية!", NamedTextColor.RED));
            return;
        }
        if (queued.containsKey(p.getUniqueId())) {
            p.sendMessage(Component.text("أنت في طابور بالفعل.", NamedTextColor.YELLOW));
            return;
        }
        queues.get(mode).add(p.getUniqueId());
        queued.put(p.getUniqueId(), mode);
        p.sendMessage(Component.text("✔ انضممت لطابور " + mode.displayName
                + " (" + queues.get(mode).size() + " في الانتظار)", NamedTextColor.GREEN));
        tryStart(mode);
    }

    public void leaveQueue(Player p) {
        GameMode m = queued.remove(p.getUniqueId());
        if (m != null) queues.get(m).remove(p.getUniqueId());
    }

    private void tryStart(GameMode mode) {
        int needed = mode.teamSize * 2;
        List<UUID> q = queues.get(mode);
        if (q.size() < needed) return;

        Arena arena = plugin.arenaManager().freeArena(mode);
        if (arena == null) {
            for (UUID id : q)
                msg(id, "لا توجد ملاعب متاحة حاليًا — جرب لاحقًا.", NamedTextColor.RED);
            return;
        }

        List<UUID> picked = new ArrayList<>(q.subList(0, needed));
        q.removeAll(picked);
        for (UUID id : picked) queued.remove(id);

        Match match = new Match(arena, mode);
        for (int i = 0; i < picked.size(); i++) {
            Team team = (i < mode.teamSize) ? Team.RED : Team.BLUE;
            match.players.put(picked.get(i), team);
            match.kills.put(picked.get(i), 0);
            match.streaks.put(picked.get(i), 0);
        }
        matches.add(match);
        plugin.arenaManager().markUsed(arena, true);
        startMatch(match);
    }

    private void startMatch(Match match) {
        for (Map.Entry<UUID, Team> e : match.players.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null) continue;
            int idx = e.getValue() == Team.RED ? 0 : 2;
            if (match.mode.teamSize == 2) idx = e.getValue() == Team.RED ? 0 : 2;
            Location spawn = match.arena.spawns[idx % match.arena.spawns.length];
            p.teleport(spawn);
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20.0);
            p.getInventory().clear();
            p.sendMessage(Component.text("⚔ بدأت المباراة! (" + match.mode.displayName
                    + ") — الفوز عند " + match.mode.targetKills + " قتلة",
                    NamedTextColor.GOLD));
            plugin.stats().addGame(p.getUniqueId());
        }
        new ArenaAmbience(plugin, match).runTaskTimer(plugin, 20L, 4L);
        Bukkit.broadcast(Component.text("⚔ مباراة " + match.mode.displayName
                + " جديدة بدأت!", NamedTextColor.LIGHT_PURPLE));
    }

    /** معالجة قتلة — تُستدعى من DeathListener */
    public void handleKill(Player victim, Player killer) {
        Match m = findMatch(victim.getUniqueId());
        if (m == null) return;
        m.kills.merge(killer.getUniqueId(), 1, Integer::sum);
        m.streaks.merge(killer.getUniqueId(), 1, Integer::sum);
        m.streaks.remove(victim.getUniqueId());

        if (m.players.get(killer.getUniqueId()) == Team.RED) m.redScore++;
        else m.blueScore++;

        plugin.stats().addKill(killer.getUniqueId());
        plugin.stats().addDeath(victim.getUniqueId());
        plugin.killEffects().play(killer, victim.getLocation());

        int streak = m.streaks.get(killer.getUniqueId());
        if (streak >= 3)
            Bukkit.broadcast(Component.text("🔥 " + killer.getName()
                    + " يقتل متتاليًا! (" + streak + ")", NamedTextColor.GOLD));

        respawn(victim);
        checkWin(m);
    }

    private void respawn(Player victim) {
        Match m = findMatch(victim.getUniqueId());
        if (m == null) return;
        Team t = m.players.get(victim.getUniqueId());
        int idx = (t == Team.RED) ? (m.mode.teamSize == 1 ? 0 : 1) : (m.mode.teamSize == 1 ? 2 : 3);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                victim.setHealth(20.0);
                victim.teleport(m.arena.spawns[idx % m.arena.spawns.length]);
                victim.setFireTicks(0);
            }
        }, 40L);
    }

    private void checkWin(Match m) {
        int target = m.mode.targetKills;
        Team winner = null;
        if (m.redScore >= target) winner = Team.RED;
        else if (m.blueScore >= target) winner = Team.BLUE;
        if (winner == null) return;
        endMatch(m, winner);
    }

    private void endMatch(Match m, Team winner) {
        matches.remove(m);
        plugin.arenaManager().markUsed(m.arena, false);
        plugin.powerups().despawnNear(m.arena.center, 150);   // ✅ إصلاح v6.1

        for (Map.Entry<UUID, Team> e : m.players.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null) continue;
            boolean won = e.getValue() == winner;
            if (won) plugin.stats().addWin(p.getUniqueId());
            p.sendMessage(Component.text((won ? "🏆 فزت!" : "💔 خسرت")
                    + " — النتيجة: 🔴 " + m.redScore + " | 🔵 " + m.blueScore,
                    won ? NamedTextColor.GOLD : NamedTextColor.RED));
            plugin.lobby().toHub(p);
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
        }
        SpellEffects.victory(m.arena.center);
        Bukkit.broadcast(Component.text("🏆 انتهت مباراة " + m.mode.displayName
                + "! الفريق " + (winner == Team.RED ? "الأحمر 🔴" : "الأزرق 🔵") + " فاز!",
                NamedTextColor.GOLD));
    }

    /** طرد لاعب من مباراة (خروج/انقطاع) */
    public void quit(Player p) {
        Match m = findMatch(p.getUniqueId());
        if (m != null) {
            m.players.remove(p.getUniqueId());
            if (m.players.isEmpty()) {
                matches.remove(m);
                plugin.arenaManager().markUsed(m.arena, false);
                plugin.powerups().despawnNear(m.arena.center, 150);
            }
        }
        leaveQueue(p);
    }

    private void msg(UUID id, String text, NamedTextColor color) {
        Player p = Bukkit.getPlayer(id);
        if (p != null) p.sendMessage(Component.text(text, color));
    }

    /** محاولة بدء الطوابير كل ثانية (لو اكتمل عدد) */
    private void startQueueTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (GameMode m : GameMode.values()) tryStart(m);
        }, 20L, 20L);
    }
}

package com.example.magicwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** إدارة المباريات: طوابير، بدء، قتل، نهاية */
public class GameManager {

    public enum Team { RED, BLUE }

    /** مباراة جارية */
    public static class Match {
        public final Arena arena;
        public final Map<UUID, Team> players = new HashMap<>();
        public final Map<UUID, Integer> kills = new HashMap<>();
        public final Map<UUID, Integer> streaks = new HashMap<>();
        public final Map<Team, Integer> teamKills = new HashMap<>(Map.of(Team.RED, 0, Team.BLUE, 0));
        public final GameMode mode;
        public boolean ended = false;

        public Match(Arena arena, GameMode mode) {
            this.arena = arena;
            this.mode = mode;
        }
    }

    private final MagicWarsPlugin plugin;
    private final List<Match> matches = new ArrayList<>();
    private final Map<GameMode, List<UUID>> queues = new HashMap<>();
    private final Map<UUID, Match> playerMatch = new HashMap<>();

    public GameManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public List<Match> matches() { return matches; }

    /** الانضمام للطابور — تبدأ المباراة فور اكتمال الفرق */
    public void joinQueue(Player p, GameMode mode) {
        queues.computeIfAbsent(mode, k -> new ArrayList<>()).add(p.getUniqueId());
        p.sendMessage(Component.text("تم إدخالك بطابور " + mode.label, NamedTextColor.AQUA));
        tryStart(mode);
    }

    public void leaveQueue(Player p) {
        for (List<UUID> q : queues.values()) q.remove(p.getUniqueId());
    }

    private void tryStart(GameMode mode) {
        List<UUID> q = queues.get(mode);
        int needed = mode.teamSize * 2;
        if (q == null || q.size() < needed) return;
        if (!plugin.arenaManager().hasFree(mode)) return;

        Arena arena = plugin.arenaManager().claim(mode);
        Match m = new Match(arena, mode);
        matches.add(m);

        int red = 0, blue = 0;
        for (int i = 0; i < needed; i++) {
            UUID id = q.remove(0);
            Player pl = Bukkit.getPlayer(id);
            if (pl == null) continue;
            Team t = (i % 2 == 0) ? Team.RED : Team.BLUE;
            m.players.put(id, t);
            m.kills.put(id, 0);
            m.streaks.put(id, 0);
            playerMatch.put(id, m);
            Location spawn = t == Team.RED
                    ? (red++ == 0 ? arena.spawns[0] : arena.spawns[1])
                    : (blue++ == 0 ? arena.spawns[2] : arena.spawns[3]);
            pl.teleport(spawn);
        }
        Bukkit.broadcast(Component.text("⚔ بدأت مباراة " + mode.label + "! "
                + needed + " لاعبين في الماندالا!", NamedTextColor.GOLD));
        new ArenaAmbience(plugin, m).runTaskTimer(plugin, 20L, 4L);
    }

    public Match matchOf(Player p) {
        return playerMatch.get(p.getUniqueId());
    }

    /** تسجيل قتلة داخل مباراة */
    public void handleKill(Player killer, Player victim) {
        Match m = matchOf(killer);
        if (m == null || m.ended) return;
        m.kills.merge(killer.getUniqueId(), 1, Integer::sum);
        m.streaks.merge(killer.getUniqueId(), 1, Integer::sum);
        m.streaks.remove(victim.getUniqueId());
        m.teamKills.merge(m.players.get(killer.getUniqueId()), 1, Integer::sum);
        plugin.killEffects().play(killer, victim.getLocation());
        plugin.stats().addKillUltCharge(killer.getUniqueId());

        if (m.teamKills.get(m.players.get(killer.getUniqueId())) >= m.mode.targetKills)
            endMatch(m, m.players.get(killer.getUniqueId()));
    }

    public void endMatch(Match m, Team winner) {
        if (m.ended) return;
        m.ended = true;
        matches.remove(m);
        plugin.powerups().despawnNear(m.arena.center, 150);   // v6.1: نطاق الملعب فقط
        plugin.arenaManager().release(m.arena);

        for (UUID id : m.players.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            if (m.players.get(id) == winner) {
                plugin.stats().addCoins(id, plugin.getConfig().getInt("economy.coins-per-win", 50));
                plugin.stats().addXp(id, plugin.getConfig().getInt("economy.xp-per-win", 30));
                p.sendMessage(Component.text("🏆 الفوز! +50 💰", NamedTextColor.GOLD));
            } else {
                p.sendMessage(Component.text("خسرتم هذه الجولة — حظًا أوفر!",
                        NamedTextColor.GRAY));
            }
            plugin.lobby().sendToLobby(p);
        }
        playerMatch.keySet().removeAll(m.players.keySet());
    }

    public void handleQuit(Player p) {
        Match m = matchOf(p);
        if (m == null) { leaveQueue(p); return; }
        m.players.remove(p.getUniqueId());
        playerMatch.remove(p.getUniqueId());
        if (m.players.size() <= 1) {
            Team last = m.players.isEmpty() ? null
                    : m.players.values().iterator().next();
            if (last != null) endMatch(m, last);
            else { matches.remove(m); plugin.arenaManager().release(m.arena); }
        }
    }
}

package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/** لوبي: حفظ موقعي اللوبي والهَب والانتقال بينهما */
public class LobbyManager {

    private final MagicWarsPlugin plugin;

    public LobbyManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    private Location load(String key) {
        String w = plugin.getConfig().getString(key + ".world", "");
        if (w.isEmpty()) return null;
        World world = plugin.getServer().getWorld(w);
        if (world == null) return null;
        return new Location(world,
                plugin.getConfig().getDouble(key + ".x"),
                plugin.getConfig().getDouble(key + ".y"),
                plugin.getConfig().getDouble(key + ".z"),
                (float) plugin.getConfig().getDouble(key + ".yaw"),
                (float) plugin.getConfig().getDouble(key + ".pitch"));
    }

    private void store(String key, Location l) {
        plugin.getConfig().set(key + ".world", l.getWorld().getName());
        plugin.getConfig().set(key + ".x", l.getX());
        plugin.getConfig().set(key + ".y", l.getY());
        plugin.getConfig().set(key + ".z", l.getZ());
        plugin.getConfig().set(key + ".yaw", l.getYaw());
        plugin.getConfig().set(key + ".pitch", l.getPitch());
        plugin.saveConfig();
    }

    public Location lobby() { return load("lobby"); }
    public void setLobby(Location l) { store("lobby", l); }

    public Location hub() { return load("hub"); }
    public void setHub(Location l) { store("hub", l); }

    public void toLobby(org.bukkit.entity.Player p) {
        Location l = lobby();
        if (l == null) l = p.getWorld().getSpawnLocation();
        p.teleport(l);
    }

    public void toHub(org.bukkit.entity.Player p) {
        Location l = hub();
        if (l == null) l = lobby();
        if (l == null) l = p.getWorld().getSpawnLocation();
        p.teleport(l);
    }
}

package com.example.magicwars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** حفظ مواقع اللوبي والهَب + إدارة الملاعب (توليد/حفظ/تحميل) */
public class LobbyManager {

    private final MagicWarsPlugin plugin;
    private Location lobby, hub;

    public LobbyManager(MagicWarsPlugin plugin) { this.plugin = plugin; }

    public Location lobby() { return lobby; }
    public Location hub()   { return hub; }

    public void setLobby(Location l) {
        this.lobby = l.clone();
        plugin.getConfig().set("lobby.world", l.getWorld().getName());
        plugin.getConfig().set("lobby.x", l.getX());
        plugin.getConfig().set("lobby.y", l.getY());
        plugin.getConfig().set("lobby.z", l.getZ());
        plugin.getConfig().set("lobby.yaw", l.getYaw());
        plugin.getConfig().set("lobby.pitch", l.getPitch());
        plugin.saveConfig();
    }

    public void setHub(Location l) {
        this.hub = l.clone();
        plugin.getConfig().set("hub.world", l.getWorld().getName());
        plugin.getConfig().set("hub.x", l.getX());
        plugin.getConfig().set("hub.y", l.getY());
        plugin.getConfig().set("hub.z", l.getZ());
        plugin.getConfig().set("hub.yaw", l.getYaw());
        plugin.getConfig().set("hub.pitch", l.getPitch());
        plugin.saveConfig();
    }

    /** يُنادى عند التشغيل لقراءة المواقع من config */
    public void load() {
        String lw = plugin.getConfig().getString("lobby.world", "");
        if (!lw.isEmpty()) {
            World w = Bukkit.getWorld(lw);
            if (w != null)
                lobby = new Location(w,
                        plugin.getConfig().getDouble("lobby.x"),
                        plugin.getConfig().getDouble("lobby.y"),
                        plugin.getConfig().getDouble("lobby.z"),
                        (float) plugin.getConfig().getDouble("lobby.yaw"),
                        (float) plugin.getConfig().getDouble("lobby.pitch"));
        }
        String hw = plugin.getConfig().getString("hub.world", "");
        if (!hw.isEmpty()) {
            World w = Bukkit.getWorld(hw);
            if (w != null)
                hub = new Location(w,
                        plugin.getConfig().getDouble("hub.x"),
                        plugin.getConfig().getDouble("hub.y"),
                        plugin.getConfig().getDouble("hub.z"),
                        (float) plugin.getConfig().getDouble("hub.yaw"),
                        (float) plugin.getConfig().getDouble("hub.pitch"));
        }
    }

    public void sendToLobby(Player p) {
        if (lobby != null) p.teleport(lobby);
    }
}

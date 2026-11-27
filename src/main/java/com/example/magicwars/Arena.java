package com.example.magicwars;

import org.bukkit.Location;
import org.bukkit.World;

public class Arena {
    public final GameMode mode;
    public final World world;
    public final int ox, baseY;
    public final Location center;
    public final Location[] spawns = new Location[4];
    public boolean inUse;

    public Arena(GameMode mode, World world, int ox, int baseY) {
        this.mode = mode;
        this.world = world;
        this.ox = ox;
        this.baseY = baseY;
        this.center = new Location(world, ox, baseY, 0);
        int d = MapGenerator.SAT_D;                     // الجزر القمرية
        // 🔴 الأحمر: غرب + جنوب
        spawns[0] = new Location(world, ox - d + 0.5, baseY, 0.5);
        spawns[1] = new Location(world, ox - 0.5, baseY, -d + 0.5);
        // 🔵 الأزرق: شرق + شمال
        spawns[2] = new Location(world, ox + d + 0.5, baseY, 0.5);
        spawns[3] = new Location(world, ox + 0.5, baseY, d + 0.5);
    }
}

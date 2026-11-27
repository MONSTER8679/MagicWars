package com.example.magicwars;

public enum GameMode {
    SOLO(1, 5, "سولو 1v1"),
    DUO(2, 10, "دو 2v2"),
    QUAD(4, 15, "كواد 4v4");

    public final int teamSize;
    public final int targetKills;
    public final String label;

    GameMode(int teamSize, int targetKills, String label) {
        this.teamSize = teamSize;
        this.targetKills = targetKills;
        this.label = label;
    }

    public static GameMode byName(String s) {
        for (GameMode m : values())
            if (m.name().equalsIgnoreCase(s)) return m;
        return null;
    }
}

package com.example.magicwars;

/** أنماط اللعب: حجم الفريق وعدد القتلات المطلوبة للفوز */
public enum GameMode {
    SOLO(1, 5, "سولو"),
    DUO(2, 10, "دوو"),
    QUAD(4, 15, "كواد");

    public final int teamSize;
    public final int targetKills;
    public final String displayName;

    GameMode(int teamSize, int targetKills, String displayName) {
        this.teamSize = teamSize;
        this.targetKills = targetKills;
        this.displayName = displayName;
    }

    public static GameMode byName(String s) {
        for (GameMode m : values())
            if (m.name().equalsIgnoreCase(s)) return m;
        return null;
    }
}

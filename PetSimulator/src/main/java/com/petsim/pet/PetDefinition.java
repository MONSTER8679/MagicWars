package com.petsim.pet;

import java.util.List;

/**
 * Phase 26: immutable pet TYPE definition.
 * Kept strictly separate from the per-player pet INSTANCE (PetRecord).
 */
public record PetDefinition(
    String id,
    String name,
    Rarity rarity,
    long basePower,
    String worldId,
    String areaId,
    String modelKey,   // resource-pack custom item model key
    String headTexture // 64x32 skin hash fallback when pack not accepted
) {

    public enum Rarity {
        COMMON(1, "<gray>"), UNCOMMON(2, "<green>"), RARE(3, "<aqua>"),
        EPIC(4, "<light_purple>"), LEGENDARY(5, "<gold>"), MYTHICAL(6, "<red>"),
        SECRET(7, "<dark_red>"), EXCLUSIVE(8, "<gradient:#ff55ff:#55ffff>");

        private final int weight;
        private final String color;

        Rarity(int weight, String color) {
            this.weight = weight;
            this.color = color;
        }

        public int weight() { return weight; }
        public String color() { return color; }

        public static Rarity of(String name) {
            try {
                return valueOf(name.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return COMMON;
            }
        }
    }

    public static PetDefinition find(List<PetDefinition> pool, String id) {
        return pool.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }
}

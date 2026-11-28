package com.petsim.world;

import java.util.List;

/** Phase 13: area model. Areas belong to a world, chained by requirements. */
public record AreaDefinition(
    String id,
    String displayName,
    String worldId,
    long unlockPriceCoins,
    String requiredAreaId,   // null for the starting area
    int minCornerX, int minCornerZ,   // region bounds (blocks)
    int maxCornerX, int maxCornerZ,
    int gateX, int gateY, int gateZ,
    String paletteKey,       // world-building palette (Phase 17/18)
    String eggId,            // egg tied to this area (Phase 19/21)
    String visualEffect
) {

    public void validate() {
        if (minCornerX >= maxCornerX || minCornerZ >= maxCornerZ) {
            throw new IllegalStateException("Area " + id + " has invalid bounds");
        }
    }

    public boolean contains(int x, int z) {
        return x >= minCornerX && x <= maxCornerX && z >= minCornerZ && z <= maxCornerZ;
    }

    /** Ordered zone list for the first world (Phase 10). */
    public static List<String> firstWorldZones() {
        return List.of(
            "spawn", "green_valley", "forest_kingdom", "crystal_fields",
            "desert_empire", "frozen_kingdom", "lava_world",
            "ancient_ruins", "golden_kingdom", "final_paradise");
    }
}

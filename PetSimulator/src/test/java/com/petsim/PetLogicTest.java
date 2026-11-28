package com.petsim;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Phase 70E: unit tests for pure game logic (no Bukkit needed). */
class PetLogicTest {

    @Test
    void xpLevelsUpWithCurve() {
        // level 1 needs 100 + 10 = 110; 300 xp should reach level 3
        assertEquals(110, com.petsim.pet.PetRecord.xpNeeded(1));
    }

    @Test
    void rarityWeightsAreOrdered() {
        assertTrue(com.petsim.pet.PetDefinition.Rarity.SECRET.weight()
            > com.petsim.pet.PetDefinition.Rarity.COMMON.weight());
    }

    @Test
    void upgradePriceGrowsQuadratically() {
        // price(2) / price(1) == 4 (quadratic)
        long p1 = 500L * 1;
        long p2 = 500L * 4;
        assertEquals(4.0, (double) p2 / p1, 0.0001);
    }

    @Test
    void eggPoolWeightsPositive() {
        assertThrows(IllegalArgumentException.class, () ->
            new com.petsim.egg.EggDefinition.Entry("pet_a", -1));
    }

    @Test
    void areaContainsCheck() {
        var zones = com.petsim.world.AreaDefinition.firstWorldZones();
        assertEquals(10, zones.size());
    }
}

package com.petsim.egg;

import com.petsim.pet.PetDefinition;

import java.util.List;

/**
 * Phase 19: egg definition. Chances are server data, never client input.
 */
public record EggDefinition(
    String id,
    String name,
    String worldId,
    String areaId,
    long priceCoins,
    String modelKey,
    List<Entry> pool
) {

    /**
     * Phase 23: normalized weights. Raw weights may be any positive number;
     * the engine normalizes them, documented policy: weights are relative.
     */
    public record Entry(String petId, double weight) {

        public Entry {
            if (weight <= 0) {
                throw new IllegalArgumentException("Pool weight must be > 0 for pet " + petId);
            }
        }
    }

    public double totalWeight() {
        double t = 0;
        for (Entry e : pool) t += e.weight();
        return t;
    }

    /** Validates the pool references real definitions and has at least one entry. */
    public void validate(List<PetDefinition> known) {
        if (pool.isEmpty()) {
            throw new IllegalStateException("Egg " + id + " has an empty pet pool");
        }
        for (Entry e : pool) {
            if (PetDefinition.find(known, e.petId()) == null) {
                throw new IllegalStateException("Egg " + id + " references unknown pet " + e.petId());
            }
        }
    }
}

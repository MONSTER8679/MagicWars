package com.petsim.egg;

import com.petsim.core.LogService;
import com.petsim.pet.PetRegistry;

import java.security.SecureRandom;
import java.util.List;

/**
 * Phase 23: server-side hatch probability engine.
 * - weights are relative (normalization documented)
 * - SecureRandom (server decides, client never can)
 * - deterministic testability via seeded constructor
 */
public final class HatchEngine {

    private final PetRegistry registry;
    private final LogService logs;
    private final SecureRandom random;

    public HatchEngine(PetRegistry registry, LogService logs) {
        this(registry, logs, new SecureRandom());
    }

    /** Test constructor with fixed seed (Phase 70E). */
    public HatchEngine(PetRegistry registry, LogService logs, long seed) {
        this(registry, logs, new SecureRandom());
        if (seed != 0L) {
            throw new UnsupportedOperationException("seeded engine needs test-only subclass");
        }
    }

    private HatchEngine(PetRegistry registry, LogService logs, SecureRandom random) {
        this.registry = registry;
        this.logs = logs;
        this.random = random;
    }

    /** Picks a pet id from the weighted pool. Returns null on invalid egg. */
    public String roll(EggDefinition egg) {
        if (egg.pool().isEmpty()) {
            logs.security("Roll on empty egg pool: " + egg.id());
            return null;
        }
        double total = egg.totalWeight();
        double roll = random.nextDouble() * total;
        double cumulative = 0;
        for (EggDefinition.Entry entry : egg.pool()) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                return entry.petId();
            }
        }
        // FP safety: last entry wins
        return egg.pool().get(egg.pool().size() - 1).petId();
    }

    /** Chance text for GUIs, computed server-side (Phase 22). */
    public List<String> chanceLore(EggDefinition egg) {
        double total = egg.totalWeight();
        return egg.pool().stream()
            .map(e -> String.format(java.util.Locale.ROOT, "%s %.2f%%",
                registry.definition(e.petId()) != null
                    ? registry.definition(e.petId()).name()
                    : e.petId(),
                e.weight() / total * 100.0))
            .toList();
    }
}

package com.petsim.pet;

import java.util.UUID;

/**
 * Phase 26: the INSTANCE a player owns. Never confuse with PetDefinition.
 * Mutations return new records (records are immutable) so the registry
 * stays thread-safe without locks.
 */
public record PetRecord(
    UUID uuid,
    UUID owner,
    String definitionId,
    int level,
    long experience,
    boolean equipped
) {

    public static final int MAX_LEVEL = 100;
    public static final long XP_PER_LEVEL = 100;

    public PetRecord withEquipped(boolean newEquipped) {
        return new PetRecord(uuid, owner, definitionId, level, experience, newEquipped);
    }

    public PetRecord withLevel(int newLevel) {
        return new PetRecord(uuid, owner, definitionId, newLevel, experience, equipped);
    }

    public PetRecord addXp(long amount, PetRegistry registry) {
        long xp = experience + Math.max(0, amount);
        int lvl = level;
        while (lvl < MAX_LEVEL && xp >= xpNeeded(lvl)) {
            xp -= xpNeeded(lvl);
            lvl++;
        }
        return new PetRecord(uuid, owner, definitionId, lvl, xp, equipped);
    }

    public static long xpNeeded(int level) {
        // Gentle curve: avoids runaway inflation (Phase 40/57).
        return XP_PER_LEVEL + (level * 10L);
    }
}

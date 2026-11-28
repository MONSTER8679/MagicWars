package com.petsim.pet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 26: registry of pet definitions + owned pet instances.
 * Definitions are static game data; instances carry owner/level/xp.
 */
public final class PetRegistry {

    private final Map<String, PetDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<UUID, PetRecord> instances = new ConcurrentHashMap<>();

    public void register(PetDefinition def) {
        definitions.put(def.id(), def);
    }

    public PetDefinition definition(String id) {
        return definitions.get(id);
    }

    public List<PetDefinition> allDefinitions() {
        return List.copyOf(definitions.values());
    }

    public List<PetDefinition> byArea(String worldId, String areaId) {
        List<PetDefinition> out = new ArrayList<>();
        for (PetDefinition d : definitions.values()) {
            if (d.areaId().equals(areaId)) out.add(d);
        }
        return out;
    }

    public List<PetDefinition> byWorld(String worldId) {
        List<PetDefinition> out = new ArrayList<>();
        for (PetDefinition d : definitions.values()) {
            if (d.worldId().equals(worldId)) out.add(d);
        }
        return out;
    }

    // ---- instances ----

    public void addInstance(PetRecord record) {
        instances.put(record.uuid(), record);
    }

    public PetRecord instance(UUID petUuid) {
        return instances.get(petUuid);
    }

    public void replaceInstance(PetRecord record) {
        instances.put(record.uuid(), record);
    }

    public void removeInstance(UUID petUuid) {
        instances.remove(petUuid);
    }

    public List<PetRecord> instancesOf(UUID owner) {
        List<PetRecord> out = new ArrayList<>();
        for (PetRecord r : instances.values()) {
            if (r.owner().equals(owner)) out.add(r);
        }
        return out;
    }

    public long equippedCount(UUID owner) {
        return instancesOf(owner).stream().filter(PetRecord::equipped).count();
    }
}

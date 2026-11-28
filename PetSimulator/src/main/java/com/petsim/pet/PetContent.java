package com.petsim.pet;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;
import com.petsim.pet.PetDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 26: content registry. Registers all pet definitions at boot.
 * Data-driven: later phases load from pets.yml, this guarantees the
 * core pools exist even with no config file.
 */
public final class PetContent {

    private final LogService logs;

    public PetContent(LogService logs) {
        this.logs = logs;
    }

    /** Registers the starter pools for the first world zones. */
    public void registerDefaults(PetRegistry registry) {
        List<PetDefinition> defs = List.of(
            // Green Valley
            def("green_cat", "Green Cat", PetDefinition.Rarity.COMMON, 5, "green_valley"),
            def("forest_dog", "Forest Dog", PetDefinition.Rarity.COMMON, 6, "green_valley"),
            def("leaf_bunny", "Leaf Bunny", PetDefinition.Rarity.UNCOMMON, 12, "green_valley"),
            def("nature_fox", "Nature Fox", PetDefinition.Rarity.RARE, 25, "green_valley"),
            def("emerald_dragon", "Emerald Dragon", PetDefinition.Rarity.EPIC, 60, "green_valley"),
            // Crystal Fields
            def("crystal_cat", "Crystal Cat", PetDefinition.Rarity.COMMON, 10, "crystal_fields"),
            def("diamond_fox", "Diamond Fox", PetDefinition.Rarity.RARE, 30, "crystal_fields"),
            def("gem_golem", "Gem Golem", PetDefinition.Rarity.EPIC, 70, "crystal_fields"),
            def("crystal_dragon", "Crystal Dragon", PetDefinition.Rarity.LEGENDARY, 150, "crystal_fields"),
            def("prism_phoenix", "Prism Phoenix", PetDefinition.Rarity.MYTHICAL, 300, "crystal_fields"),
            // Desert Empire
            def("sand_cat", "Sand Cat", PetDefinition.Rarity.COMMON, 12, "desert_empire"),
            def("desert_scorpion", "Desert Scorpion", PetDefinition.Rarity.UNCOMMON, 20, "desert_empire"),
            def("golden_cobra", "Golden Cobra", PetDefinition.Rarity.RARE, 38, "desert_empire"),
            def("pharaoh_dog", "Pharaoh Dog", PetDefinition.Rarity.EPIC, 80, "desert_empire"),
            def("ancient_sand_dragon", "Ancient Sand Dragon", PetDefinition.Rarity.LEGENDARY, 170, "desert_empire")
        );
        for (PetDefinition d : defs) {
            registry.register(d);
        }
        logs.info("core", "Registered " + defs.size() + " pet definitions (defaults; more areas next phases)");
    }

    private PetDefinition def(String id, String name, PetDefinition.Rarity rarity,
                             long power, String areaId) {
        return new PetDefinition(id, name, rarity, power,
            "petsim_world", areaId,
            "petsim/" + id,   // resource pack model key
            "");              // head texture fallback, filled by pack phase
    }
}

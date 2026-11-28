package com.petsim.egg;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;
import com.petsim.economy.EconomyManager;
import com.petsim.pet.PetRecord;
import com.petsim.pet.PetRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Phase 19/21/22/25: egg manager — hatch flow, locks, auto-hatch limits.
 * All purchases server-side; results never come from the client.
 */
public final class EggManager {

    private final PetRegistry registry;
    private final EconomyManager economy;
    private final HatchEngine engine;
    private final LogService logs;

    // Phase 25: auto-hatch config (loaded from config in wiring phase)
    private long autoHatchCoinLimit = 100_000;
    private int autoHatchInventoryCap = 100;

    public EggManager(PetRegistry registry, EconomyManager economy, HatchEngine engine, LogService logs) {
        this.registry = registry;
        this.economy = economy;
        this.engine = engine;
        this.logs = logs;
    }

    /** Phase 21: can the player hatch this egg at all? */
    public String lockReason(UUID player, EggDefinition egg, com.petsim.area.AreaManager areas) {
        if (!areas.isUnlocked(player, egg.worldId(), egg.areaId())) {
            return "area-locked";
        }
        return null;
    }

    /**
     * Phase 22: single hatch. Server checks price, rolls result, credits pet.
     * Returns the hatched pet definition id, or null with a reason.
     */
    public HatchResult hatch(UUID player, EggDefinition egg, com.petsim.area.AreaManager areas) {
        String lock = lockReason(player, egg, areas);
        if (lock != null) return HatchResult.failed(lock);

        if (!economy.withdraw(player, EconomyManager.Currency.COINS, egg.priceCoins())) {
            return HatchResult.failed("insufficient-coins");
        }
        String petId = engine.roll(egg);
        if (petId == null) {
            economy.add(player, EconomyManager.Currency.COINS, egg.priceCoins()); // refund
            return HatchResult.failed("engine-error");
        }
        PetRecord record = new PetRecord(UUID.randomUUID(), player, petId, 1, 0, false);
        registry.addInstance(record);
        logs.info("hatching", "Hatched " + petId + " from " + egg.id());
        return HatchResult.success(petId);
    }

    /** Phase 22: hatch three at once (cheaper per egg is NOT given — no silent discounts). */
    public List<HatchResult> hatchThree(UUID player, EggDefinition egg, com.petsim.area.AreaManager areas) {
        List<HatchResult> out = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            out.add(hatch(player, egg, areas));
            if (!out.get(i).success()) break; // stop on first failure (no funds = stop)
        }
        return out;
    }

    public record HatchResult(boolean success, String petId, String failReason) {
        static HatchResult success(String petId) { return new HatchResult(true, petId, null); }
        static HatchResult failed(String reason) { return new HatchResult(false, null, reason); }
    }
}

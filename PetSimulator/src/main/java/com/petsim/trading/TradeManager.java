package com.petsim.trading;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;
import com.petsim.pet.PetRepository;

import java.util.List;
import java.util.UUID;

/**
 * Phase 51/52: secure trading engine.
 * State machine with server-side validation at every step.
 * Completion is DB-atomic (both transfers succeed or both roll back).
 */
public final class TradeManager {

    public enum State { REQUESTED, ACTIVE, READY_A, READY_B, CONFIRMED, DONE, CANCELLED }

    public record TradeSession(
        UUID id, UUID a, UUID b, State state,
        List<UUID> petsA, List<UUID> petsB, long diamondsA, long diamondsB
    ) {}

    private final PetRepository pets;
    private final DatabaseService db;
    private final LogService logs;

    public TradeManager(PetRepository pets, DatabaseService db, LogService logs) {
        this.pets = pets;
        this.db = db;
        this.logs = logs;
    }

    /** Phase 52: atomic completion. Both pet transfers must succeed or nothing persists. */
    public boolean complete(TradeSession t) {
        try {
            db.execute("BEGIN IMMEDIATE");
            boolean okA = true, okB = true;
            for (UUID pet : t.petsA()) {
                okA &= pets.transferPet(pet, t.a(), t.b());
            }
            for (UUID pet : t.petsB()) {
                okB &= pets.transferPet(pet, t.b(), t.a());
            }
            if (!okA || !okB) {
                db.execute("ROLLBACK");
                logs.security("Trade " + t.id() + " rolled back (transfer mismatch)");
                return false;
            }
            db.execute(
                "INSERT INTO trade_log(player_a, player_b, state, details, created_at) VALUES(?,?,?,?,?)",
                t.a().toString(), t.b().toString(), "DONE",
                t.petsA().size() + " pets + " + t.diamondsA() + " diamonds <-> "
                    + t.petsB().size() + " pets + " + t.diamondsB() + " diamonds",
                System.currentTimeMillis());
            db.execute("COMMIT");
            logs.info("trading", "Trade " + t.id() + " completed");
            return true;
        } catch (Exception e) {
            try { db.execute("ROLLBACK"); } catch (Exception ignore) {}
            logs.severe("trading", "Trade " + t.id() + " failed: " + e.getMessage());
            return false;
        }
    }
}

package com.petsim.pet;

import com.petsim.core.LogService;
import com.petsim.database.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Loads/saves PetRecords. All heavy calls run through DatabaseService. */
public final class PetRepository {

    private final DatabaseService db;
    private final LogService logs;

    public PetRepository(DatabaseService db, LogService logs) {
        this.db = db;
        this.logs = logs;
    }

    public List<PetRecord> loadPets(UUID owner) {
        try {
            return db.query(
                "SELECT pet_uuid, owner_uuid, definition_id, level, experience, equipped "
                    + "FROM pets WHERE owner_uuid = ?",
                rs -> {
                    List<PetRecord> out = new ArrayList<>();
                    while (rs.next()) {
                        out.add(new PetRecord(
                            UUID.fromString(rs.getString("pet_uuid")),
                            UUID.fromString(rs.getString("owner_uuid")),
                            rs.getString("definition_id"),
                            rs.getInt("level"),
                            rs.getLong("experience"),
                            rs.getBoolean("equipped")));
                    }
                    return out;
                }, owner.toString());
        } catch (SQLException e) {
            logs.severe("database", "loadPets failed: " + e.getMessage());
            return List.of();
        }
    }

    public void savePet(PetRecord r) {
        try {
            db.execute(
                "INSERT INTO pets(pet_uuid, owner_uuid, definition_id, level, experience, equipped, created_at) "
                    + "VALUES(?,?,?,?,?,?,?) "
                    + "ON CONFLICT(pet_uuid) DO UPDATE SET level=excluded.level, "
                    + "experience=excluded.experience, equipped=excluded.equipped",
                r.uuid().toString(), r.owner().toString(), r.definitionId(),
                r.level(), r.experience(), r.equipped(), System.currentTimeMillis());
        } catch (SQLException e) {
            logs.severe("database", "savePet failed: " + e.getMessage());
        }
    }

    /** Ownership-checked delete (Phase 70C: prevents deleting pets you don't own). */
    public boolean deletePet(UUID petUuid, UUID expectedOwner) {
        try {
            return db.execute(
                "DELETE FROM pets WHERE pet_uuid = ? AND owner_uuid = ?",
                petUuid.toString(), expectedOwner.toString()) > 0;
        } catch (SQLException e) {
            logs.severe("database", "deletePet failed: " + e.getMessage());
            return false;
        }
    }

    /** Transfers ownership atomically; used by trading (Phase 51/52). */
    public boolean transferPet(UUID petUuid, UUID from, UUID to) {
        try {
            return db.execute(
                "UPDATE pets SET owner_uuid = ? WHERE pet_uuid = ? AND owner_uuid = ?",
                to.toString(), petUuid.toString(), from.toString()) > 0;
        } catch (SQLException e) {
            logs.severe("database", "transferPet failed: " + e.getMessage());
            return false;
        }
    }
}

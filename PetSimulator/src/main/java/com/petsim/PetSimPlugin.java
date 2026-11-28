package com.petsim;

import com.petsim.core.ConfigManager;
import com.petsim.core.LogService;
import com.petsim.core.ModuleManager;
import com.petsim.database.DatabaseService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * PetSimulator bootstrap. Phase 4: layered wiring.
 * Only core layers are wired here; gameplay systems register
 * themselves through ModuleManager (Phase 5).
 */
public final class PetSimPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ModuleManager moduleManager;
    private LogService logService;
    private DatabaseService database;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Phase 6: central config (loads defaults, validates, migrates schema).
        this.configManager = new ConfigManager(this);
        configManager.load();

        // Phase 7: separate logging channels.
        this.logService = new LogService(this, configManager);
        logService.info("core", "Config loaded, schema v" + configManager.schemaVersion());

        // Phase 5: module registry. Systems that are disabled in config
        // never register; other systems must not depend on disabled ones.
        this.moduleManager = new ModuleManager(this, configManager, logService);

        // Phase 70A: SQLite storage, schema versioned, async-ready.
        this.database = new DatabaseService(this, configManager, logService);
        try {
            database.init();
        } catch (Exception e) {
            logService.severe("database", "Failed to init storage: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Gameplay systems register below via moduleManager in later phases.

        logService.info("core", "Enabled in " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void onDisable() {
        // Phase 70B: save-then-close order, cleanup entities.
        if (database != null) {
            try {
                database.close();
            } catch (Exception e) {
                getLogger().severe("Storage close failed: " + e.getMessage());
            }
        }
        if (logService != null) {
            logService.flush();
        }
    }

    public ConfigManager configs() { return configManager; }
    public ModuleManager modules() { return moduleManager; }
    public LogService logs() { return logService; }
    public DatabaseService db() { return database; }

    /** Safe reload (Phase 6): reloads config + modules without touching player data. */
    public void safeReload() {
        configManager.reload();
        moduleManager.reload();
        logService.reload(configManager);
        logService.info("core", "Safe reload complete (player data untouched)");
    }
}

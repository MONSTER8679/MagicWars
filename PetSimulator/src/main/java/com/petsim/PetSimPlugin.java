package com.petsim;

import com.petsim.area.AreaManager;
import com.petsim.commands.PetSimCommand;
import com.petsim.core.ConfigManager;
import com.petsim.core.LogService;
import com.petsim.core.ModuleManager;
import com.petsim.database.DatabaseService;
import com.petsim.economy.EconomyManager;
import com.petsim.economy.UpgradeManager;
import com.petsim.egg.EggManager;
import com.petsim.egg.HatchEngine;
import com.petsim.listeners.PlayerLifecycleListener;
import com.petsim.pet.PetContent;
import com.petsim.pet.PetFollowService;
import com.petsim.pet.PetRegistry;
import com.petsim.pet.PetRepository;
import com.petsim.security.AreaProtectionListener;
import com.petsim.world.AreaDefinition;
import com.petsim.world.WorldManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * PetSimulator bootstrap. Phase 4: layered wiring.
 * Core wiring done; gameplay systems registered through ModuleManager (Phase 5).
 */
public final class PetSimPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ModuleManager moduleManager;
    private LogService logService;
    private DatabaseService database;
    private WorldManager worldManager;
    private AreaManager areaManager;
    private EconomyManager economy;
    private UpgradeManager upgrades;
    private PetRegistry petRegistry;
    private PetRepository petRepository;
    private PetFollowService petFollow;
    private HatchEngine hatchEngine;
    private EggManager eggManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Phase 6: central config.
        this.configManager = new ConfigManager(this);
        configManager.load();

        // Phase 7: logging channels.
        this.logService = new LogService(this, configManager);
        logService.info("core", "Config loaded, schema v" + configManager.schemaVersion());

        // Phase 5: module registry.
        this.moduleManager = new ModuleManager(this, configManager, logService);

        // Phase 70A: storage.
        this.database = new DatabaseService(this, configManager, logService);
        try {
            database.init();
        } catch (Exception e) {
            logService.severe("database", "Failed to init storage: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Phase 8: world.
        this.worldManager = new WorldManager(logService);
        try {
            String worldName = configManager.world("first_world", "petsim_world");
            int border = parseBorder(configManager.world("border_size", "2048"));
            worldManager.loadOrCreate(worldName, border);

            // Phase 10/13: register the ten first-world zones.
            this.areaManager = new AreaManager(database, logService);
            registerFirstWorldAreas(worldName);
        } catch (Exception e) {
            logService.severe("world_ops", "World init failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Phase 34/39: economy.
        this.economy = new EconomyManager(database, logService);
        this.upgrades = new UpgradeManager(database, logService);

        // Phase 26: pets.
        this.petRegistry = new PetRegistry();
        new PetContent(logService).registerDefaults(petRegistry);
        this.petRepository = new PetRepository(database, logService);

        // Phase 30/31: follow displays on shared tick.
        this.petFollow = new PetFollowService(this, petRegistry, logService);
        petFollow.start(configManager.performance("pet_update_interval_ticks", 2));

        // Phase 23/25: eggs.
        this.hatchEngine = new HatchEngine(petRegistry, logService);
        this.eggManager = new EggManager(petRegistry, economy, hatchEngine, logService);

        // Listeners.
        getServer().getPluginManager().registerEvents(
            new PlayerLifecycleListener(this, logService), this);
        getServer().getPluginManager().registerEvents(
            new AreaProtectionListener(worldManager, areaManager, logService), this);

        // Commands.
        PluginCommand cmd = getCommand("petsim");
        if (cmd != null) {
            PetSimCommand executor = new PetSimCommand(this, logService);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        logService.info("core", "Enabled in " + (System.currentTimeMillis() - start) + "ms");
    }

    private void registerFirstWorldAreas(String worldName) {
        // Phase 10: ten zones chained by requirement. 256x256 blocks each.
        List<String> zones = AreaDefinition.firstWorldZones();
        long[] prices = {0, 500, 2_500, 10_000, 40_000, 150_000, 500_000, 1_500_000, 5_000_000, 15_000_000};
        String[] palettes = {
            "spawn", "green", "forest", "crystal", "desert",
            "ice", "lava", "ruins", "golden", "paradise"
        };
        for (int i = 0; i < zones.size(); i++) {
            int x = ((i % 5) - 2) * 300;
            int z = ((i / 5) - 1) * 300;
            areaManager.register(new AreaDefinition(
                zones.get(i), zoneDisplay(zones.get(i)), worldName,
                prices[i],
                i == 0 ? null : zones.get(i - 1),
                x - 128, z - 128, x + 128, z + 128,
                x, 64, z + 128,
                palettes[i],
                i == 0 ? null : zones.get(i) + "_egg",
                null));
        }
        logService.info("world_ops", "Registered " + zones.size() + " areas");
    }

    private String zoneDisplay(String id) {
        return switch (id) {
            case "spawn" -> "Spawn Zone";
            case "green_valley" -> "Green Valley";
            case "forest_kingdom" -> "Forest Kingdom";
            case "crystal_fields" -> "Crystal Fields";
            case "desert_empire" -> "Desert Empire";
            case "frozen_kingdom" -> "Frozen Kingdom";
            case "lava_world" -> "Lava World";
            case "ancient_ruins" -> "Ancient Ruins";
            case "golden_kingdom" -> "Golden Kingdom";
            case "final_paradise" -> "Final Paradise";
            default -> id;
        };
    }

    private int parseBorder(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 2048;
        }
    }

    @Override
    public void onDisable() {
        if (petFollow != null) petFollow.stop();
        if (database != null) {
            try {
                if (configManager != null && configManager.raw().getBoolean("storage.backup_on_stop", true)) {
                    database.backup();
                }
                database.close();
            } catch (Exception e) {
                getLogger().severe("Storage close failed: " + e.getMessage());
            }
        }
        if (logService != null) logService.flush();
    }

    public ConfigManager configs() { return configManager; }
    public ModuleManager modules() { return moduleManager; }
    public LogService logs() { return logService; }
    public DatabaseService db() { return database; }
    public WorldManager worlds() { return worldManager; }
    public AreaManager areas() { return areaManager; }
    public EconomyManager economy() { return economy; }
    public UpgradeManager upgrades() { return upgrades; }
    public PetRegistry pets() { return petRegistry; }
    public PetRepository petRepo() { return petRepository; }
    public PetFollowService petFollow() { return petFollow; }
    public EggManager eggs() { return eggManager; }

    /** Safe reload (Phase 6): config + modules only; player data untouched. */
    public void safeReload() {
        configManager.reload();
        logService.reload(configManager);
        logService.info("core", "Safe reload complete (player data untouched)");
    }
}

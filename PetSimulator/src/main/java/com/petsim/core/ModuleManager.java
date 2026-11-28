package com.petsim.core;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 5: module registry.
 * Each gameplay system registers under an id. Systems must look up
 * optional dependencies through {@link #optional(String)} and handle
 * a null return gracefully — disabling one module must never break another.
 */
public final class ModuleManager {

    public interface Module {
        String id();
        void enable();
        void disable();
    }

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final LogService logs;
    private final Map<String, Module> modules = new ConcurrentHashMap<>();

    public ModuleManager(JavaPlugin plugin, ConfigManager config, LogService logs) {
        this.plugin = plugin;
        this.config = config;
        this.logs = logs;
    }

    /** Registers a module only if enabled in config. */
    public void register(Module module) {
        if (!config.moduleEnabled(module.id())) {
            logs.info("core", "Module '" + module.id() + "' disabled by config, skipped");
            return;
        }
        module.enable();
        modules.put(module.id(), module);
        logs.info("core", "Module enabled: " + module.id());
    }

    /** Modules must use this for cross-module lookups. */
    public Module optional(String id) {
        return modules.get(id);
    }

    /** Hard dependency: throws with a precise message instead of NPE later. */
    public Module required(String id) {
        Module m = modules.get(id);
        if (m == null) {
            throw new IllegalStateException("Required module '" + id + "' is not enabled");
        }
        return m;
    }

    public Collection<Module> all() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public void reload() {
        // Safe reload: disable all, re-read config, re-register. Player data
        // lives in services outside modules, so it survives.
        for (Module m : modules.values()) {
            try {
                m.disable();
            } catch (Exception e) {
                logs.warn("core", "Disable failed for module " + m.id() + ": " + e.getMessage());
            }
        }
        modules.clear();
        // Re-registration is done by the plugin's system wiring on safeReload().
    }

    public void shutdown() {
        for (Module m : modules.values()) {
            try {
                m.disable();
            } catch (Exception e) {
                logs.warn("core", "Shutdown of " + m.id() + " failed: " + e.getMessage());
            }
        }
    }
}

package com.example.magicwars;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class MagicWarsPlugin extends JavaPlugin {

    private static MagicWarsPlugin instance;

    private ManaManager manaManager;
    private WandManager wandManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private LobbyManager lobbyManager;
    private NpcManager npcManager;
    private StatsManager statsManager;
    private ShopManager shopManager;
    private SpellBookGui spellBook;
    private PowerupManager powerupManager;
    private ScoreboardManager scoreboard;
    private UltimateManager ultimateManager;
    private KillEffectsManager killEffects;
    private TrailManager trailManager;
    private DailyRewardManager dailyRewards;
    private BossManager bossManager;
    private SetupManager setupManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        statsManager = new StatsManager(this);
        manaManager = new ManaManager(this);
        wandManager = new WandManager(this);
        arenaManager = new ArenaManager(this);
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);
        npcManager = new NpcManager(this);
        shopManager = new ShopManager(this);
        spellBook = new SpellBookGui(this);
        powerupManager = new PowerupManager(this);
        scoreboard = new ScoreboardManager();
        ultimateManager = new UltimateManager(this);
        killEffects = new KillEffectsManager(this);
        trailManager = new TrailManager(this);
        dailyRewards = new DailyRewardManager(this);
        bossManager = new BossManager(this);
        setupManager = new SetupManager(this);

        var pm = getServer().getPluginManager();
        pm.registerEvents(new CastListener(this), this);
        pm.registerEvents(new DeathListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(npcManager, this);
        pm.registerEvents(shopManager, this);
        pm.registerEvents(spellBook, this);
        pm.registerEvents(powerupManager, this);
        pm.registerEvents(ultimateManager, this);
        pm.registerEvents(trailManager, this);
        pm.registerEvents(bossManager, this);

        powerupManager.startTask();

        Objects.requireNonNull(getCommand("mw")).setExecutor(new MagicCommand(this));
        PluginCommand admin = Objects.requireNonNull(getCommand("mwadmin"));
        AdminCommand adminCmd = new AdminCommand(this);
        admin.setExecutor(adminCmd);
        admin.setTabCompleter(adminCmd);

        PlayerCommand playerCmd = new PlayerCommand(this);
        PluginCommand play = Objects.requireNonNull(getCommand("play"));
        play.setExecutor(playerCmd);
        play.setTabCompleter(playerCmd);
        for (String name : List.of("lobby", "shop", "book", "stats"))
            Objects.requireNonNull(getCommand(name)).setExecutor(playerCmd);

        CosmeticCommand cosmeticCmd = new CosmeticCommand(this);
        for (String name : List.of("killeffect", "trail")) {
            PluginCommand c = Objects.requireNonNull(getCommand(name));
            c.setExecutor(cosmeticCmd);
            c.setTabCompleter(cosmeticCmd);
        }
        Objects.requireNonNull(getCommand("daily")).setExecutor(dailyRewards);

        getLogger().info("MagicWars v6.1 جاهز! ⚔👹");
    }

    public static MagicWarsPlugin getInstance() { return instance; }

    public ManaManager manaManager()      { return manaManager; }
    public WandManager wandManager()      { return wandManager; }
    public ArenaManager arenaManager()    { return arenaManager; }
    public GameManager gameManager()      { return gameManager; }
    public LobbyManager lobby()          { return lobbyManager; }
    public NpcManager npcManager()       { return npcManager; }
    public StatsManager stats()           { return statsManager; }
    public ShopManager shop()             { return shopManager; }
    public SpellBookGui book()            { return spellBook; }
    public PowerupManager powerups()      { return powerupManager; }
    public ScoreboardManager scoreboard() { return scoreboard; }
    public UltimateManager ultimate()     { return ultimateManager; }
    public KillEffectsManager killEffects() { return killEffects; }
    public TrailManager trails()          { return trailManager; }
    public BossManager boss()             { return bossManager; }
    public SetupManager setup()            { return setupManager; }

    @Override
    public void onDisable() {
        if (statsManager != null) statsManager.save();
    }
}

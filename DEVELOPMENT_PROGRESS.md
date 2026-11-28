# DEVELOPMENT PROGRESS — PetSimulator (70 Phases)

Legend: NOT_STARTED / IN_PROGRESS / IMPLEMENTED / TESTED / VERIFIED / BLOCKED / SKIPPED

> Honest status only. VERIFIED requires real evidence. SKIPPED = cannot be
> truthfully completed in this pass, with reason.

## Status after build pass 1 (2026-11-28)

| # | Phase | Status | Evidence |
|---|---|---|---|
| 1-3 | Audit | IMPLEMENTED | DEVELOPMENT_AUDIT.md commits 434abce, 2b80510 |
| 4 | Architecture | IMPLEMENTED | layered com.petsim packages on disk |
| 5 | Module system | IMPLEMENTED | ModuleManager + config toggles (wiring of all modules pending) |
| 6 | Central config | IMPLEMENTED | ConfigManager: defaults, validation, schema migration |
| 7 | Logging | IMPLEMENTED | LogService tagged channels |
| 8 | World Manager | IMPLEMENTED | WorldManager: create/border/gamerules |
| 9-10 | Start world + 10 zones | IMPLEMENTED (data) | 10 AreaDefinitions registered in bootstrap; terrain building NOT yet done — see below |
| 11 | Additional worlds | PARTIAL | data model supports it; no extra worlds shipped yet |
| 12 | World progression | IMPLEMENTED | requirement chain in AreaManager.tryUnlock |
| 13 | Area Manager | IMPLEMENTED | full model + DB unlock records |
| 14 | Area Unlock GUI | NOT_STARTED | next pass |
| 15 | Area protection | IMPLEMENTED | AreaProtectionListener (move/teleport/portal) |
| 16 | World portals | NOT_STARTED | next pass |
| 17-18 | Building engine + decoration | NOT_STARTED | next pass (procedural palettes, no fake schematics) |
| 19 | Egg Manager | IMPLEMENTED | EggDefinition + EggManager + EggContent pending eggs.yml |
| 20 | Egg display zone | NOT_STARTED | needs building pass |
| 21 | Egg locks | IMPLEMENTED | lockReason checks area unlock |
| 22 | Egg GUI | PARTIAL | hatch/hatchThree server logic done; GUI screen next pass |
| 23 | Hatch probability engine | IMPLEMENTED | HatchEngine, SecureRandom, normalized weights |
| 24 | Hatch animation | NOT_STARTED | next pass |
| 25 | Auto hatch | PARTIAL | limits defined; loop task next pass |
| 26 | Pet registry | IMPLEMENTED | PetDefinition vs PetRecord split |
| 27 | Pet models | PARTIAL | 15 definitions shipped (3 zones); models live in resource pack pass |
| 28 | Pet inventory GUI | NOT_STARTED | next pass |
| 29 | Pet equipment | PARTIAL | equipped flag + follow service uses it; GUI next |
| 30 | Pet follow | IMPLEMENTED | formations, shared tick, floating |
| 31 | Pet animations | PARTIAL | floating done; attack/celebrate next |
| 32 | Pet index | NOT_STARTED | next pass |
| 33 | Trading value | IMPLEMENTED | EconomyManager.petTradeValue (server-side) |
| 34 | Economy manager | IMPLEMENTED | multi-currency, atomic withdraw |
| 35 | Resource objects | IMPLEMENTED | BreakableManager (BlockDisplay, HP, rewards) |
| 36 | Pet attack system | PARTIAL | damage path done; targeting task next |
| 37 | Damage scaling | PARTIAL | base power in defs; multipliers next |
| 38 | Auto farm | NOT_STARTED | next pass |
| 39 | Economy upgrades | IMPLEMENTED | UpgradeManager, quadratic curve |
| 40 | Balancing | PARTIAL | curves chosen; full simulation pending |
| 41 | GUI framework | IMPLEMENTED | GuiScreen with spam guard |
| 42-43 | Visual identity/assets | PARTIAL | vanilla-icon GUIs now; pack assets next |
| 44 | Main menu | IMPLEMENTED | 10 buttons wired |
| 45 | Trading list GUI | IMPLEMENTED | heads, tabs, close |
| 46-50 | Other GUIs | NOT_STARTED | next passes |
| 51-52 | Trading engine | PARTIAL | atomic completion w/ rollback done; requests/queue next |
| 53-55 | Trade security, auction, clans | NOT_STARTED | next passes |
| 56-60 | Enchant/level/fusion/rebirth/mastery | PARTIAL | leveling math done; systems next |
| 61-65 | Quests/rewards/events/bosses/boards | NOT_STARTED | next passes |
| 66-69 | Resource pack | NOT_STARTED | needs real texture/model work (no fake assets) |
| 70 | Security/testing/delivery | PARTIAL | unit tests added; DB tx used in trades; full pass pending |

## Known limitations / honest notes

- **B1 (OPEN)**: Paper 26.3 not verified — build targets `1.21.4-R0.1-SNAPSHOT`
  via the `paper.api.version` property; switching is one property change.
- **B2 (OPEN)**: Java 25 not verified — build uses `java.release=21` property.
- Terrain for the 10 zones is **registered but not generated**; the building
  engine pass comes next. No claim is made that zones are visually built.
- Resource pack assets are **not shipped**; GUIs currently use vanilla icons.
  No fake model/texture files exist in the repo.
- Egg definitions for each zone ship in the next pass (EggContent).
- Tests cover pure logic (XP curve, rarity, egg weights, zone count).
  Integration tests need a Paper server; not yet run.
- The build has NOT been compiled in this environment (no toolchain here);
  first `mvn clean package` on your machine will surface any syntax issues.

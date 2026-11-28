# DEVELOPMENT AUDIT — PetSimulator

Date: 2026-11-28
Target: Paper 26.3 (unverified — see Blockers), Java 25 (unverified)
Repository: MONSTER8679/MagicWars, branch `feature/pet-simulator`

## 1. Current Structure (as found)

The repository contains **MagicWars**, a magic PvP mini-game plugin:

- Maven (`pom.xml`): `paper-api 1.21.4-R0.1-SNAPSHOT`, `maven.compiler.release 21`.
- 31 classes in a flat package `com.example.magicwars` (no sub-packages).
- Resources: `plugin.yml`, `config.yml`.
- No tests, no database, no resource pack, no Pet Simulator code anywhere.

Checked sources: GitHub repos (MagicWars, luckyblockpack, termux-bot, miniature-jjrj, BOT-ELGAZARV6, BOT-SEVER) and chat attachments (none present).

## 2. Finding: no existing Pet Simulator project

There is **no existing Pet Simulator plugin** in any reachable location. Per the
master prompt rule ("develop the current project, don't replace it silently"),
this audit documents the decision explicitly:

- PetSimulator is built **from scratch** on branch `feature/pet-simulator`.
- MagicWars code is **not touched or deleted** (it stays fully functional).
- The new plugin lives under `PetSimulator/` as an independent Maven module.

## 3. Problems in the existing codebase (MagicWars — informational)

| Problem | Impact |
|---|---|
| Flat package, no layering | hard to maintain |
| No persistence (stats in memory) | data loss on restart |
| No tests | regressions undetected |
| Hardcoded values in classes | config changes need recompile |

PetSimulator must avoid all of the above.

## 4. Blockers (explicit, not silently skipped)

| # | Blocker | Status | Effect |
|---|---|---|---|
| B1 | Paper 26.3 availability could not be verified (network sandbox failures on 2026-11-28) | OPEN | Build file uses a `paper.api.version` property; value must be confirmed before claiming 26.3 compatibility |
| B2 | Java 25 toolchain availability unverified | OPEN | `maven.compiler.release` is a property, switchable |
| B3 | No existing project files provided by owner | RESOLVED | build from scratch, documented here |

Per the master prompt: **no compatibility claim is made for Paper 26.3 or Java 25
until verification succeeds.** The build is version-parameterized so switching is
a one-property change, fully documented.

## 5. Architecture (approved plan)

Layered packages under `com.petsim`:

- `core` — bootstrap, module registry, config, logging
- `world` / `area` — world + area management, protection, portals
- `economy` — currencies, upgrades, balancing
- `egg` — egg manager, hatch engine, auto-hatch
- `pet` — registry, instances, follow, animations
- `inventory` — pet inventory + equip limits
- `combat` — breakables, pet targeting, damage
- `trading` / `friends` / `auction` / `clan` — social systems (module-toggled)
- `quest` / `event` / `rebirth` / `mastery` / `enchanting`
- `gui` — framework + every screen
- `npc` — server-side NPC (Interactables-style entities)
- `database` — SQLite (JDBC), schema versioning, migrations, async
- `commands` / `listeners` / `security` / `stats` / `util`

Game logic separated from GUI, persistence, and display.

## 6. Scope policy for this pass

The 70 phases are being implemented in priority order. Anything that cannot be
completed truthfully in this pass is marked **BLOCKED/SKIPPED** in
`DEVELOPMENT_PROGRESS.md` — never silently claimed as done.

## 7. Files planned to change

- New: everything under `PetSimulator/` (see progress file).
- Modified in MagicWars: **nothing**.

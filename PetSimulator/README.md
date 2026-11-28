# PetSimulator — installation & configuration guide

Build:
```
cd PetSimulator && mvn clean package
```

Install: copy `target/PetSimulator-0.1.0-SNAPSHOT.jar` into the server's
`plugins/` folder, plus a Paper 1.21.x server.

## Resource pack

The plugin reads `resource_pack.url` from config.yml. Leave it **empty**
until you host the pack zip at a real public URL — the plugin deliberately
never ships a fake URL. Fill in `sha1` after hosting so clients verify it.

## Storage

SQLite file lands in `plugins/PetSimulator/database/petsim.db`.
Backups via `/petsim admin backup` go to `plugins/PetSimulator/backups/`.

## Version policy (important)

`pom.xml` uses two properties:

- `paper.api.version` — currently `1.21.4-R0.1-SNAPSHOT`
- `java.release` — currently `21`

Reason: Paper "26.3" could not be verified during the audit (see
DEVELOPMENT_AUDIT.md blockers B1/B2). Per the project rules, no version
claim is made without verification. When you confirm the exact Paper
artifact you run, change these two properties in one place — no code
edits needed.

## What is implemented vs pending

See DEVELOPMENT_PROGRESS.md for the honest per-phase status. Anything not
truthfully completed is marked, never silently claimed.

## Commands

- `/petsim` or `/petsim menu` — main menu
- `/petsim help` — command list
- `/petsim admin reload` — safe config reload (does NOT touch player data)
- Admin subcommands require `petsim.admin`

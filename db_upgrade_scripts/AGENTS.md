# AGENTS.md — `db_upgrade_scripts/`

> Versioned upgrade/rollback SQL for existing `mosip_toolkit` deployments.
> Parent: [`../AGENTS.md`](../AGENTS.md). Related: [`db_scripts/AGENTS.md`](../db_scripts/AGENTS.md) (fresh install only).

## 1. Purpose

Move an **already-deployed** `mosip_toolkit` DB between released versions (upgrade or rollback) — not for creating a database from scratch (use [`db_scripts/`](../db_scripts/AGENTS.md)).

[`.github/workflows/db-test.yml`](../.github/workflows/db-test.yml) (`mosip/kattu` Postgres test workflow) triggers on `db_scripts/**` only and never executes anything here. Validate upgrade/rollback SQL manually against a copy of the target schema version, and keep both folders consistent for a given schema change.

## 2. Layout

```text
db_upgrade_scripts/
├── README.MD
└── mosip_toolkit/
    ├── upgrade.sh              # entry point: terminate connections -> run upgrade or rollback SQL
    ├── upgrade.properties      # DB connection details, ACTION, CURRENT_VERSION, UPGRADE_VERSION
    └── sql/
        ├── 1.1.0_to_1.2.0_upgrade.sql / _rollback.sql
        ├── 1.2.0_to_1.3.0_upgrade.sql / _rollback.sql
        ├── 1.3.0_to_1.4.0_upgrade.sql / _rollback.sql
        ├── 1.4.0_to_1.4.1_upgrade.sql / _rollback.sql
        ├── 1.4.1_to_1.4.2_upgrade.sql / _rollback.sql
        └── 1.4.2_to_1.4.3_upgrade.sql / _rollback.sql
```

Every version step has a matching upgrade/rollback pair — no gaps in the current chain (1.1.0 → 1.4.3).

## 3. How to run

```bash
cd db_upgrade_scripts/mosip_toolkit
# edit upgrade.properties: MOSIP_DB_NAME, DB_SERVERIP, DB_PORT, SU_USER_PWD, DBUSER_PWD,
# ACTION (upgrade|rollback), CURRENT_VERSION, UPGRADE_VERSION
./upgrade.sh upgrade.properties
```

`upgrade.sh` terminates active connections to `MOSIP_DB_NAME`, then runs `sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_upgrade.sql` (or `_rollback.sql` when `ACTION=rollback`) — exits non-zero if that file is missing.

## 4. Adding a new version step

1. New schema change: add `sql/<from>_to_<to>_upgrade.sql` (mirroring whatever DDL changed in [`db_scripts/mosip_toolkit/ddl/`](../db_scripts/AGENTS.md)) **and** a `_rollback.sql` that reverses it.
2. Filename versions must exactly match the `CURRENT_VERSION`/`UPGRADE_VERSION` a deployer sets in `upgrade.properties` — `upgrade.sh` builds the filename from those two properties.
3. Never renumber or delete prior pairs — older-release upgrades rely on the full chain.

## 5. Agent rules

**Do**: always ship both `_upgrade.sql` and `_rollback.sql` together; set `ACTION`/`CURRENT_VERSION`/`UPGRADE_VERSION` explicitly (no defaults); validate manually since CI doesn't cover this folder.

**Do not**: skip the rollback file (`upgrade.sh` fails fast if missing); point `upgrade.properties` at production without a backup (`psql -v ON_ERROR_STOP=1` runs directly); break the version chain by skipping a `<from>_to_<to>` pair.

---

*Last updated: 2026-08-10.*

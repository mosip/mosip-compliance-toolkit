# AGENTS.md — `db_scripts/`

> Greenfield PostgreSQL install for the `mosip_toolkit` database.
> Parent: [`../AGENTS.md`](../AGENTS.md). Related: [`db_upgrade_scripts/AGENTS.md`](../db_upgrade_scripts/AGENTS.md).

## 1. Purpose

**Fresh** environments only (sandbox init, empty Postgres) — `deploy.sh` **drops** the existing DB/role before recreating them. For an already-deployed environment use [`db_upgrade_scripts/`](../db_upgrade_scripts/AGENTS.md) instead.

`init_db.sh` runs this deploy inside an existing MOSIP K8s cluster (installs the `postgres-init` Helm chart with `init_values.yaml`).

## 2. Layout

```text
db_scripts/
├── README.MD
├── copy_cm_func.sh          # shared helper: copy a configmap/secret across namespaces
├── init_db.sh               # cluster entry point: helm-installs postgres-init with init_values.yaml
├── init_values.yaml         # values for the postgres-init chart (dbUserPasswords.dbuserPassword placeholder)
└── mosip_toolkit/
    ├── deploy.sh             # entry point: drop -> role -> DB -> DDL -> grants -> optional DML
    ├── deploy.properties     # DB_SERVERIP, DB_PORT, MOSIP_DB_NAME, DB_UNAME, DML_FLAG
    ├── db.sql                # CREATE DATABASE
    ├── ddl.sql                # \ir includes of ddl/*.sql
    ├── ddl/                   # per-table DDL (testcase.sql, collections.sql, biometric_scores.sql, ...)
    ├── role_dbuser.sql        # creates the app DB user
    ├── grants.sql             # grants for the app DB user
    ├── drop_db.sql            # destructive reset (used by deploy.sh)
    └── drop_role.sql          # destructive reset (used by deploy.sh)
```

Single schema, `mosip_toolkit`. No `dml.sql`/`dml/` checked in — `deploy.sh` only runs `dml.sql` when `DML_FLAG=1`; leave `DML_FLAG=0` unless you add one.

## 3. How to run

Local raw-SQL run:

```bash
cd db_scripts/mosip_toolkit
# edit deploy.properties: DB_SERVERIP, DB_PORT, MOSIP_DB_NAME (default mosip_toolkit), DB_UNAME (default toolkituser), DML_FLAG
./deploy.sh deploy.properties
```

`deploy.sh` prompts for `SU_USER_PWD`/`DBUSER_PWD` (or reads them from the env), terminates active connections to `MOSIP_DB_NAME`, then drops and recreates the DB and role.

Cluster install (existing MOSIP K8s cluster with Postgres running):

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add mosip https://mosip.github.io/mosip-helm
cd db_scripts
./init_db.sh [kubeconfig]
```

`init_db.sh` reads the app DB user's password from the `postgres` namespace's `db-common-secrets` secret and passes it via `--set dbUserPasswords.dbuserPassword="$DB_USER_PASSWORD"` — **not** from `init_values.yaml` (that field stays commented out/unused). It then (re)installs `postgres-init-toolkit` in the `compliance-toolkit` namespace, overwriting any existing `mosip_toolkit` DB — back it up first if it holds real data.

**Known risk**: `--set` can leak the password into Helm release metadata/process args. `mosip/postgres-init` is external to this repo and shows no Kubernetes Secret/external-secret input as an alternative — don't invent a `secretKeyRef`-style field it doesn't support; the real fix belongs in the chart.

## 4. Adding schema changes

1. Add/edit DDL under `mosip_toolkit/ddl/` (one file per table/FK set — see `ddl/fk.sql` for cross-table FKs).
2. Wire it into `mosip_toolkit/ddl.sql` via `\ir ddl/<file>.sql`.
3. Reaching an already-deployed environment: also add a matching upgrade/rollback pair in [`db_upgrade_scripts/`](../db_upgrade_scripts/AGENTS.md).

## 5. Agent rules

**Do**: wire every new DDL file into `ddl.sql` (unincluded files never apply); set `deploy.properties` + passwords before `deploy.sh`; pair deployed-env schema changes with `db_upgrade_scripts/`.

**Do not**: run `deploy.sh`/`init_db.sh` against data that must survive (unconditional drop); commit real credentials into `deploy.properties`/`init_values.yaml`; skip the `db_upgrade_scripts/` companion for a live-environment DDL change.

---

*Last updated: 2026-08-10.*

# AGENTS.md — `helm/`

> K8s Helm chart + cluster-side install scripts for the Compliance Toolkit backend.
> Parent: [`../AGENTS.md`](../AGENTS.md). DB setup before install: [`db_scripts/AGENTS.md`](../db_scripts/AGENTS.md). Java app: [`../mosip-compliance-toolkit/CLAUDE.md`](../mosip-compliance-toolkit/CLAUDE.md).

## 1. Chart

| Chart | Path | Deploys | Image |
|-------|------|---------|-------|
| `compliance-toolkit` | `helm/compliance-toolkit/` | backend Deployment + Service | `mosip-compliance-toolkit/Dockerfile` |

`Chart.yaml` (`apiVersion: v2`, `version: 0.0.1-develop`) depends on the Bitnami `common` chart. Templates: `deployment.yaml`, `service.yaml`, `serviceaccount.yaml`, `servicemonitor.yaml` (Prometheus), `virtualservice.yaml` (Istio). `install.sh` also installs a separate `compliance-toolkit-batch-job` chart published to the `mosip` Helm repo — **not** part of this `helm/` folder.

## 2. Layout

```text
helm/
└── compliance-toolkit/
    ├── Chart.yaml
    ├── values.yaml
    ├── ctk-set-cookie-header.yaml    # Istio EnvoyFilter applied by install.sh
    ├── keycloak-init-values.yaml     # values for the mosip/keycloak-init chart
    ├── copy_cm_func.sh               # shared helper: copy a configmap/secret across namespaces
    ├── copy_cm.sh                    # copies global/artifactory-share/config-server-share configmaps
    ├── install.sh                    # primary cluster install entry point
    ├── keycloak-init.sh              # creates/updates the toolkit Keycloak client
    ├── restart.sh                    # rolling restart of the compliance-toolkit namespace's deployments
    ├── delete.sh                     # uninstalls the compliance-toolkit helm release
    └── templates/                    # NOTES.txt, _helpers.tpl, deployment/service/serviceaccount/servicemonitor/virtualservice.yaml
```

CI: [`chart-lint-publish.yml`](../.github/workflows/chart-lint-publish.yml) (`mosip/kattu`) lints/publishes on `helm/**` changes; [`verify-keycloak-init.yml`](../.github/workflows/verify-keycloak-init.yml) checks the keycloak values file on `helm/compliance-toolkit/keycloak-init**` changes.

## 3. Install

```bash
cd helm/compliance-toolkit
./install.sh [kubeconfig]
```

`install.sh`: create `compliance-toolkit` namespace → disable Istio auto-injection → prompt for host, write into `global` ConfigMap (`mosip-compliance-host`) → `copy_cm.sh` (global/artifactory-share/config-server-share) → apply `ctk-set-cookie-header.yaml` EnvoyFilter → `keycloak-init.sh` → `helm install` both `compliance-toolkit` and `compliance-toolkit-batch-job` (pinned `CHART_VERSION=0.0.1-develop` in the script) → wait for rollout.

```bash
./keycloak-init.sh   # standalone: (re)create the Keycloak client only
./restart.sh          # rolling restart of all deployments in the namespace
./delete.sh            # interactive teardown of the helm release
```

`keycloak-init.sh` copies Keycloak ConfigMaps/secrets in, prompts for reCAPTCHA keys, runs `mosip/keycloak-init` (`keycloak-init-values.yaml`) to create the `mosip_toolkit_client`/`mosip_toolkit_android_client` clients, and syncs the resulting secret into `keycloak`/`config-server` namespaces.

## 4. Agent rules

**Do**: bump `Chart.yaml`'s `version` when publishing — keep it, `install.sh`'s `CHART_VERSION` (`compliance-toolkit`), and `keycloak-init.sh`'s `CHART_VERSION` (`toolkit-keycloak-init`) aligned with the published `mosip` Helm repo; add new values to `values.yaml` with sane defaults (`install.sh` only overrides `istio.corsPolicy.allowOrigins` via `--set`); `helm lint helm/compliance-toolkit` locally before relying on CI; run the DB deploy ([`db_scripts/`](../db_scripts/AGENTS.md)) before `install.sh` — it expects `mosip_toolkit` to already exist.

**Do not**: hardcode environment-specific hosts into templates (use `values.yaml`/`--set`/ConfigMap, as `install.sh` does); put reCAPTCHA keys or Keycloak client secrets in `values.yaml`/`--set`/ConfigMap — Kubernetes Secrets only (`keycloak-init.sh` handles this, never commit a real value); hand-edit a generated `Chart.lock`; assume `compliance-toolkit-batch-job` lives in this folder — it's published separately.

---

*Last updated: 2026-08-10.*

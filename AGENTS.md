# AGENTS.md

## Repository Overview

**MOSIP Compliance Toolkit** (CTK): a Spring Boot REST API that tests MOSIP-compatible biometric components before certifying them as compliant partners. Three project types:

- **SDK** — biometric algorithm SDKs (quality check, match, extract, segment)
- **SBI** — [Secure Biometric Interface](https://docs.mosip.io/develop/biometrics/secure-biometric-interface) capture-hardware devices
- **ABIS** — Automated Biometric Identification Systems

Workflow: create a project → create a test-case collection → upload biometric test data → run tests → generate a report → submit for admin review → approve/reject.

This repo is backend + DB scripts + Helm only. Reference UI: [mosip-compliance-toolkit-ui](https://github.com/mosip/mosip-compliance-toolkit-ui). Product docs: [MOSIP compliance toolkit docs](https://docs.mosip.io/compliance-tool-kit).

## Guide index

| Area | Path | Guide |
|------|------|-------|
| Java/Maven Spring Boot service | `mosip-compliance-toolkit/` | [`mosip-compliance-toolkit/CLAUDE.md`](mosip-compliance-toolkit/CLAUDE.md) — full API/service/entity reference, keep in sync with code |
| Fresh DB install (DDL) | `db_scripts/` | [`db_scripts/AGENTS.md`](db_scripts/AGENTS.md) |
| Version upgrade/rollback SQL | `db_upgrade_scripts/` | [`db_upgrade_scripts/AGENTS.md`](db_upgrade_scripts/AGENTS.md) |
| K8s Helm chart + cluster install | `helm/` | [`helm/AGENTS.md`](helm/AGENTS.md) |
| Test/schema assets (no build) | `resources/` | [`resources/ReadMe.md`](resources/ReadMe.md) |

CI workflows live in `.github/workflows/`: `push-trigger.yml`, `db-test.yml`, `chart-lint-publish.yml`, `verify-keycloak-init.yml`.

## Technology Stack

- Java 11, Maven, Spring Boot 2.0.2 (Web, Data JPA, Security, Cloud Config Client)
- PostgreSQL (`mosip_toolkit` schema) in prod; H2 in-memory for tests
- MinIO S3 for biometric test-data ZIPs/uploads; Keycloak (JWT/OAuth2) for IAM
- Reports: Apache Velocity → HTML → PDF via Flying Saucer
- Config externalized via Spring Cloud Config Server, not committed here
- CI/CD: GitHub Actions delegating to reusable `mosip/kattu` workflows; deploy via `helm/`, image from `mosip-compliance-toolkit/Dockerfile`

## Build & Test Commands

No root `pom.xml` — run Maven from `mosip-compliance-toolkit/`:

```shell
cd mosip-compliance-toolkit
mvn clean install                              # full build + tests
mvn clean package -Dgpg.skip=true -DskipTests  # matches CI publish behavior
mvn spring-boot:run                            # port 8099, context path /v1/toolkit
mvn test
mvn -Dtest=ClassName test
mvn -Dtest=ClassName#methodName test
mvn -Dsonar.host.url=https://sonarcloud.io -Psonar verify   # needs SONAR_TOKEN; -D before the goal
docker build -f Dockerfile .                   # build context is this module, not repo root
```

Tests: `mosip-compliance-toolkit/src/test/java/io/mosip/compliance/toolkit/`. Controller tests use `@WebMvcTest` with mocked services/`SecurityContext`; service tests use `@SpringBootTest` or Mockito+H2. JaCoCo excludes DTOs/entities/constants/config/repositories/filters (`sonar.coverage.exclusions` in `pom.xml`).

## Configuration

- `application.properties` loads from Spring Cloud Config Server at startup (`bootstrap.properties`: `spring.cloud.config.uri/label/name=compliance-toolkit`). Deployed environments don't necessarily match what's checked in.
- Centrally overridden via `compliance-toolkit-default.properties` in [mosip-config](https://github.com/mosip/mosip-config) — don't hardcode env-specific values here.
- `db_scripts/init_db.sh` reads the DB user password from the `postgres` namespace's `db-common-secrets` secret and injects it via Helm `--set` — a known plaintext-exposure risk (see [`db_scripts/AGENTS.md`](db_scripts/AGENTS.md)). Keep `init_values.yaml`'s `dbUserPasswords.dbuserPassword` commented out; never commit real credentials anywhere in `compliance-toolkit-default.properties`, `init_values.yaml`, or Helm `values.yaml` overrides.
- Helm deployment: `helm/compliance-toolkit/` (`keycloak-init.sh`, `install.sh`, `restart.sh`, `delete.sh`).

## Development Workflow

- Integration branch: `develop`; releases: `release-<version>` (e.g. `release-1.4.x`).
- `push-trigger.yml` triggers on `develop`/`master`/`release-1*`/`0.*`/`1.*`/`MOSIP*` pushes and PRs — Maven build, then branch-dependent Nexus publish / Docker build / SonarCloud, via `mosip/kattu`.
- Run `mvn clean install` from `mosip-compliance-toolkit/` before opening a PR — CI runs the same build.

## Pull Request Guidelines

- Target `develop` unless backporting to a release branch.
- Scope changes to the module touched (`mosip-compliance-toolkit/`, `db_scripts/`, `db_upgrade_scripts/`, `helm/`); explain cross-cutting changes in the PR description.
- Schema change reaching a deployed env: add DDL/DML under `db_scripts/mosip_toolkit/` **and** a matching pair under `db_upgrade_scripts/`. Fresh-install-only: `db_scripts/` alone.
- API contract changes: check whether [mosip-compliance-toolkit-ui](https://github.com/mosip/mosip-compliance-toolkit-ui) needs a matching update, note it in the PR.
- Sign off commits (`git commit -s`).

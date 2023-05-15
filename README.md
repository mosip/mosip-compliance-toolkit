[![Maven Package upon a push](https://github.com/mosip/mosip-compliance-toolkit/actions/workflows/push_trigger.yml/badge.svg?branch=1.1.0)](https://github.com/mosip/mosip-compliance-toolkit/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=develop&project=mosip_mosip-compliance-toolkit&metric=alert_status)](https://sonarcloud.io/dashboard?branch=1.1.0&id=mosip_mosip-compliance-toolkit)

# Mosip-compliance-toolkit 
This repository contains the source code for MOSIP Compliance-toolkit services.  For an overview refer [here](https://docs.mosip.io/compliance-tool-kit).  The modules exposes API endpoints. For a reference front-end UI implementation refer to [Compliance-toolkit UI github repo](https://github.com/mosip/mosip-compliance-toolkit-ui/)

Mosip-compliance-toolkit is used to test following biometric components:
1. [Biomtric devices](https://docs.mosip.io/1.2.0/biometrics/biometric-devices)
2. [Biometric SDK](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk)

## Database
See [DB guide](db_scripts/README.MD).

## Config-Server
To run Compliance-toolkit services, run [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server).
All properties mentioned is ```mosip-compliance-toolkit/src/main/resources/application.properties``` can be overwritten in config server file ```compliance-toolkit-default.properties```

## Build & run (for developers)
Prerequisites:

1. [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)
2. JDK 1.11  
3. Build and install:
    ```
    $ cd mosip-compliance-toolkit
    $ mvn clean install -Dgpg.skip=true
    ```
4. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```
## Installing in k8s cluster using helm
### Pre-requisites
1. Set the kube config file of the Mosip cluster having dependent services is set correctly in PC.
1. Make sure [DB setup](db_scripts#install-in-existing-mosip-k8-cluster) is done.
1. Add [compliance-toolkit-default.properties](https://github.com/mosip/mosip-config/blob/v0.0.9-CTK/compliance-toolkit-default.properties) in required branch of config repo.
1. Below are the dependent services required for compliance toolkit service:
    | Chart | Chart version |
    |---|---|
    |[Clamav](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/external/antivirus/clamav) | 2.4.1 |
    |[Keycloak](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/external/iam) | 7.1.18 |
    |[Keycloak-init](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/external/iam) | 12.0.1-beta |
    |[Postgres](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/external/postgres) | 10.16.2 |
    |[Postgres Init](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/external/postgres) | 12.0.1-beta |
    |[Minio](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/external/object-store) | 10.1.6 |
    |[Config-server](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/mosip/config-server) | 12.0.1-beta |
    |[Artifactory server](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B1/deployment/v3/mosip/artifactory) | 12.0.1-beta |
    |[Auditmanager service](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B1/deployment/v3/mosip/kernel/install.sh) | 12.0.1-beta |
    |[Authmanager service](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B1/deployment/v3/mosip/kernel/install.sh) | 12.0.1-beta |
    |[Keymanager service](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B1/deployment/v3/mosip/kernel/install.sh) | 12.0.1-beta |
    |[Notifier service](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B1/deployment/v3/mosip/kernel/install.sh) | 12.0.1-beta |
    |[Partner manager service](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B1/deployment/v3/mosip/pms/install.sh) | 12.0.1-beta |
### Install

* Install `kubectl` and `helm` utilities.
* Run the `keycloak-init.sh` script to create the `mosip-toolkit-client` keycloak client. You can skip this step if you have already done it.
  ```
  cd helm
  ./keycloak-init.sh
  ```
* Run the `install.sh` to deploy complaince-toolkit.
  ```
  cd helm
  ./install.sh [cluster-kubeconfig-file]
  ```
### Restart
```
cd helm
./restart.sh [cluster-kubeconfig-file]
```
### Delete
```
cd helm
./delete.sh [cluster-kubeconfig-file]
```

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).

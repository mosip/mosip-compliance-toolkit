[![Maven Package upon a push](https://github.com/mosip/mosip-compliance-toolkit/actions/workflows/push_trigger.yml/badge.svg?branch=master)](https://github.com/mosip/mosip-compliance-toolkit/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=master&project=mosip_mosip-compliance-toolkit&metric=alert_status)](https://sonarcloud.io/dashboard?branch=master&id=mosip_mosip-compliance-toolkit)

# Mosip-compliance-toolkit 
This repository contains the source code for MOSIP Compliance-toolkit services.  For an overview refer [here](https://docs.mosip.io/1.2.0/modules/compliance-tool-kit).  The modules exposes API endpoints. For a reference front-end UI implementation refer to [Compliance-toolkit UI github repo](https://github.com/mosip/mosip-compliance-toolkit-ui/)

Mosip-compliance-toolkit used to test following biometic components
1. [Biomtric devices](https://docs.mosip.io/1.2.0/biometrics/biometric-devices)
2. [Biometric SDK](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk)
3. [ABIS](https://docs.mosip.io/1.2.0/biometrics/abis)

## Database
See [DB guide](https://github.com/mosip/mosip-compliance-toolkit/blob/master/db_scripts/README.MD)

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
## Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).

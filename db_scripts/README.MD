# Compliance Toolkit Database

## Overview
This folder containers various SQL scripts to create database and tables in postgres. The tables are described under `<db name>/ddl/`. Default data that's populated in the tables is present under `<db name>/dml` folder 

## Install in existing MOSIP K8 Cluster
These scripts are automatically run with below mentioned script in existing k8 cluster with Postgres installed.
### Pre-requisites
- Command line utilities:
  - kubectl
  - helm
- Helm repos:
  ```sh
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm repo add mosip https://mosip.github.io/mosip-helm
  ```
### Install
- Set your kube_config file or kube_config variable in PC.
- Update `init_values.yaml` with db-common-password from postgres namespace in required field `dbUserPasswords.dbuserPassword`.
- `./init_db.sh`

## Install for developers
Developers may run the SQLs using `<db name>/deploy.sh` script.

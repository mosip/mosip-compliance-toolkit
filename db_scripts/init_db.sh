#!/bin/sh
# Script to initialize Compliance DB. 
## Usage: ./init_db.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=postgres
CHART_VERSION=12.0.1-B2
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update
while true; do
    read -p "CAUTION: Do we already have Postgres installed? Also make sure the toolkit DB is backed up as the same will be overiden. Do you still want to continue?" yn
    if [ $yn = "Y" ]
      then
        echo Removing existing mosip_toolkit DB installation
        helm -n $NS delete postgres-init-toolkit
        echo Initializing DB
        helm -n $NS install postgres-init-toolkit mosip/postgres-init -f init_values.yaml --version $CHART_VERSION --wait --wait-for-jobs
        break
      else
        break
    fi
done

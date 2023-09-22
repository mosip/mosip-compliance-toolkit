#!/bin/sh
# Script to initialize Compliance DB. 
## Usage: ./init_db.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=compliance-toolkit
CHART_VERSION=12.0.1-B3
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update
while true; do
    read -p "CAUTION: Do we already have Postgres installed? Also make sure the toolkit DB is backed up as the same will be overiden. Do you still want to continue?" yn
    if [ $yn = "Y" ]
      then
        echo Create $NS namespace
        kubectl create ns $NS
        echo Removing existing mosip_toolkit DB installation
        helm -n $NS delete postgres-init-toolkit
        echo Copy Postgres secrets
        ./copy_cm_func.sh secret postgres-postgresql postgres $NS
        DB_USER_PASSWORD=$( kubectl -n postgres get secrets db-common-secrets -o jsonpath={.data.db-dbuser-password} | base64 -d )
        echo Initializing DB
        helm -n $NS install postgres-init-toolkit mosip/postgres-init \
        -f init_values.yaml \
        --version $CHART_VERSION \
        --set image.tag=1.2.0-CTK \
        --set dbUserPasswords.dbuserPassword="$DB_USER_PASSWORD" \
        --wait --wait-for-jobs
        break
      else
        break
    fi
done

#!/bin/sh
# Installs all compliance-toolkit helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=compliance-toolkit

echo Updating Helm Dependencies
helm dependency update

echo Create $NS namespace
kubectl create ns $NS

echo Istio label
kubectl label ns $NS istio-injection=disabled --overwrite
helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

read -p "Please provide IDP host (ex: compliance.sandbox.xyz.net ): " COMPLIANCE_HOST

if [ -z $COMPLIANCE_HOST ]; then
  echo "COMPLIANCE host not provided; EXITING;"
  exit 1;
fi

kubectl -n default get cm global -o json | jq ".data[\"mosip-compliance-host\"]=\"$COMPLIANCE_HOST\"" | kubectl apply -f -

echo Copy configmaps
./copy_cm.sh

API_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-api-internal-host})
COMPLIANCE_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-compliance-host})

./keycloak-init.sh

echo Installing compliance-toolkit
helm -n $NS install compliance-toolkit . --set istio.corsPolicy.allowOrigins\[0\].prefix=https://$COMPLIANCE_HOST

kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

echo Installed compliance-toolkit services

#!/bin/bash
# Installs all compliance-toolkit helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=compliance-toolkit
CHART_VERSION=1.4.2

echo Create $NS namespace
kubectl create ns $NS

function installing_compliance-toolkit() {
  echo Istio label
  kubectl label ns $NS istio-injection=disabled --overwrite
  helm repo add mosip https://mosip.github.io/mosip-helm
  helm repo update

  read -p "Please provide compliance toolkit host (ex: compliance.sandbox.xyz.net ): " COMPLIANCE_HOST

  if [ -z $COMPLIANCE_HOST ]; then
    echo "COMPLIANCE host not provided; EXITING;"
    exit 1;
  fi

  kubectl -n default get cm global -o json | jq ".data[\"mosip-compliance-host\"]=\"$COMPLIANCE_HOST\"" | kubectl apply -f -

  echo Copy configmaps
  ./copy_cm.sh

  COMPLIANCE_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-compliance-host})

  echo "Applying EnvoyFilter to set cookie header for compliance toolkit service"
  kubectl -n istio-system apply -f ctk-set-cookie-header.yaml

  ./keycloak-init.sh

  echo Installing compliance-toolkit
  helm -n $NS install compliance-toolkit mosip/compliance-toolkit --set istio.corsPolicy.allowOrigins\[0\].prefix=https://$COMPLIANCE_HOST --set istio.corsPolicy.allowOrigins\[1\].prefix=http://localhost --version $CHART_VERSION

  echo Installing compliance-toolkit-batch-job
  helm -n $NS install compliance-toolkit-batch-job mosip/compliance-toolkit-batch-job --version $CHART_VERSION

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed compliance-toolkit services
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_compliance-toolkit   # calling function

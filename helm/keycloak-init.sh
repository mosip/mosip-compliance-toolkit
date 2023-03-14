#!/bin/bash
COPY_UTIL=./copy_cm_func.sh
NS=compliance-toolkit
CHART_VERSION=12.0.1-B2

helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

echo checking if toolkit client is created already
IAMHOST_URL=$(kubectl get cm global -o jsonpath={.data.mosip-iam-external-host})
TOOLKIT_CLIENT_SECRET_KEY="mosip_toolkit_client_secret"
TOOLKIT_CLIENT_SECRET_VALUE=$( kubectl -n keycloak get secret keycloak-client-secrets -o jsonpath={.data.mosip_toolkit_client_secret} | base64 -d )
TOOLKIT_ANDROID_CLIENT_SECRET_KEY="mosip_toolkit_android_client_secret"
TOOLKIT_ANDROID_CLIENT_SECRET_VALUE=$( kubectl -n keycloak get secret keycloak-client-secrets -o jsonpath={.data.mosip_toolkit_android_client_secret} | base64 -d )
echo Creating Toolkit keycloak client
echo "Copy keycloak configmap and secrets"
$COPY_UTIL configmap keycloak-host keycloak $NS
$COPY_UTIL configmap keycloak-env-vars keycloak $NS
$COPY_UTIL secret keycloak keycloak $NS

echo "Creating and copying keycloak toolkit client"
helm -n $NS delete toolkit-keycloak-init
kubectl -n $NS delete secret  --ignore-not-found=true keycloak-client-secrets
helm -n $NS install toolkit-keycloak-init mosip/keycloak-init \
-f keycloak-init-values.yaml \
--set frontend="https://$IAMHOST_URL/auth" \
--set clientSecrets[0].name="$TOOLKIT_CLIENT_SECRET_KEY" \
--set clientSecrets[0].secret="$TOOLKIT_CLIENT_SECRET_VALUE" \
--set clientSecrets[1].name="$TOOLKIT_ANDROID_CLIENT_SECRET_KEY" \
--set clientSecrets[1].secret="$TOOLKIT_ANDROID_CLIENT_SECRET_VALUE" \
--version $CHART_VERSION --wait

TOOLKIT_CLIENT_SECRET_VALUE=$( kubectl -n $NS get secret keycloak-client-secrets -o json |  jq ".data.$TOOLKIT_CLIENT_SECRET_KEY" )
TOOLKIT_ANDROID_CLIENT_SECRET_VALUE=$( kubectl -n $NS get secret keycloak-client-secrets -o json | jq ".data.$TOOLKIT_ANDROID_CLIENT_SECRET_KEY" )

kubectl -n keycloak get secret keycloak-client-secrets -o json | jq ".data[\"$TOOLKIT_CLIENT_SECRET_KEY\"]=$TOOLKIT_CLIENT_SECRET_VALUE" | kubectl apply -f -
kubectl -n config-server get secret keycloak-client-secrets -o json | jq ".data[\"$TOOLKIT_CLIENT_SECRET_KEY\"]=$TOOLKIT_CLIENT_SECRET_VALUE" | kubectl apply -f -
kubectl -n keycloak get secret keycloak-client-secrets -o json | jq ".data[\"$TOOLKIT_ANDROID_CLIENT_SECRET_KEY\"]=$TOOLKIT_ANDROID_CLIENT_SECRET_VALUE" | kubectl apply -f -
kubectl -n config-server get secret keycloak-client-secrets -o json | jq ".data[\"$TOOLKIT_ANDROID_CLIENT_SECRET_KEY\"]=$TOOLKIT_ANDROID_CLIENT_SECRET_VALUE" | kubectl apply -f -

echo "Check the existence of the toolkit secret & host placeholder & pass the toolkit secret & toolkit host to config-server deployment if the placeholder does not exist."
TOOLKIT_HOST=$( kubectl -n config-server get deployment -o json | jq -c '.items[].spec.template.spec.containers[].env[]| select(.name == "SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_MOSIP_COMPLIANCE_HOST")|.name' )
if [[ -z $TOOLKIT_HOST ]]; then
  kubectl -n config-server set env --keys=mosip-compliance-host --from configmap/global deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  echo "Waiting for config-server to be Up and running"
  kubectl -n config-server rollout status deploy/config-server
fi
TOOLKIT_SECRET=$( kubectl -n config-server get deployment -o json | jq -c '.items[].spec.template.spec.containers[].env[]| select(.name == "SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_MOSIP_TOOLKIT_CLIENT_SECRET")|.name' )
if [[ -z $TOOLKIT_SECRET ]]; then
  kubectl -n config-server set env --keys=mosip_toolkit_client_secret --from secret/keycloak-client-secrets deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  echo "Waiting for config-server to be Up and running"
  kubectl -n config-server rollout status deploy/config-server
fi

TOOLKIT_ANDROID_SECRET=$( kubectl -n config-server get deployment -o json | jq -c '.items[].spec.template.spec.containers[].env[]| select(.name == "SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_MOSIP_ANDROID_TOOLKIT_CLIENT_SECRET")|.name' )
if [[ -z $TOOLKIT_ANDROID_SECRET ]]; then
  kubectl -n config-server set env --keys=mosip_toolkit_android_client_secret --from secret/keycloak-client-secrets deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  echo "Waiting for config-server to be Up and running"
  kubectl -n config-server rollout status deploy/config-server
fi

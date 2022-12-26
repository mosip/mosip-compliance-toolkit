#!/bin/bash
COPY_UTIL=./copy_cm_func.sh

echo checking if toolkit client is created already
if [  -z "$(kubectl -n keycloak get secret keycloak-client-secrets -o jsonpath={.data.mosip_toolkit_client_secret})" ]
then
echo Toolkit client not present
echo Creating Toolkit client
$COPY_UTIL configmap keycloak-host keycloak compliance-toolkit
$COPY_UTIL configmap keycloak-env-vars keycloak compliance-toolkit
$COPY_UTIL secret keycloak keycloak compliance-toolkit
$COPY_UTIL secret keycloak-client-secrets keycloak compliance-toolkit
IAMHOST_URL=$(kubectl get cm global -o jsonpath={.data.mosip-iam-external-host})
echo creating and copying keycloak toolkit client
helm -n compliance-toolkit delete keycloak-init
helm -n compliance-toolkit install keycloak-init mosip/keycloak-init -f keycloak-init-values.yaml --set frontend=https://$IAMHOST_URL/auth --version 12.0.2 --wait
$COPY_UTIL secret keycloak-client-secrets compliance-toolkit keycloak
$COPY_UTIL secret keycloak-client-secrets compliance-toolkit config-server
echo check if tollkit secret is passed to config server deployment
	if [ -z "$(kubectl -n config-server set env deploy/config-server --list | grep SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_MOSIP_TOOLKIT_CLIENT_SECRET)" ]
	then
	kubectl -n config-server set env --keys=mosip-compliance-host --from configmap/global deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
	kubectl -n config-server set env --keys=mosip_toolkit_client_secret --from secret/keycloak-client-secrets deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
	fi
else
echo Toolkit client already present
fi

echo Waiting for config-server to be Up and running
kubectl -n config-server rollout status deploy/config-server

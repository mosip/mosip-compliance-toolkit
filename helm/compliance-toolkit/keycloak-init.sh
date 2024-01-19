#!/bin/bash
COPY_UTIL=./copy_cm_func.sh
NS=compliance-toolkit
CHART_VERSION=12.0.2

helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

echo checking if toolkit client is created already
kubectl -n $NS create ns $NS

IAMHOST_URL=$(kubectl get cm global -o jsonpath={.data.mosip-iam-external-host})
CTK_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-compliance-host})
TOOLKIT_CLIENT_SECRET_KEY="mosip_toolkit_client_secret"
TOOLKIT_CLIENT_SECRET_VALUE=$( kubectl -n keycloak get secret keycloak-client-secrets -o jsonpath={.data.mosip_toolkit_client_secret} | base64 -d )
TOOLKIT_ANDROID_CLIENT_SECRET_KEY="mosip_toolkit_android_client_secret"
TOOLKIT_ANDROID_CLIENT_SECRET_VALUE=$( kubectl -n keycloak get secret keycloak-client-secrets -o jsonpath={.data.mosip_toolkit_android_client_secret} | base64 -d )

echo Creating Toolkit keycloak client
echo "Copy keycloak configmap and secrets"
$COPY_UTIL configmap keycloak-host keycloak $NS
$COPY_UTIL configmap keycloak-env-vars keycloak $NS
$COPY_UTIL secret keycloak keycloak $NS

read -p "Please enter the recaptcha admin site key for domain $CTK_HOST & $IAMHOST_URL : "  CTK_SITE_KEY
read -p "Please enter the recaptcha admin secret key for domain $CTK_HOST & $IAMHOST_URL : " CTK_SECRET_KEY
read -p "Please enter the useRecaptchaNet for domain $CTK_HOST & $IAMHOST_URL (optional. default: '' ) : " USE_RECAPTCHA_NET

if [[ -z $CTK_SITE_KEY ]]; then
  echo "recaptcha site key is empty; EXITING;";
  exit 1;
fi
if [[ -z $CTK_SECRET_KEY ]]; then
  echo "recaptcha secret key is empty; EXITING;";
  exit 1;
fi
if [[ -z $USE_RECAPTCHA_NET ]]; then
  USE_RECAPTCHA_NET=''
fi

echo "Setting up captcha secrets"
kubectl -n $NS --ignore-not-found=true delete  secrets ctk-captcha
kubectl -n $NS create secret generic ctk-captcha --from-literal=ctk-captcha-site-key="$CTK_SITE_KEY" --from-literal=ctk-captcha-secret-key="$CTK_SECRET_KEY" --from-literal=ctk-captcha-use-recaptcha-net="$USE_RECAPTCHA_NET" --dry-run=client -o yaml | kubectl apply -f -

echo "Creating and copying keycloak toolkit client"
helm -n $NS delete toolkit-keycloak-init
kubectl -n $NS delete secret  --ignore-not-found=true keycloak-client-secrets
helm -n $NS install toolkit-keycloak-init /home/techno-384/Desktop/MOSIP/mosip-helm/charts/keycloak-init \
--set keycloak.realms.mosip.realm_config.attributes.frontendUrl="https://$IAMHOST_URL/auth" \
--set keycloak.realms.mosip.realm_config.browserSecurityHeaders.contentSecurityPolicy="\"frame-src 'self' https://www.google.com; frame-ancestors 'self'; object-src 'none';\"" \
--set clientSecrets[0].name="$TOOLKIT_CLIENT_SECRET_KEY" \
--set clientSecrets[0].secret="$TOOLKIT_CLIENT_SECRET_VALUE" \
--set clientSecrets[1].name="$TOOLKIT_ANDROID_CLIENT_SECRET_KEY" \
--set clientSecrets[1].secret="$TOOLKIT_ANDROID_CLIENT_SECRET_VALUE" \
--set extraEnvVarsSecret[0]="ctk-captcha" \
--version $CHART_VERSION --wait -f keycloak-init-values.yaml


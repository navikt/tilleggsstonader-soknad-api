kubectl config use-context dev-gcp
SOKNAD_API="tilleggsstonader-soknad-api"
PODNAVN=$(kubectl -n tilleggsstonader get pods --field-selector=status.phase==Running -o name | grep $SOKNAD_API |  sed "s/^.\{4\}//" | head -n 1);

# echo "Henter variabler fra $PODNAVN"

PODVARIABLER="$(kubectl -n tilleggsstonader exec -c $SOKNAD_API -it "$PODNAVN" -- env)"
# echo "PODVARIABLER=$PODVARIABLER"
TOKEN_X_CLIENT_ID="$(echo "$PODVARIABLER" | grep "TOKEN_X_CLIENT_ID" | tr -d '\r' )";
TOKEN_X_WELL_KNOWN_URL="$(echo "$PODVARIABLER" | grep "TOKEN_X_WELL_KNOWN_URL" | tr -d '\r' )";
TOKEN_X_PRIVATE_JWK="$(echo "$PODVARIABLER" | grep "TOKEN_X_PRIVATE_JWK" | tr -d '\r' )";

if [[ -z "$TOKEN_X_CLIENT_ID" || -z "$TOKEN_X_WELL_KNOWN_URL" || -z "$TOKEN_X_PRIVATE_JWK" ]]
then
      echo "Fant ikke alle variabler"
      exit 1
fi
echo "Envs:"
echo "$TOKEN_X_WELL_KNOWN_URL"
echo "$TOKEN_X_CLIENT_ID"
echo "$TOKEN_X_PRIVATE_JWK"

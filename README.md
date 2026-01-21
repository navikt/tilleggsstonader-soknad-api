# tilleggsstonader-soknad-api

Backend - søknad for tilleggsstønader

## Lokal kjøring

- Kjør opp Spring-appen `SøknadApiLocal`

## Lokal kjøring av soknad-api og ts-sak

1. Kjør opp Spring-appen `SøknadApiLocal`
    - For at applikasjonen skal fungere mot ts-sak må man sette opp miljøvariabler som beskrevet under "Secrets"
2. Kjør opp Spring-appen `SakAppLocalPostgres` i `tilleggsstonader-sak/src/test/kotlin/no/nav/tilleggsstonader/sak/SakAppLocalPostgres.kt`

## Secrets
For at man skal få svar fra ts-sak må man hente ut secrets:

1. Logg inn med `gcloud auth login`
2. Hent ut fra cluster: `kubectl --context dev-gcp -n tilleggsstonader get secret azuread-tilleggsstonader-soknad-api-lokal -o json | jq '.data | map_values(@base64d)' | grep CLIENT`
3. Hent ut fra cluster: `kubectl --context dev-gcp -n tilleggsstonader get secret azuread-tilleggsstonader-sak-lokal -o json | jq '.data | map_values(@base64d)' | grep TENANT`
4. Variablene legges inn under `SøknadApiLocal` -> Edit Configurations -> Modify Options -> huk av for Environment
   variables og legg til følgende variabler:
    - `AZURE_APP_CLIENT_ID={secret};AZURE_APP_CLIENT_SECRET={secret};AZURE_APP_TENANT_ID={secret}`

## Kode generert av GitHub Copilot
Dette repoet bruker GitHub Copilot til å generere kode.,
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
2. Hent AZURE_APP_CLIENT_ID og AZURE_APP_CLIENT_SECRET ut fra cluster: `nais secret get azuread-tilleggsstonader-soknad-api-lokal -e dev-gcp -t tilleggsstonader --with-values --reason "Lokal utvikling" --output json | jq '.[]'`
3. Hent AZURE_APP_TENANT_ID fra cluster: `nais secret get azuread-tilleggsstonader-sak-lokal -e dev-gcp -t tilleggsstonader --with-values --reason "Lokal utvikling" | grep TENANT`
4. Variablene legges inn under `SøknadApiLocal` -> Edit Configurations -> Modify Options -> huk av for Environment
   variables og legg til følgende variabler:
    - `AZURE_APP_CLIENT_ID={secret};AZURE_APP_CLIENT_SECRET={secret};AZURE_APP_TENANT_ID={secret}`

## Kode generert av GitHub Copilot
Dette repoet bruker GitHub Copilot til å generere kode.,
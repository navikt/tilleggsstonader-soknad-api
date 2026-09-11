# tilleggsstonader-soknad-api

Backend - søknad for tilleggsstønader

## Samtidig innsending av kjørelister

`POST /api/kjorelister` serialiseres per innlogget bruker med en transaksjonsbundet PostgreSQL advisory lock.
Låsen holdes fra før validering til skjema, vedlegg og tasks er committet eller rullet tilbake, og virker på tvers av podder.
GET-kall og andre søknadstyper omfattes ikke.

`kjoreliste.laas-timeout-sekunder` begrenser låseventingen til 5 sekunder som standard.
Timeout gir HTTP 409; etter vellykket låseopptak gir en allerede innsendt uke fortsatt HTTP 400.
Dette er ikke en grense for hele HTTP-kallet. Både ventende og behandlende kall opptar databaseforbindelser.

Beskyttelsen er komplett først når alle podder kjører versjonen med lås. Følg HTTP-latens, 409/5xx og Hikari-poolventing ved utrulling.
Rollback krever ingen databaseendring, men gjeninnfører risikoen for duplikater. Historiske duplikater ryddes ikke automatisk.

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
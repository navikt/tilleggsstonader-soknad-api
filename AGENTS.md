## Søknad-api og dataflyt

Kotlin-koden ligger under `src/main/kotlin/no/nav/tilleggsstonader/soknad/`.

1. `soknad/SøknadController.kt` mottar søknads-DTO-en fra `tilleggsstonader-soknad`. Mapperne under `soknad/` bygger den delte søknadskontrakten i `InnsendtSkjema` fra `tilleggsstonader-kontrakter`.
2. `soknad/SkjemaService.kt` lagrer kontraktsobjektet som JSON og oppretter `LagPdfTask` og en separat notifikasjonstask. Vedlegg lagres sammen med søknaden.
3. `prosessering/LagPdfTask.kt` lager PDF. Når tasken fullføres, oppretter den `ArkiverSøknadTask`. PDF-generatorens `dokument/pdf/SøknadTreeWalker.kt` går gjennom kontraktfeltene, inkludert spørsmål-/svartekst i `EnumFelt`.
4. `arkivering/ArkiveringService.kt` og `ArkiverDokumentRequestMapper.kt` sender strukturert JSON, PDF og vedlegg til `tilleggsstonader-integrasjoner` via `IntegrasjonerClient` på `api/arkiv`. Returnert journalpost-ID lagres på skjemaet.
5. Sak mottar en journalposthendelse på Kafka og henter originaldokumentet fra journalposten via integrasjoner. Denne søknadsflyten er ikke et direkte kall fra søknad-api til sak-api, og en fullført innsending betyr ikke at sak allerede har mottatt søknaden.

## Justering av kontrakt

- For lokal kontraktsbruk avkommenteres `includeBuild("../tilleggsstonader-kontrakter")` i `settings.gradle.kts`.
- Ikke endre den publiserte kontraktsversjonen som del av denne lokale koblingen.
- Følg med på feilede PDF-/arkiveringstasks ved endringer i obligatoriske kontraktfelt. Ikke logg søknadsinnhold.

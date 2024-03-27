package no.nav.tilleggsstonader.soknad.infrastruktur

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("mock-familie-soknad-controller")
@RestController
@RequestMapping("api/mellomlager")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class MellomlagringController {

    val storage = mutableMapOf<String, String>()

    @PostMapping(
        path = ["/{stonad}"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun mellomlagreSøknad(
        @PathVariable("stonad") stønad: String,
        @RequestBody(required = true) søknad: String,
    ): ResponseEntity<Unit> {
        storage[id(stønad)] = søknad
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping(path = ["/{stonad}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentMellomlagretSøknad(@PathVariable("stonad") stønad: String): ResponseEntity<String> {
        return storage[id(stønad)]
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()
    }

    @DeleteMapping(path = ["/{stonad}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun slettMellomlagretSøknad(@PathVariable("stonad") stønad: String): ResponseEntity<String> {
        storage.remove(id(stønad))
        return ResponseEntity.noContent().build()
    }

    private fun id(stønad: String) = stønad + EksternBrukerUtils.hentFnrFraToken()
}

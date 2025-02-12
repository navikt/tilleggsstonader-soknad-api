package no.nav.tilleggsstonader.soknad.infrastruktur

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Profile("mock-familie-vedlegg-controller")
@RestController
@RequestMapping("api/vedlegg")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class VedleggController {
    val storage = mutableMapOf<UUID, ByteArray>()

    @PostMapping(
        path = ["{bucket}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun addAttachment(
        @PathVariable("bucket") bucket: String,
        @RequestParam("file") multipartFile: MultipartFile,
    ): ResponseEntity<Any> {
        if (multipartFile.isEmpty) {
            return ResponseEntity(mapOf("melding" to "CODE=DOCUMENT_MISSING"), HttpStatus.BAD_REQUEST)
        }

        val bytes = multipartFile.bytes

        if (bytes.size > 10 * 1024 * 1024) {
            return ResponseEntity(mapOf("melding" to "CODE=IMAGE_TOO_LARGE"), HttpStatus.BAD_REQUEST)
        }

        val uuid = UUID.randomUUID()

        storage[uuid] = bytes
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf("dokumentId" to uuid.toString(), "filnavn" to multipartFile.originalFilename!!))
    }
}

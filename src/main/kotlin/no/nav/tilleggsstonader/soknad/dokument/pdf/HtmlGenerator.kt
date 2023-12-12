package no.nav.tilleggsstonader.soknad.dokument.pdf

import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.util.DatoFormat.DATE_FORMAT_NORSK
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HtmlGenerator(
    @Value("\${pdf.html.prettyPrint:false}")
    private val prettyPrint: Boolean,
) {

    fun generateHtml(stønadstype: Stønadstype, felter: Avsnitt, mottattTidspunkt: LocalDateTime): String {
        return createHTMLDocument().html {
            head {
                meta {
                    httpEquiv = "content-type"
                    content = "text/html; charset=utf-8"
                }
                style { unsafe { raw(søknadCss) } }
                title { +stønadstype.tittel }
            }
            body {
                div("header") {
                    div("ikon-og-dato") {
                        unsafe { raw(navIkone) }
                        p { +mottattTidspunkt.toLocalDate().format(DATE_FORMAT_NORSK) }
                    }
                    div("stonad-tittel") {
                        h1 { +stønadstype.tittel }
                    }
                    mapFelter(felter)
                }
            }
        }.serialize(prettyPrint = prettyPrint)
    }

    private fun FlowContent.header(avsnitt: Avsnitt, nivå: Int, className: String) {
        return when (nivå) {
            1 -> h1(className) { +avsnitt.label }
            2 -> h2(className) { +avsnitt.label }
            3 -> h3(className) { +avsnitt.label }
            else -> h4(className) { +avsnitt.label }
        }
    }

    private fun FlowContent.mapFelter(verdier: HtmlFelt, nivå: Int = 1) {
        val nivåClassName = "level-$nivå"
        return when (verdier) {
            is Verdi -> {
                verdier.alternativer?.let {
                    div("alternativer") { +it.joinToString(",") }
                }
                +verdier.verdi
            }

            is Avsnitt -> div {
                header(verdier, nivå, nivåClassName)
                verdier.verdier.map {
                    div(nivåClassName) {
                        mapFelter(it, minOf(nivå + 1, 4))
                    }
                }
            }

            is HorisontalLinje -> hr { }
        }
    }
}

private val søknadCss = readFile("html/søknad.css")

private val navIkone = readFile("html/nav_svg.html")

private fun readFile(filnavn: String): String =
    HtmlGenerator::class.java.classLoader.getResource(filnavn)!!.readText()

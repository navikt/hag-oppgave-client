package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import no.nav.helsearbeidsgiver.oppgave.domain.HentOppgaverRequest
import no.nav.helsearbeidsgiver.oppgave.domain.Oppgave
import no.nav.helsearbeidsgiver.oppgave.domain.OppgaveListeResponse
import no.nav.helsearbeidsgiver.oppgave.domain.OpprettOppgaveRequest
import no.nav.helsearbeidsgiver.oppgave.domain.OpprettOppgaveResponse
import no.nav.helsearbeidsgiver.oppgave.exception.HentOppgaveFeiletException
import no.nav.helsearbeidsgiver.oppgave.exception.OpprettOppgaveFeiletException
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger

class OppgaveClient(
    private val url: String,
    private val getToken: () -> String,
) {
    private val logger = logger()
    private val sikkerLogger = sikkerLogger()

    private val httpClient = createHttpClient()

    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OpprettOppgaveResponse =
        runCatching {
            httpClient
                .post(url) {
                    contentTypeJson()
                    correlationId()
                    bearerAuth(getToken())
                    setBody(opprettOppgaveRequest)
                }.body<OpprettOppgaveResponse>()
        }.onSuccess {
            sikkerLogger.info("Oppgave opprettet: ${it.id}")
        }.getOrElse { e ->
            throw OpprettOppgaveFeiletException(e)
        }

    suspend fun hentOppgaver(hentOppgaverRequest: HentOppgaverRequest): OppgaveListeResponse =
        runCatching {
            httpClient
                .get(url) {
                    contentTypeJson()
                    correlationId()
                    bearerAuth(getToken())
                    parameter("oppgavetype", hentOppgaverRequest.oppgavetype)
                    parameter("tema", hentOppgaverRequest.tema)
                    parameter("behandlingstype", hentOppgaverRequest.behandlingstype)
                    parameter("behandlingstema", hentOppgaverRequest.behandlingstema)
                    parameter("statuskategori", hentOppgaverRequest.statuskategori)
                    parameter("tildeltEnhetsnr", hentOppgaverRequest.tildeltEnhetsnr)
                    parameter("tilordnetRessurs", hentOppgaverRequest.tilordnetRessurs)
                    parameter("journalpostId", hentOppgaverRequest.journalpostId)
                    parameter("saksreferanse", hentOppgaverRequest.saksreferanse)
                    parameter("orgnr", hentOppgaverRequest.orgnr)
                    parameter("limit", hentOppgaverRequest.limit)
                    parameter("offset", hentOppgaverRequest.offset)
                }.body<OppgaveListeResponse>()
        }.onSuccess {
            logger.info("Antall oppgaver hentet: ${it.antallTreffTotalt}")
        }.getOrElse { e ->
            throw HentOppgaveFeiletException(e)
        }

    suspend fun hentOppgave(oppgaveId: Int): Oppgave =
        runCatching {
            httpClient
                .get("$url/$oppgaveId") {
                    contentTypeJson()
                    correlationId()
                    bearerAuth(getToken())
                }.body<Oppgave>()
        }.onSuccess {
            sikkerLogger.info("Oppgave hentet: ${it.id}")
        }.getOrElse { e ->
            throw HentOppgaveFeiletException(e)
        }
}

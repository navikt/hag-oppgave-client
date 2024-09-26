package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
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
    private val maxRetries: Int = 0,
) {
    private val httpClient = createHttpClient(maxRetries)

    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OpprettOppgaveResponse {
        val result =
            runCatching {
                val httpResponse =
                    httpClient.post(url) {
                        contentTypeJson()
                        correlationId()
                        header("Authorization", "Bearer ${getToken()}")
                        setBody(opprettOppgaveRequest)
                    }
                httpResponse.body<OpprettOppgaveResponse>()
            }
        return result.fold(
            onSuccess = { response ->
                response.also { sikkerLogger().info("Oppgave opprettet: ${it.id}") }
            },
            onFailure = { e ->
                throw OpprettOppgaveFeiletException(e)
            },
        )
    }

    suspend fun hentOppgaver(hentOppgaverRequest: HentOppgaverRequest): OppgaveListeResponse {
        val result =
            runCatching {
                val httpResponse =
                    httpClient.get(url) {
                        contentTypeJson()
                        correlationId()
                        header("Authorization", "Bearer ${getToken()}")
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
                    }
                httpResponse.body<OppgaveListeResponse>()
            }
        return result.fold(
            onSuccess = { response ->
                response.also { logger().info("Antall oppgaver hentet: ${it.antallTreffTotalt}") }
            },
            onFailure = { e ->
                throw HentOppgaveFeiletException(e)
            },
        )
    }

    suspend fun hentOppgave(oppgaveId: Int): Oppgave {
        val result =
            runCatching {
                val httpResponse =
                    httpClient.get("$url/$oppgaveId") {
                        contentTypeJson()
                        correlationId()
                        header("Authorization", "Bearer ${getToken()}")
                    }
                httpResponse.body<Oppgave>()
            }
        return result.fold(
            onSuccess = { response ->
                response.also { sikkerLogger().info("Oppgave hentet: ${it.id}") }
            },
            onFailure = { e ->
                throw HentOppgaveFeiletException(e)
            },
        )
    }
}

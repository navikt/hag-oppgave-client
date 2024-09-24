package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import no.nav.helsearbeidsgiver.utils.log.MdcUtils
import no.nav.helsearbeidsgiver.utils.log.logger

interface OppgaveClient {
    suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OpprettOppgaveResponse

    suspend fun hentOppgaver(hentOppgaverRequest: HentOppgaverRequest): OppgaveListeResponse

    suspend fun hentOppgave(oppgaveId: Int): Oppgave
}

class OppgaveClientImpl(
    private val url: String,
    private val getToken: () -> String,
) : OppgaveClient {
    override suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OpprettOppgaveResponse {
        val token = getToken()
        val httpClient = createHttpClient()
        val httpResponse =
            httpClient.post(url) {
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                header("Authorization", "Bearer $token")
                header("X-Correlation-ID", MdcUtils.getCallId())
                setBody(opprettOppgaveRequest)
            }
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.call.response.body()
            }

            HttpStatusCode.Created -> {
                httpResponse.call.response.body()
            }

            else -> {
                logger().error("Feilet å opprette oppgave : $httpResponse")
                throw RuntimeException("Feilet å opprette oppgave")
            }
        }
    }

    override suspend fun hentOppgaver(hentOppgaverRequest: HentOppgaverRequest): OppgaveListeResponse {
        val httpClient = createHttpClient()
        val httpResponse =
            httpClient.get("$url") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getToken()}")
                header("X-Correlation-ID", MdcUtils.getCallId())
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
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.call.response.body()
            }

            else -> {
                logger().error("Feilet å hente oppgave : $httpResponse")
                throw RuntimeException("Feilet å hente oppgave")
            }
        }
    }

    override suspend fun hentOppgave(oppgaveId: Int): Oppgave {
        val httpClient = createHttpClient()
        val httpResponse =
            httpClient.get("$url/$oppgaveId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getToken()}")
                header("X-Correlation-ID", MdcUtils.getCallId())
            }
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                httpResponse.call.response.body()
            }

            else -> {
                logger().error("Feilet å hente oppgave : $httpResponse")
                throw RuntimeException("Feilet å hente oppgave")
            }
        }
    }
}

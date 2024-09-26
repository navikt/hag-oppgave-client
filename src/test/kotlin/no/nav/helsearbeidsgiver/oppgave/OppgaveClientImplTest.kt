package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.oppgave.domain.Behandlingstema
import no.nav.helsearbeidsgiver.oppgave.domain.Behandlingstype
import no.nav.helsearbeidsgiver.oppgave.domain.HentOppgaverRequest
import no.nav.helsearbeidsgiver.oppgave.domain.Oppgavetype
import no.nav.helsearbeidsgiver.oppgave.domain.OpprettOppgaveRequest
import no.nav.helsearbeidsgiver.oppgave.domain.Prioritet
import no.nav.helsearbeidsgiver.oppgave.domain.Statuskategori
import no.nav.helsearbeidsgiver.oppgave.domain.Tema
import no.nav.helsearbeidsgiver.oppgave.exception.HentOppgaveFeiletException
import no.nav.helsearbeidsgiver.oppgave.exception.OpprettOppgaveFeiletException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val URL = "http://dummyUrl"

class OppgaveClientImplTest {
    @Test
    fun `hentOppgave should return Oppgave on success`() =
        runBlocking {
            val now = LocalDate.now()
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{
                    "id": 1,
                    "oppgavetype": "TYPE",
                    "aktivDato": "${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                    "prioritet": "HOY",
                    "status": "AAPEN"
                }""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                    )

                val response = oppgaveClient.hentOppgave(1)

                assertEquals(1, response.id)
                assertEquals("TYPE", response.oppgavetype)
                assertEquals(now, response.aktivDato)
                assertEquals("HOY", response.prioritet)
                assertEquals("AAPEN", response.status)
            }
        }

    @Test
    fun `hentOppgave should throw HentOppgaveFeiletException on failure`() =
        runBlocking {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = "Not Found",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                    )

                val exception =
                    assertThrows<HentOppgaveFeiletException> {
                        runBlocking { oppgaveClient.hentOppgave(1) }
                    }

                assertTrue(exception.cause is ClientRequestException)
            }
        }

    @Test
    fun `opprettOppgave should return OpprettOppgaveResponse on success`() =
        runBlocking {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{ "id": 1,"status": "CREATED" }""",
                        status = HttpStatusCode.Created,
                        headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                    )
                val request =
                    OpprettOppgaveRequest(
                        oppgavetype = "TYPE",
                        aktivDato = LocalDate.now(),
                        prioritet = Prioritet.HOY,
                    )

                val response = oppgaveClient.opprettOppgave(request)

                assertEquals(1, response.id)
            }
        }

    @Test
    fun `opprettOppgave should throw OpprettOppgaveFeiletException on failure`() =
        runBlocking {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = "Internal Server Error",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                    )
                val request =
                    OpprettOppgaveRequest(
                        oppgavetype = "TYPE",
                        aktivDato = LocalDate.now(),
                        prioritet = Prioritet.HOY,
                    )

                val exception =
                    assertThrows<OpprettOppgaveFeiletException> {
                        runBlocking { oppgaveClient.opprettOppgave(request) }
                    }

                assertTrue(exception.cause is ServerResponseException)
            }
        }

    @Test
    fun `hentOppgaver should return OppgaveListeResponse on success`() =
        runBlocking {
            val now = LocalDate.now()
            val mockEngine =
                MockEngine { _ ->

                    respond(
                        content = """{
                "oppgaver": [
                    {
                        "id": 1,
                        "oppgavetype": "INNT",
                        "status": "AAPEN"
                        "tema": "SYK",
                        "behandlingstype": "ae0106",
                        "behandlingstema": "ab0061",
                        "tildeltEnhetsnr": "1234",
                        "tilordnetRessurs": "5678",
                        "journalpostId": "91011",
                        "saksreferanse": "121314",
                        "orgnr": "516171111"
                        "aktivDato": "${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                        "prioritet": "HOY"
                    }
                ],
                "antallTreffTotalt": 1
            }""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                    )
                val request =
                    HentOppgaverRequest(
                        oppgavetype = Oppgavetype.INNTEKTSMELDING,
                        tema = Tema.SYK,
                        behandlingstype = Behandlingstype.UTLAND,
                        behandlingstema = Behandlingstema.NORMAL,
                        statuskategori = Statuskategori.AAPEN,
                        tildeltEnhetsnr = "1234",
                        tilordnetRessurs = "5678",
                        journalpostId = "91011",
                        saksreferanse = "121314",
                        orgnr = "516171111",
                        limit = 10,
                        offset = 0,
                    )

                val response = oppgaveClient.hentOppgaver(request)

                assertEquals(1, response.oppgaver.size)
                assertEquals(1, response.oppgaver[0].id)
                assertEquals(Oppgavetype.INNTEKTSMELDING, response.oppgaver[0].oppgavetype)
                assertEquals(Statuskategori.AAPEN.name, response.oppgaver[0].status)
                assertEquals(Tema.SYK, response.oppgaver[0].tema)
                assertEquals(Behandlingstype.UTLAND, response.oppgaver[0].behandlingstype)
                assertEquals(Behandlingstema.NORMAL, response.oppgaver[0].behandlingstema)
                assertEquals("1234", response.oppgaver[0].tildeltEnhetsnr)
                assertEquals("5678", response.oppgaver[0].tilordnetRessurs)
                assertEquals("91011", response.oppgaver[0].journalpostId)
                assertEquals("121314", response.oppgaver[0].saksreferanse)
                assertEquals("516171111", response.oppgaver[0].orgnr)
                assertEquals(now, response.oppgaver[0].aktivDato)
            }
        }

    @Test
    fun `hentOppgaver should throw HentOppgaveFeiletException on failure`() =
        runBlocking {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = "Not Found",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                    )
                val request =
                    HentOppgaverRequest(
                        oppgavetype = Oppgavetype.INNTEKTSMELDING,
                        tema = Tema.SYK,
                        behandlingstype = Behandlingstype.UTLAND,
                        behandlingstema = Behandlingstema.NORMAL,
                        statuskategori = Statuskategori.AAPEN,
                        tildeltEnhetsnr = "1234",
                        tilordnetRessurs = "5678",
                        journalpostId = "91011",
                        saksreferanse = "121314",
                        orgnr = "516171111",
                        limit = 10,
                        offset = 0,
                    )

                val exception =
                    assertThrows<HentOppgaveFeiletException> {
                        runBlocking { oppgaveClient.hentOppgaver(request) }
                    }

                assertTrue(exception.cause is ClientRequestException)
            }
        }

    @Test
    fun `hentOppgave should retry on SocketTimeoutException`() =
        runBlocking {
            var callCount = 0
            val mockEngine =
                MockEngine { _ ->
                    callCount++
                    throw SocketTimeoutException("Connection timeout")
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient(2) } returns httpClientMock(mockEngine, 2)

                val oppgaveClient =
                    OppgaveClientImpl(
                        URL,
                        getToken = { "token" },
                        maxRetries = 2,
                    )

                val exception =
                    assertThrows<HentOppgaveFeiletException> {
                        runBlocking { oppgaveClient.hentOppgave(1) }
                    }
                assertTrue(exception.cause is SocketTimeoutException)
                assertEquals(3, callCount)
            }
        }

    private fun httpClientMock(
        mockEngine: MockEngine,
        maxRetry: Int = 0,
    ) = HttpClient(mockEngine) {
        configure(maxRetry)
    }
}

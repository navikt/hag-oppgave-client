package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

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

                val oppgaveClient = OppgaveClientImpl("http://localhost") { "token" }

                val response = oppgaveClient.hentOppgave(1)

                assertEquals(1, response.id)
                assertEquals("TYPE", response.oppgavetype)
                assertEquals(now, response.aktivDato)
                assertEquals("HOY", response.prioritet)
                assertEquals("AAPEN", response.status)
            }
        }

    @Test
    fun `opprettOppgave should return OpprettOppgaveResponse on success`() =
        runBlocking {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{
                "id": 1,
                "status": "CREATED"
            }""",
                        status = HttpStatusCode.Created,
                        headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                    )
                }

            mockkStatic(::createHttpClient) {
                every { createHttpClient() } returns httpClientMock(mockEngine)

                val oppgaveClient = OppgaveClientImpl("http://localhost") { "token" }
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

                val oppgaveClient = OppgaveClientImpl("http://localhost") { "token" }
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

    private fun httpClientMock(mockEngine: MockEngine) =
        HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
}

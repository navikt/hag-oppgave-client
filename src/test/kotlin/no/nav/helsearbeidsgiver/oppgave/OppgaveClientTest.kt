package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.mockk.every
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import no.nav.helsearbeidsgiver.utils.json.toJson
import no.nav.helsearbeidsgiver.utils.test.json.removeJsonWhitespace
import no.nav.helsearbeidsgiver.utils.test.mock.mockStatic
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val URL = "http://dummyUrl"

class OppgaveClientTest {
    @Test
    fun `hentOppgave should return Oppgave on success`() {
        val now = LocalDate.now()
        val respons =
            """
            {
                "id": 1,
                "oppgavetype": "TYPE",
                "aktivDato": ${now.toJson()},
                "prioritet": "HOY",
                "status": "AAPEN"
            }
            """.removeJsonWhitespace()

        val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.OK to respons)

        val oppgave = runBlocking { mockOppgaveClient.hentOppgave(1) }

        assertEquals(1, oppgave.id)
        assertEquals("TYPE", oppgave.oppgavetype)
        assertEquals(now, oppgave.aktivDato)
        assertEquals("HOY", oppgave.prioritet)
        assertEquals("AAPEN", oppgave.status)
    }

    @Test
    fun `hentOppgave should throw HentOppgaveFeiletException on failure`() {
        val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.NotFound to "Not Found")

        val exception =
            assertThrows<HentOppgaveFeiletException> {
                runBlocking { mockOppgaveClient.hentOppgave(1) }
            }

        assertTrue(exception.cause is ClientRequestException)
    }

    @Test
    fun `opprettOppgave should return OpprettOppgaveResponse on success`() {
        val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.Created to """{ "id": 1,"status": "CREATED" }""")

        val request =
            OpprettOppgaveRequest(
                oppgavetype = "TYPE",
                aktivDato = LocalDate.now(),
                prioritet = Prioritet.HOY,
            )

        val response = runBlocking { mockOppgaveClient.opprettOppgave(request) }

        assertEquals(1, response.id)
    }

    @Test
    fun `opprettOppgave should throw OpprettOppgaveFeiletException on failure`() {
        val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.InternalServerError to "Internal Server Error")

        val request =
            OpprettOppgaveRequest(
                oppgavetype = "TYPE",
                aktivDato = LocalDate.now(),
                prioritet = Prioritet.HOY,
            )

        val exception =
            assertThrows<OpprettOppgaveFeiletException> {
                runBlocking { mockOppgaveClient.opprettOppgave(request) }
            }

        assertTrue(exception.cause is ServerResponseException)
    }

    @Test
    fun `hentOppgaver should return OppgaveListeResponse on success`() {
        val now = LocalDate.now()
        val respons =
            """
            {
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
                        "aktivDato": ${now.toJson()},
                        "prioritet": "HOY"
                    }
                ],
                "antallTreffTotalt": 1
            }
            """

        val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.OK to respons)

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

        val oppgaver = runBlocking { mockOppgaveClient.hentOppgaver(request) }

        assertEquals(1, oppgaver.oppgaver.size)
        assertEquals(1, oppgaver.oppgaver[0].id)
        assertEquals(Oppgavetype.INNTEKTSMELDING, oppgaver.oppgaver[0].oppgavetype)
        assertEquals(Statuskategori.AAPEN.name, oppgaver.oppgaver[0].status)
        assertEquals(Tema.SYK, oppgaver.oppgaver[0].tema)
        assertEquals(Behandlingstype.UTLAND, oppgaver.oppgaver[0].behandlingstype)
        assertEquals(Behandlingstema.NORMAL, oppgaver.oppgaver[0].behandlingstema)
        assertEquals("1234", oppgaver.oppgaver[0].tildeltEnhetsnr)
        assertEquals("5678", oppgaver.oppgaver[0].tilordnetRessurs)
        assertEquals("91011", oppgaver.oppgaver[0].journalpostId)
        assertEquals("121314", oppgaver.oppgaver[0].saksreferanse)
        assertEquals("516171111", oppgaver.oppgaver[0].orgnr)
        assertEquals(now, oppgaver.oppgaver[0].aktivDato)
    }

    @Test
    fun `hentOppgaver should throw HentOppgaveFeiletException on failure`() {
        val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.NotFound to "Not Found")

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
                runBlocking { mockOppgaveClient.hentOppgaver(request) }
            }

        assertTrue(exception.cause is ClientRequestException)
    }

    @Test
    fun `hentOppgave should retry on SocketTimeoutException`() {
        var callCount = 0
        val mockEngine =
            MockEngine { _ ->
                callCount++
                throw SocketTimeoutException("Connection timeout")
            }

        val mockOppgaveClient =
            mockStatic(::createHttpClient) {
                every { createHttpClient(2) } returns HttpClient(mockEngine) { configure(2) }

                OppgaveClient(
                    URL,
                    getToken = { "token" },
                    maxRetries = 2,
                )
            }

        runTest {
            val exception =
                assertThrows<HentOppgaveFeiletException> {
                    mockOppgaveClient.hentOppgave(1)
                }

            assertTrue(exception.cause is SocketTimeoutException)
            assertEquals(3, callCount)
        }
    }
}

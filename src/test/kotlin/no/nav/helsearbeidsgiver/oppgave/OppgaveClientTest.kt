package no.nav.helsearbeidsgiver.oppgave

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
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
import java.time.LocalDate

class OppgaveClientTest :
    FunSpec({
        test("oppgave opprettes") {
            val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.Created to Mock.opprettOppgaveResponse)

            val response = mockOppgaveClient.opprettOppgave(Mock.opprettOppgaveRequest)

            response.id shouldBe 1
        }

        test("oppgaver hentes") {
            val now = LocalDate.now()

            val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.OK to Mock.hentOppgaverResponse(now))

            val oppgaver = mockOppgaveClient.hentOppgaver(Mock.hentOppgaverRequest)

            oppgaver.oppgaver shouldHaveSize 1
            oppgaver.oppgaver[0].also { oppgave ->
                oppgave.id shouldBe 1
                oppgave.oppgavetype shouldBe Oppgavetype.INNTEKTSMELDING
                oppgave.status shouldBe Statuskategori.AAPEN.name
                oppgave.tema shouldBe Tema.SYK
                oppgave.behandlingstype shouldBe Behandlingstype.UTLAND
                oppgave.behandlingstema shouldBe Behandlingstema.NORMAL
                oppgave.tildeltEnhetsnr shouldBe "1234"
                oppgave.tilordnetRessurs shouldBe "5678"
                oppgave.journalpostId shouldBe "91011"
                oppgave.saksreferanse shouldBe "121314"
                oppgave.orgnr shouldBe "516171111"
                oppgave.aktivDato shouldBe now
            }
        }

        test("oppgave hentes") {
            val now = LocalDate.now()

            val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.OK to Mock.hentOppgaveResponse(now))

            val oppgave = mockOppgaveClient.hentOppgave(1)

            oppgave.id shouldBe 1
            oppgave.oppgavetype shouldBe "TYPE"
            oppgave.aktivDato shouldBe now
            oppgave.prioritet shouldBe "HOY"
            oppgave.status shouldBe "AAPEN"
        }

        listOf<Triple<String, suspend OppgaveClient.() -> Unit, String>>(
            Triple(
                OppgaveClient::opprettOppgave.name,
                { opprettOppgave(Mock.opprettOppgaveRequest) },
                Mock.opprettOppgaveResponse,
            ),
            Triple(
                OppgaveClient::hentOppgaver.name,
                { hentOppgaver(Mock.hentOppgaverRequest) },
                Mock.hentOppgaverResponse(LocalDate.now()),
            ),
            Triple(
                OppgaveClient::hentOppgave.name,
                { hentOppgave(1) },
                Mock.hentOppgaveResponse(LocalDate.now()),
            ),
        ).forEach { (testFnName, testFn, okResponse) ->
            context(testFnName) {
                test("feiler ved 4xx-feil") {
                    val mockOppgaveClient = mockOppgaveClient(HttpStatusCode.NotFound to "")

                    val e =
                        shouldThrowAny {
                            mockOppgaveClient.testFn()
                        }

                    shouldBeCorrectExceptionType(testFnName, e)

                    (e.cause as? ClientRequestException).also {
                        it.shouldNotBeNull()
                        it.response.status shouldBe HttpStatusCode.NotFound
                    }
                }

                test("lykkes ved færre 5xx-feil enn max retries (3)") {
                    val mockOppgaveClient =
                        mockOppgaveClient(
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.OK to okResponse,
                        )

                    runTest {
                        shouldNotThrowAny {
                            mockOppgaveClient.testFn()
                        }
                    }
                }

                test("feiler ved flere 5xx-feil enn max retries (3)") {
                    val mockOppgaveClient =
                        mockOppgaveClient(
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                        )

                    runTest {
                        val e =
                            shouldThrowAny {
                                mockOppgaveClient.testFn()
                            }

                        shouldBeCorrectExceptionType(testFnName, e)

                        (e.cause as? ServerResponseException).also {
                            it.shouldNotBeNull()
                            it.response.status shouldBe HttpStatusCode.InternalServerError
                        }
                    }
                }

                test("kall feiler og prøver på nytt ved timeout") {
                    val mockOppgaveClient =
                        mockOppgaveClient(
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to okResponse,
                        )

                    runTest {
                        shouldNotThrowAny {
                            mockOppgaveClient.testFn()
                        }
                    }
                }
            }
        }
    })

private fun shouldBeCorrectExceptionType(
    testFnName: String,
    e: Throwable,
) {
    when (testFnName) {
        OppgaveClient::opprettOppgave.name ->
            e.shouldBeInstanceOf<OpprettOppgaveFeiletException>()
        else ->
            e.shouldBeInstanceOf<HentOppgaveFeiletException>()
    }
}

object Mock {
    val opprettOppgaveRequest =
        OpprettOppgaveRequest(
            oppgavetype = "TYPE",
            aktivDato = LocalDate.now(),
            prioritet = Prioritet.HOY,
        )

    val hentOppgaverRequest =
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

    val opprettOppgaveResponse =
        """
        {
            "id": 1,
            "status": "CREATED"
        }
        """.removeJsonWhitespace()

    fun hentOppgaveResponse(aktivDato: LocalDate): String =
        """
        {
            "id": 1,
            "oppgavetype": "TYPE",
            "aktivDato": ${aktivDato.toJson()},
            "prioritet": "HOY",
            "status": "AAPEN"
        }
        """.removeJsonWhitespace()

    fun hentOppgaverResponse(aktivDato: LocalDate): String =
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
                    "aktivDato": ${aktivDato.toJson()},
                    "prioritet": "HOY"
                }
            ],
            "antallTreffTotalt": 1
        }
        """.removeJsonWhitespace()
}

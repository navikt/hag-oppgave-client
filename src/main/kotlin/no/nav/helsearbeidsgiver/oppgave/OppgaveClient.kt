package no.nav.syfo.client

import io.ktor.client.HttpClient
import org.slf4j.LoggerFactory
import java.time.LocalDate

class OppgaveClient constructor(
    val oppgavebehndlingUrl: String,
    val httpClient: HttpClient,
) {
    private val log = LoggerFactory.getLogger(OppgaveClient::class.java)

    private suspend fun opprettOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OpprettOppgaveResponse {
        TODO("Implementer opprettOppgave")
    }

    private suspend fun hentOppgave(
        oppgavetype: String,
        journalpostId: String,
    ): OppgaveResponse {
        TODO("Implementer hentOppgave")
    }

    private suspend fun hentHvisOppgaveFinnes(
        oppgavetype: String,
        journalpostId: String,
    ): Any? {
        TODO("Implementer hentHvisOppgaveFinnes")
    }
}

data class OpprettOppgaveRequest(
    val tildeltEnhetsnr: String? = null,
    val aktoerId: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val beskrivelse: String? = null,
    val tema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val behandlingstema: String? = null,
    val aktivDato: LocalDate,
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: String,
)

data class OpprettOppgaveResponse(
    val id: Int,
)

data class OppgaveResponse(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>,
)

data class Oppgave(
    val id: Int,
    val tildeltEnhetsnr: String?,
    val aktoerId: String?,
    val journalpostId: String?,
    val saksreferanse: String?,
    val tema: String?,
    val oppgavetype: String?,
)

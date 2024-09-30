@file:UseSerializers(LocalDateSerializer::class, LocalDateTimeSerializer::class)

package no.nav.helsearbeidsgiver.oppgave.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.helsearbeidsgiver.utils.json.serializer.LocalDateSerializer
import no.nav.helsearbeidsgiver.utils.json.serializer.LocalDateTimeSerializer
import java.time.LocalDate

@Serializable
data class Oppgave(
    val id: Int? = null,
    val versjon: Int? = null,
    val tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val aktoerId: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val tema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val behandlingstema: String? = null,
    val aktivDato: LocalDate,
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: String,
    val status: String? = null,
    val mappeId: Int? = null,
    val orgnr: String? = null,
)

@Serializable
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
    val prioritet: Prioritet,
)

@Serializable
data class HentOppgaverRequest(
    val oppgavetype: String? = null,
    val tema: String? = null,
    val behandlingstype: String? = null,
    val behandlingstema: String? = null,
    val statuskategori: Statuskategori? = null,
    val tildeltEnhetsnr: String? = null,
    val tilordnetRessurs: String? = null,
    val journalpostId: String? = null,
    val saksreferanse: String? = null,
    val orgnr: String? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val sorteringsrekkefolge: String? = null,
    val sorteringsfelt: String? = null,
)

@Serializable
data class OpprettOppgaveResponse(
    val id: Int,
)

@Serializable
data class OppgaveListeResponse(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>,
)

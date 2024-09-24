package no.nav.helsearbeidsgiver.oppgave

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    @Serializable(LocalDateSerializer::class)
    val aktivDato: LocalDate,
    @Serializable(LocalDateSerializer::class)
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
    @Serializable(LocalDateSerializer::class)
    val aktivDato: LocalDate,
    @Serializable(LocalDateSerializer::class)
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: Prioritet,
)

@Serializable
data class HentOppgaverRequest(
    val oppgavetype: String?,
    val tema: String?,
    val behandlingstype: String?,
    val behandlingstema: String?,
    val statuskategori: Statuskategori?,
    val tildeltEnhetsnr: String?,
    val tilordnetRessurs: String?,
    val journalpostId: String?,
    val saksreferanse: String?,
    val orgnr: String?,
    val limit: Int?,
    val offset: Int?,
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

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: LocalDate,
    ) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)
}

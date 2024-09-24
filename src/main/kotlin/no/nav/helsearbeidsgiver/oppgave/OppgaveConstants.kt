package no.nav.helsearbeidsgiver.oppgave

import kotlinx.serialization.Serializable

@Serializable
object Oppgavetype {
    const val INNTEKTSMELDING = "INNT"
    const val FORDELINGSOPPGAVE = "FDR"
    const val ROBOT_BEHANDLING = "ROB_BEH"
}

@Serializable
object Behandlingstype {
    const val UTLAND = "ae0106"
    const val DIGITAL_KRAV = "ae0121"
}

@Serializable
object Behandlingstema {
    const val NORMAL = "ab0061"
    const val SPEIL = "ab0455"
    const val UTBETALING_TIL_BRUKER = "ab0458"
    const val BESTRIDER_SYKEMELDING = "ab0421"
    const val FRITAK_AGP = "ab0200"
}

@Serializable
object Tema {
    const val SYK = "SYK"
}

@Serializable
enum class Statuskategori { AAPEN, AVSLUTTET }

@Serializable
enum class Prioritet { HOY, NORM, LAV }

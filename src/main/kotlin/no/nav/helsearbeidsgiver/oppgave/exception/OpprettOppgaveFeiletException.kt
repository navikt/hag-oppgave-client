package no.nav.helsearbeidsgiver.oppgave.exception

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger

private val sikkerLogger = sikkerLogger()

class OpprettOppgaveFeiletException(
    e: Throwable,
) : RuntimeException("Feilet å opprette oppgave: ", e) {
    init {
        val feilmelding =
            when (e) {
                is ClientRequestException -> "Oppgave-client: Feilet å opprette oppgave : ${e.response.status}, ${e.message}"
                is ServerResponseException -> "Oppgave-client: Feilet å opprette oppgave : ${e.response.status}, ${e.message}"
                else -> "Oppgave-client: Feilet å opprette oppgave : $e"
            }

        sikkerLogger.error(feilmelding)
    }
}

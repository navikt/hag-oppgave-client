package no.nav.helsearbeidsgiver.oppgave.exception

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger

private val sikkerLogger = sikkerLogger()

class HentOppgaveFeiletException(
    e: Throwable,
) : RuntimeException("Feilet å hente oppgave: ", e) {
    init {
        val feilmelding =
            when (e) {
                is ClientRequestException -> "Oppgave-client: Feilet å hente oppgave : ${e.response.status}, ${e.message}"
                is ServerResponseException -> "Oppgave-client: Feilet å hente oppgave : ${e.response.status}, ${e.message}"
                else -> "Oppgave-client: Feilet å hente oppgave : $e"
            }

        sikkerLogger.error(feilmelding)
    }
}

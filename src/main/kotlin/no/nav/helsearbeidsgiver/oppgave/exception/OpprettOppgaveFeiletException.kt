package no.nav.helsearbeidsgiver.oppgave.exception

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger

class OpprettOppgaveFeiletException(
    e: Throwable,
) : RuntimeException("Feilet å opprette oppgave: ", e) {
    init {
        when (e) {
            is ClientRequestException -> {
                sikkerLogger().error("Oppgave-client: Feilet å opprette oppgave : ${e.response.status}, ${e.message}")
            }

            is ServerResponseException -> {
                sikkerLogger().error("Oppgave-client: Feilet å opprette oppgave : ${e.response.status}, ${e.message}")
            }

            else -> {
                sikkerLogger().error("Oppgave-client: Feilet å opprette oppgave : $e")
            }
        }
    }
}

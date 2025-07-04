package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import no.nav.helsearbeidsgiver.utils.test.mock.mockStatic

fun mockOppgaveClient(vararg responses: Pair<HttpStatusCode, String>): OppgaveClient {
    val mockEngine =
        MockEngine.create {
            reuseHandlers = false
            requestHandlers.addAll(
                responses.map { (status, content) ->
                    {
                        respond(
                            content = content,
                            status = status,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }
                },
            )
        }

    val mockHttpClient = HttpClient(mockEngine) { configure(retries = 0) }

    return mockStatic(::createHttpClient) {
        every { createHttpClient() } returns mockHttpClient
        OppgaveClient(
            "mock-url",
            { "fake token" },
        )
    }
}

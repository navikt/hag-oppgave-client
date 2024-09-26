package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import no.nav.helsearbeidsgiver.utils.json.jsonConfig
import no.nav.helsearbeidsgiver.utils.log.MdcUtils

internal fun createHttpClient(maxRetries: Int = 0): HttpClient = HttpClient(Apache5) { configure(maxRetries) }

internal fun HttpClientConfig<*>.configure(retries: Int) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(jsonConfig)
    }
    install(HttpRequestRetry) {
        maxRetries = retries
        retryOnServerErrors(maxRetries)
        retryOnExceptionIf { _, cause ->
            cause.isRetryableException()
        }
        exponentialDelay()
    }
}

internal fun HttpRequestBuilder.correlationId() {
    header("X-Correlation-ID", MdcUtils.getCallId())
}

internal fun HttpRequestBuilder.contentTypeJson() {
    contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
}

private fun Throwable.isRetryableException() =
    when (this) {
        is SocketTimeoutException -> true
        is ConnectTimeoutException -> true
        is HttpRequestTimeoutException -> true
        is java.net.SocketTimeoutException -> true
        else -> false
    }

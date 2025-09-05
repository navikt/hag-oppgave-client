package no.nav.helsearbeidsgiver.oppgave

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import no.nav.helsearbeidsgiver.utils.json.jsonConfig
import no.nav.helsearbeidsgiver.utils.log.MdcUtils

internal fun createHttpClient(): HttpClient = HttpClient(Apache5) { configure() }

internal fun HttpClientConfig<*>.configure() {
    expectSuccess = true

    install(ContentNegotiation) {
        json(jsonConfig)
    }

    install(HttpRequestRetry) {
        retryOnException(
            maxRetries = 3,
            retryOnTimeout = true,
        )
        exponentialDelay()
    }

    install(HttpTimeout) {
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }
}

internal fun HttpRequestBuilder.correlationId() {
    header("X-Correlation-ID", MdcUtils.getCallId())
}

internal fun HttpRequestBuilder.contentTypeJson() {
    contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
}

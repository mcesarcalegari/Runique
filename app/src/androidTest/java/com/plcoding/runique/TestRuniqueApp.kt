package com.plcoding.runique

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/** The real app with only HTTP replaced by a Ktor [MockEngine] backend. */
class TestRuniqueApp : RuniqueApp() {
    override val testOverrideModules: List<Module> = listOf(
        module {
            single<HttpClient> { fakeBackendClient() }
        }
    )
}

private fun fakeBackendClient(): HttpClient {
    val engine = MockEngine { request ->
        val body = when {
            request.url.encodedPath.endsWith("/login") ->
                """{"accessToken":"access","refreshToken":"refresh","accessTokenExpirationTimestamp":0,"userId":"user"}"""
            request.url.encodedPath.endsWith("/runs") && request.method == HttpMethod.Get -> "[]"
            else -> "{}"
        }
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}

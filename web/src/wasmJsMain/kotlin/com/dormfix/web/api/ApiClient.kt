package com.dormfix.web.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Must match HelloResponse.java on the server. */
@Serializable
data class HelloResponse(
    val message: String,
    val serverTime: String,
)

/** All calls to the Spring Boot backend go through here. */
object ApiClient {
    private const val BASE_URL = "http://localhost:8080"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun hello(): HelloResponse = client.get("$BASE_URL/api/hello").body()
}

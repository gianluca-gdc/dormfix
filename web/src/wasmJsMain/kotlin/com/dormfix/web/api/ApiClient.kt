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

@Serializable
data class TicketResponse(
    val id: Int,
    val residentName: String,
    val category: String,
    val roomNumber: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: String,
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
)

/** All calls to the Spring Boot backend go through here. */
object ApiClient {
    private const val BASE_URL = "http://localhost:8080"

    // Set to true to render fake tickets without a running backend.
    const val USE_FAKE_DATA = false

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val fakeTickets = listOf(
        TicketResponse(1, "Sam", "Heating", "214", "Heater not working", "HIGH", "OPEN", "2026-10-20T14:30:00Z"),
        TicketResponse(2, "Alex", "Plumbing", "101", "Sink leaking badly", "MEDIUM", "IN_PROGRESS", "2026-10-21T09:00:00Z"),
        TicketResponse(3, "Jordan", "Electrical", "305", "Light flickering", "LOW", "OPEN", "2026-10-22T16:45:00Z"),
    )

    private val fakeCategories = listOf(
        Category(1, "Heating"),
        Category(2, "Plumbing"),
        Category(3, "Electrical"),
    )

    suspend fun hello(): HelloResponse = client.get("$BASE_URL/api/hello").body()

    suspend fun getTickets(): List<TicketResponse> =
        if (USE_FAKE_DATA) fakeTickets
        else client.get("$BASE_URL/api/tickets").body()

    suspend fun getCategories(): List<Category> =
        if (USE_FAKE_DATA) fakeCategories
        else client.get("$BASE_URL/api/categories").body()
}

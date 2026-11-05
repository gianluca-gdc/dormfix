package com.dormfix.web.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class HelloResponse(val message: String, val serverTime: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val userId: Int,
    val name: String,
    val role: String,
    val roomNumber: String? = null,
)

@Serializable
data class ApiError(val error: String)

@Serializable
data class TicketResponse(
    val id: Int,
    val residentId: Int,
    val residentName: String,
    val category: String,
    val roomNumber: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: String,
)

@Serializable
data class Category(val id: Int, val name: String)

@Serializable
data class CreateTicketRequest(
    val residentId: Int,
    val categoryId: Int,
    val roomNumber: String,
    val description: String,
    val priority: String,
)

@Serializable
data class PatchStatusRequest(
    val newStatus: String,
    val actorId: Int,
    val note: String,
)

@Serializable
data class StatusHistoryEntry(
    val oldStatus: String,
    val newStatus: String,
    val note: String,
    val updatedBy: String,
    val createdAt: String,
)

/** All calls to the Spring Boot backend go through here. */
object ApiClient {
    private const val BASE_URL = "http://localhost:8080"

    // Set to true to use hardcoded responses without a running backend.
    const val USE_FAKE_DATA = false

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (status.value in 200..299) return body()
        val err = runCatching { body<ApiError>() }.getOrNull()
        throw Exception(err?.error ?: "Error ${status.value}")
    }

    // ── fake data ────────────────────────────────────────────────────────────

    private val fakeResidentUser = LoginResponse(1, "Sam", "RESIDENT", "214")
    private val fakeStaffUser    = LoginResponse(2, "Dana", "STAFF", null)

    private val fakeTickets = listOf(
        TicketResponse(1, 1, "Sam", "Plumbing",   "214", "The bathroom sink is leaking.",  "HIGH",   "OPEN",        "2026-10-20T14:30:00Z"),
        TicketResponse(2, 1, "Sam", "Electrical", "214", "The desk lamp outlet is loose.", "MEDIUM", "IN_PROGRESS", "2026-10-21T09:00:00Z"),
        TicketResponse(3, 1, "Sam", "Heating",    "214", "The radiator is working again.", "LOW",    "RESOLVED",    "2026-10-22T16:45:00Z"),
    )

    private val fakeCategories = listOf(
        Category(1, "Plumbing"),
        Category(2, "Electrical"),
        Category(3, "Heating"),
        Category(4, "Furniture"),
        Category(5, "Other"),
    )

    private val fakeHistory = listOf(
        StatusHistoryEntry("OPEN", "IN_PROGRESS", "Started work on the outlet.", "Dana", "2026-10-21T10:00:00Z"),
    )

    // ── auth ─────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): LoginResponse {
        if (USE_FAKE_DATA) {
            return if ("staff" in email.lowercase()) fakeStaffUser else fakeResidentUser
        }
        return client.post("$BASE_URL/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.bodyOrThrow()
    }

    // ── misc ─────────────────────────────────────────────────────────────────

    suspend fun hello(): HelloResponse = client.get("$BASE_URL/api/hello").body()

    suspend fun getCategories(): List<Category> {
        if (USE_FAKE_DATA) return fakeCategories
        return client.get("$BASE_URL/api/categories").bodyOrThrow()
    }

    // ── tickets ───────────────────────────────────────────────────────────────

    suspend fun getTickets(): List<TicketResponse> {
        if (USE_FAKE_DATA) return fakeTickets
        return client.get("$BASE_URL/api/tickets").bodyOrThrow()
    }

    suspend fun getResidentTickets(residentId: Int): List<TicketResponse> {
        if (USE_FAKE_DATA) return fakeTickets.filter { it.residentId == residentId }
        return client.get("$BASE_URL/api/tickets?residentId=$residentId").bodyOrThrow()
    }

    suspend fun getTicket(id: Int): TicketResponse {
        if (USE_FAKE_DATA) return fakeTickets.firstOrNull { it.id == id }
            ?: throw Exception("Request not found.")
        return client.get("$BASE_URL/api/tickets/$id").bodyOrThrow()
    }

    suspend fun createTicket(request: CreateTicketRequest): TicketResponse {
        if (USE_FAKE_DATA) return TicketResponse(
            id = 99,
            residentId = request.residentId,
            residentName = fakeResidentUser.name,
            category = fakeCategories.firstOrNull { it.id == request.categoryId }?.name ?: "Other",
            roomNumber = request.roomNumber,
            description = request.description,
            priority = request.priority,
            status = "OPEN",
            createdAt = "2026-11-04T10:00:00Z",
        )
        return client.post("$BASE_URL/api/tickets") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyOrThrow()
    }

    suspend fun patchStatus(ticketId: Int, request: PatchStatusRequest): TicketResponse {
        if (USE_FAKE_DATA) {
            val t = fakeTickets.firstOrNull { it.id == ticketId }
                ?: throw Exception("Request not found.")
            return t.copy(status = request.newStatus)
        }
        return client.patch("$BASE_URL/api/tickets/$ticketId/status") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyOrThrow()
    }

    suspend fun getTicketHistory(ticketId: Int): List<StatusHistoryEntry> {
        if (USE_FAKE_DATA) return if (ticketId == 2) fakeHistory else emptyList()
        return client.get("$BASE_URL/api/tickets/$ticketId/history").bodyOrThrow()
    }
}

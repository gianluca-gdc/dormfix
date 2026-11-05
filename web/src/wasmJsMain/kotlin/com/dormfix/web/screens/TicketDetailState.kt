package com.dormfix.web.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dormfix.web.api.ApiClient
import com.dormfix.web.api.LoginResponse
import com.dormfix.web.api.PatchStatusRequest
import com.dormfix.web.api.StatusHistoryEntry
import com.dormfix.web.api.TicketResponse

sealed interface DetailLoadState {
    data object Loading : DetailLoadState
    data class Success(val ticket: TicketResponse) : DetailLoadState
    data class Error(val message: String) : DetailLoadState
}

sealed interface PatchState {
    data object Idle : PatchState
    data object Loading : PatchState
    data class Error(val message: String) : PatchState
}

class TicketDetailState(private val ticketId: Int, private val user: LoginResponse) {
    var detailState: DetailLoadState by mutableStateOf(DetailLoadState.Loading)
    var history: List<StatusHistoryEntry> by mutableStateOf(emptyList())
    var note: String by mutableStateOf("")
    var patchState: PatchState by mutableStateOf(PatchState.Idle)

    fun nextStatuses(currentStatus: String): List<String> = when (currentStatus) {
        "OPEN"        -> listOf("IN_PROGRESS")
        "IN_PROGRESS" -> listOf("RESOLVED")
        else          -> emptyList()
    }

    suspend fun load() {
        detailState = DetailLoadState.Loading
        detailState = try {
            DetailLoadState.Success(ApiClient.getTicket(ticketId))
        } catch (e: Exception) {
            DetailLoadState.Error(e.message ?: "Unknown error")
        }
        history = try { ApiClient.getTicketHistory(ticketId) } catch (e: Exception) { emptyList() }
    }

    suspend fun applyStatus(newStatus: String) {
        patchState = PatchState.Loading
        try {
            val updated = ApiClient.patchStatus(
                ticketId,
                PatchStatusRequest(newStatus = newStatus, actorId = user.userId, note = note),
            )
            detailState = DetailLoadState.Success(updated)
            note = ""
            patchState = PatchState.Idle
            history = try { ApiClient.getTicketHistory(ticketId) } catch (e: Exception) { history }
        } catch (e: Exception) {
            patchState = PatchState.Error(e.message ?: "Could not update status")
        }
    }
}

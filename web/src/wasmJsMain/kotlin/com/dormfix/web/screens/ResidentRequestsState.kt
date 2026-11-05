package com.dormfix.web.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dormfix.web.api.ApiClient

class ResidentRequestsState(private val residentId: Int) {
    var ticketsState: TicketsLoadState by mutableStateOf(TicketsLoadState.Loading)

    suspend fun load() {
        ticketsState = TicketsLoadState.Loading
        ticketsState = try {
            TicketsLoadState.Success(ApiClient.getResidentTickets(residentId))
        } catch (e: Exception) {
            TicketsLoadState.Error(e.message ?: "Unknown error")
        }
    }
}

package com.dormfix.web.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dormfix.web.api.ApiClient
import com.dormfix.web.api.Category
import com.dormfix.web.api.TicketResponse

sealed interface TicketsLoadState {
    data object Loading : TicketsLoadState
    data class Success(val tickets: List<TicketResponse>) : TicketsLoadState
    data class Error(val message: String) : TicketsLoadState
}

class AllRequestsState {
    var ticketsState: TicketsLoadState by mutableStateOf(TicketsLoadState.Loading)
    var categories: List<Category> by mutableStateOf(emptyList())
    var filterStatus: String by mutableStateOf("")
    var filterCategory: String by mutableStateOf("")
    var filterPriority: String by mutableStateOf("")

    fun filteredTickets(): List<TicketResponse> {
        val tickets = (ticketsState as? TicketsLoadState.Success)?.tickets ?: return emptyList()
        return filterTickets(tickets, filterStatus, filterCategory, filterPriority)
    }

    suspend fun load() {
        ticketsState = TicketsLoadState.Loading
        ticketsState = try {
            TicketsLoadState.Success(ApiClient.getTickets())
        } catch (e: Exception) {
            TicketsLoadState.Error(e.message ?: "Unknown error")
        }
        categories = try {
            ApiClient.getCategories()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun filterTickets(
    tickets: List<TicketResponse>,
    status: String,
    category: String,
    priority: String,
): List<TicketResponse> = tickets.filter { ticket ->
    (status.isEmpty() || ticket.status == status) &&
    (category.isEmpty() || ticket.category == category) &&
    (priority.isEmpty() || ticket.priority == priority)
}

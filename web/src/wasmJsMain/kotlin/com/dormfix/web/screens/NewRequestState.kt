package com.dormfix.web.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dormfix.web.api.ApiClient
import com.dormfix.web.api.Category
import com.dormfix.web.api.CreateTicketRequest
import com.dormfix.web.api.LoginResponse
import com.dormfix.web.api.TicketResponse

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data class Error(val message: String) : SubmitState
}

class NewRequestState(private val user: LoginResponse) {
    var categories: List<Category> by mutableStateOf(emptyList())
    var selectedCategory: Category? by mutableStateOf(null)
    var roomNumber: String by mutableStateOf(user.roomNumber ?: "")
    var priority: String by mutableStateOf("MEDIUM")
    var description: String by mutableStateOf("")
    var descriptionError: String? by mutableStateOf(null)
    var submitState: SubmitState by mutableStateOf(SubmitState.Idle)

    suspend fun loadCategories() {
        categories = try { ApiClient.getCategories() } catch (e: Exception) { emptyList() }
        if (selectedCategory == null) selectedCategory = categories.firstOrNull()
    }

    fun validate(): Boolean {
        descriptionError = if (description.isBlank()) "Description is required." else null
        return descriptionError == null
    }

    suspend fun submit(): TicketResponse? {
        if (!validate()) return null
        val cat = selectedCategory ?: return null
        submitState = SubmitState.Loading
        return try {
            val ticket = ApiClient.createTicket(
                CreateTicketRequest(
                    residentId = user.userId,
                    categoryId = cat.id,
                    roomNumber = roomNumber,
                    description = description,
                    priority = priority,
                )
            )
            submitState = SubmitState.Idle
            ticket
        } catch (e: Exception) {
            submitState = SubmitState.Error(e.message ?: "Could not submit request")
            null
        }
    }
}

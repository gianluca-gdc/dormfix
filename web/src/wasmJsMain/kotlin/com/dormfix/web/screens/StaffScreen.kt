package com.dormfix.web.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dormfix.web.api.ApiClient

private sealed interface TicketsState {
    data object Loading : TicketsState
    data class Success(val count: Int) : TicketsState
    data class Error(val message: String) : TicketsState
}

/** Staff view — loads the ticket list from the backend. Phase 2 shows count / empty state. */
@Composable
fun StaffScreen(onBack: () -> Unit) {
    var state by remember { mutableStateOf<TicketsState>(TicketsState.Loading) }

    LaunchedEffect(Unit) {
        state = try {
            val tickets = ApiClient.getTickets()
            TicketsState.Success(tickets.size)
        } catch (e: Exception) {
            TicketsState.Error(e.message ?: "Unknown error")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("All Requests", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            TicketsState.Loading -> CircularProgressIndicator()
            is TicketsState.Success -> {
                if (s.count == 0) {
                    Text("No requests yet", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text("${s.count} ticket(s) found", style = MaterialTheme.typography.bodyLarge)
                }
            }
            is TicketsState.Error -> {
                Text(
                    "Could not load tickets. Is the backend running?",
                    color = MaterialTheme.colorScheme.error,
                )
                Text(s.message, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onBack) {
            Text("Back to Home")
        }
    }
}

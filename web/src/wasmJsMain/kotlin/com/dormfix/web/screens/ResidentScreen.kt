package com.dormfix.web.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dormfix.web.api.LoginResponse
import com.dormfix.web.api.TicketResponse

@Composable
fun ResidentScreen(
    user: LoginResponse,
    onNewRequest: () -> Unit,
    onTicketClick: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    val state = remember { ResidentRequestsState(user.userId) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) { state.load() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("My Requests", style = MaterialTheme.typography.headlineMedium)
                Text("Hi, ${user.name}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onNewRequest) { Text("+ New request") }
                TextButton(onClick = onLogout) { Text("Log out") }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val s = state.ticketsState) {
            TicketsLoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is TicketsLoadState.Error -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Could not load requests.", color = MaterialTheme.colorScheme.error)
                Text(s.message, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { refreshTrigger++ }) { Text("Retry") }
            }
            is TicketsLoadState.Success -> {
                if (s.tickets.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No requests yet", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.tickets, key = { it.id }) { ticket ->
                            ResidentTicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResidentTicketCard(ticket: TicketResponse, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Room ${ticket.roomNumber}", style = MaterialTheme.typography.titleSmall)
                    Text("·", style = MaterialTheme.typography.bodyMedium)
                    Text(ticket.category, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    ticket.priority,
                    style = MaterialTheme.typography.labelSmall,
                    color = residentPriorityColor(ticket.priority),
                )
            }
            ResidentStatusChip(ticket.status)
        }
    }
}

@Composable
private fun ResidentStatusChip(status: String) {
    val containerColor = when (status) {
        "OPEN"        -> MaterialTheme.colorScheme.primaryContainer
        "IN_PROGRESS" -> MaterialTheme.colorScheme.secondaryContainer
        else          -> MaterialTheme.colorScheme.surfaceVariant
    }
    val labelColor = when (status) {
        "OPEN"        -> MaterialTheme.colorScheme.onPrimaryContainer
        "IN_PROGRESS" -> MaterialTheme.colorScheme.onSecondaryContainer
        else          -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(50), color = containerColor) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
        )
    }
}

@Composable
private fun residentPriorityColor(priority: String): Color = when (priority) {
    "HIGH"   -> MaterialTheme.colorScheme.error
    "MEDIUM" -> MaterialTheme.colorScheme.primary
    else     -> MaterialTheme.colorScheme.onSurfaceVariant
}

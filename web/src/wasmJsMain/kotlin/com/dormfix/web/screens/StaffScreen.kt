package com.dormfix.web.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.dormfix.web.api.Category
import com.dormfix.web.api.TicketResponse

@Composable
fun StaffScreen(onBack: () -> Unit) {
    val state = remember { AllRequestsState() }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        state.load()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("All Requests", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(16.dp))

        FilterRow(
            categories = state.categories,
            filterStatus = state.filterStatus,
            filterCategory = state.filterCategory,
            filterPriority = state.filterPriority,
            onStatusChange = { state.filterStatus = it },
            onCategoryChange = { state.filterCategory = it },
            onPriorityChange = { state.filterPriority = it },
        )

        Spacer(Modifier.height(16.dp))

        when (val s = state.ticketsState) {
            TicketsLoadState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TicketsLoadState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Could not load tickets.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(s.message, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { refreshTrigger++ }) { Text("Retry") }
                }
            }
            is TicketsLoadState.Success -> {
                val filtered = state.filteredTickets()
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No requests yet", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.id }) { ticket ->
                            TicketCard(ticket)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    categories: List<Category>,
    filterStatus: String,
    filterCategory: String,
    filterPriority: String,
    onStatusChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterDropdown(
            label = "Status",
            options = listOf("OPEN", "IN_PROGRESS", "CLOSED"),
            selected = filterStatus,
            onSelect = onStatusChange,
            modifier = Modifier.weight(1f),
        )
        FilterDropdown(
            label = "Category",
            options = categories.map { it.name },
            selected = filterCategory,
            onSelect = onCategoryChange,
            modifier = Modifier.weight(1f),
        )
        FilterDropdown(
            label = "Priority",
            options = listOf("HIGH", "MEDIUM", "LOW"),
            selected = filterPriority,
            onSelect = onPriorityChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (selected.isEmpty()) label else selected)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All $label") },
                onClick = { onSelect(""); expanded = false },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: TicketResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                Spacer(Modifier.height(2.dp))
                Text(
                    ticket.residentName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    ticket.priority,
                    style = MaterialTheme.typography.labelSmall,
                    color = priorityColor(ticket.priority),
                )
            }
            StatusChip(ticket.status)
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val containerColor = when (status) {
        "OPEN" -> MaterialTheme.colorScheme.primaryContainer
        "IN_PROGRESS" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val labelColor = when (status) {
        "OPEN" -> MaterialTheme.colorScheme.onPrimaryContainer
        "IN_PROGRESS" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
private fun priorityColor(priority: String): Color = when (priority) {
    "HIGH" -> MaterialTheme.colorScheme.error
    "MEDIUM" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dormfix.web.api.LoginResponse
import com.dormfix.web.api.StatusHistoryEntry
import com.dormfix.web.api.TicketResponse
import kotlinx.coroutines.launch

@Composable
fun TicketDetailScreen(
    ticketId: Int,
    user: LoginResponse,
    onBack: () -> Unit,
) {
    val state = remember { TicketDetailState(ticketId, user) }
    val scope = rememberCoroutineScope()
    var selectedStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { state.load() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }

        Spacer(Modifier.height(8.dp))

        when (val ds = state.detailState) {
            DetailLoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is DetailLoadState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(ds.message, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { scope.launch { state.load() } }) { Text("Retry") }
                }
            }
            is DetailLoadState.Success -> {
                val ticket = ds.ticket

                LaunchedEffect(ticket.status) {
                    selectedStatus = state.nextStatuses(ticket.status).firstOrNull() ?: ""
                }

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DetailHeader(ticket)

                    Spacer(Modifier.height(16.dp))

                    Text("Description", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(ticket.description, style = MaterialTheme.typography.bodyMedium)

                    if (state.history.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Text("History", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        state.history.forEach { entry ->
                            HistoryRow(entry)
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    if (state.patchState is PatchState.Error) {
                        Text(
                            (state.patchState as PatchState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    when (user.role) {
                        "STAFF" -> {
                            val nextOptions = state.nextStatuses(ticket.status)
                            if (nextOptions.isNotEmpty()) {
                                StaffActions(
                                    nextOptions = nextOptions,
                                    selectedStatus = selectedStatus,
                                    onStatusSelect = { selectedStatus = it },
                                    note = state.note,
                                    onNoteChange = { state.note = it },
                                    isLoading = state.patchState is PatchState.Loading,
                                    onSave = { scope.launch { state.applyStatus(selectedStatus) } },
                                )
                            } else {
                                Text(
                                    "No further actions available.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        "RESIDENT" -> {
                            if (ticket.residentId == user.userId && ticket.status == "RESOLVED") {
                                Button(
                                    onClick = { scope.launch { state.applyStatus("OPEN") } },
                                    enabled = state.patchState !is PatchState.Loading,
                                ) { Text("Reopen") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(ticket: TicketResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Room ${ticket.roomNumber} · ${ticket.category}",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                ticket.residentName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${ticket.priority} priority",
                style = MaterialTheme.typography.labelSmall,
                color = detailPriorityColor(ticket.priority),
            )
        }
        DetailStatusChip(ticket.status)
    }
}

@Composable
private fun StaffActions(
    nextOptions: List<String>,
    selectedStatus: String,
    onStatusSelect: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    isLoading: Boolean,
    onSave: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Text("Update status", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))

    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedStatus.isEmpty()) "Select status" else selectedStatus)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            nextOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onStatusSelect(option); expanded = false },
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Note (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = onSave,
        enabled = !isLoading && selectedStatus.isNotEmpty(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isLoading) CircularProgressIndicator() else Text("Save")
    }
}

@Composable
private fun HistoryRow(entry: StatusHistoryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${entry.oldStatus} → ${entry.newStatus}",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    entry.updatedBy,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.note.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(entry.note, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                entry.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailStatusChip(status: String) {
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
private fun detailPriorityColor(priority: String): Color = when (priority) {
    "HIGH"   -> MaterialTheme.colorScheme.error
    "MEDIUM" -> MaterialTheme.colorScheme.primary
    else     -> MaterialTheme.colorScheme.onSurfaceVariant
}

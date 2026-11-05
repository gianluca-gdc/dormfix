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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.dormfix.web.api.LoginResponse
import com.dormfix.web.api.TicketResponse
import kotlinx.coroutines.launch

@Composable
fun NewRequestScreen(
    user: LoginResponse,
    onBack: () -> Unit,
    onSuccess: (TicketResponse) -> Unit,
) {
    val state = remember { NewRequestState(user) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { state.loadCategories() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("New Request", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onBack) { Text("Cancel") }
        }

        Spacer(Modifier.height(24.dp))

        Text("Category", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        FormDropdown(
            label = "Select category",
            options = state.categories.map { it.name },
            selected = state.selectedCategory?.name ?: "",
            onSelect = { name -> state.selectedCategory = state.categories.firstOrNull { it.name == name } },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Text("Room number", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = state.roomNumber,
            onValueChange = { state.roomNumber = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(Modifier.height(16.dp))

        Text("Priority", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        FormDropdown(
            label = "Select priority",
            options = listOf("LOW", "MEDIUM", "HIGH"),
            selected = state.priority,
            onSelect = { state.priority = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Text("Description", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = { state.description = it; state.descriptionError = null },
            isError = state.descriptionError != null,
            supportingText = if (state.descriptionError != null) {
                { Text(state.descriptionError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )

        Spacer(Modifier.height(16.dp))

        if (state.submitState is SubmitState.Error) {
            Text(
                (state.submitState as SubmitState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    val ticket = state.submit()
                    if (ticket != null) onSuccess(ticket)
                }
            },
            enabled = state.submitState !is SubmitState.Loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.submitState is SubmitState.Loading) {
                CircularProgressIndicator()
            } else {
                Text("Submit request")
            }
        }
    }
}

@Composable
private fun FormDropdown(
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
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

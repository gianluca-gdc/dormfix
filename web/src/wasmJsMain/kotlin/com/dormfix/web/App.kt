package com.dormfix.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dormfix.web.api.ApiClient
import com.dormfix.web.api.HelloResponse
import com.dormfix.web.screens.HomeScreen
import com.dormfix.web.screens.ResidentScreen
import com.dormfix.web.screens.StaffScreen

private enum class Screen { HOME, RESIDENT, STAFF }

private sealed interface HelloState {
    data object Loading : HelloState
    data class Success(val response: HelloResponse) : HelloState
    data class Error(val message: String) : HelloState
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    onResidentClick = { currentScreen = Screen.RESIDENT },
                    onStaffClick = { currentScreen = Screen.STAFF },
                    helloCard = { HelloCard() },
                )
                Screen.RESIDENT -> ResidentScreen(
                    onBack = { currentScreen = Screen.HOME },
                )
                Screen.STAFF -> StaffScreen(
                    onBack = { currentScreen = Screen.HOME },
                )
            }
        }
    }
}

@Composable
fun HelloCard() {
    var state by remember { mutableStateOf<HelloState>(HelloState.Loading) }
    var refreshCount by remember { mutableIntStateOf(0) }

    // Calls the backend when the page loads, and again whenever "Try again" is clicked
    LaunchedEffect(refreshCount) {
        state = HelloState.Loading
        state = try {
            HelloState.Success(ApiClient.hello())
        } catch (e: Exception) {
            HelloState.Error(e.message ?: "Unknown error")
        }
    }

    Card(modifier = Modifier.widthIn(max = 420.dp).padding(16.dp)) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Backend check", style = MaterialTheme.typography.titleMedium)

            when (val s = state) {
                HelloState.Loading -> CircularProgressIndicator()
                is HelloState.Success -> {
                    Text(s.response.message, style = MaterialTheme.typography.bodyMedium)
                    Text("Server time: ${s.response.serverTime}", style = MaterialTheme.typography.bodySmall)
                }
                is HelloState.Error -> {
                    Text(
                        "Couldn't reach the backend. Is it running on port 8080?",
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(s.message, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(onClick = { refreshCount++ }) {
                Text("Try again")
            }
        }
    }
}

package com.dormfix.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dormfix.web.api.LoginResponse
import com.dormfix.web.api.TicketResponse
import com.dormfix.web.screens.LoginScreen
import com.dormfix.web.screens.NewRequestScreen
import com.dormfix.web.screens.ResidentScreen
import com.dormfix.web.screens.StaffScreen
import com.dormfix.web.screens.TicketDetailScreen

private sealed interface AppScreen {
    data object Login : AppScreen
    data class ResidentRequests(val user: LoginResponse) : AppScreen
    data class StaffRequests(val user: LoginResponse) : AppScreen
    data class NewRequest(val user: LoginResponse) : AppScreen
    data class TicketDetail(val ticketId: Int, val user: LoginResponse, val back: AppScreen) : AppScreen
}

@Composable
fun App() {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val s = screen) {
                AppScreen.Login -> LoginScreen(
                    onLoginSuccess = { user ->
                        screen = if (user.role == "STAFF") AppScreen.StaffRequests(user)
                                 else AppScreen.ResidentRequests(user)
                    },
                )
                is AppScreen.ResidentRequests -> ResidentScreen(
                    user = s.user,
                    onNewRequest = { screen = AppScreen.NewRequest(s.user) },
                    onTicketClick = { id -> screen = AppScreen.TicketDetail(id, s.user, s) },
                    onLogout = { screen = AppScreen.Login },
                )
                is AppScreen.StaffRequests -> StaffScreen(
                    user = s.user,
                    onTicketClick = { id -> screen = AppScreen.TicketDetail(id, s.user, s) },
                    onLogout = { screen = AppScreen.Login },
                )
                is AppScreen.NewRequest -> NewRequestScreen(
                    user = s.user,
                    onBack = { screen = AppScreen.ResidentRequests(s.user) },
                    onSuccess = { ticket: TicketResponse ->
                        screen = AppScreen.TicketDetail(ticket.id, s.user, AppScreen.ResidentRequests(s.user))
                    },
                )
                is AppScreen.TicketDetail -> TicketDetailScreen(
                    ticketId = s.ticketId,
                    user = s.user,
                    onBack = { screen = s.back },
                )
            }
        }
    }
}

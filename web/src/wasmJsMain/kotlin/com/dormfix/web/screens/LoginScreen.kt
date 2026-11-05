package com.dormfix.web.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dormfix.web.api.LoginResponse
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (LoginResponse) -> Unit) {
    val state = remember { LoginState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("DormFix", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text("Sign in to continue", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth()) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { state.email = it; state.emailError = null },
                label = { Text("Email") },
                isError = state.emailError != null,
                supportingText = if (state.emailError != null) {
                    { Text(state.emailError!!) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { state.password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(8.dp))

            if (state.loadState is LoginLoadState.Error) {
                Text(
                    (state.loadState as LoginLoadState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    scope.launch {
                        val user = state.login()
                        if (user != null) onLoginSuccess(user)
                    }
                },
                enabled = state.loadState !is LoginLoadState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.loadState is LoginLoadState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Log in")
                }
            }
        }
    }
}

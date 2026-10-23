package com.dormfix.web.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Landing page. The user picks their role here to navigate to the right screen.
 * The HelloCard is kept here so we can always verify the backend is reachable.
 */
@Composable
fun HomeScreen(
    onResidentClick: () -> Unit,
    onStaffClick: () -> Unit,
    helloCard: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("DormFix", style = MaterialTheme.typography.displaySmall)
        Text("Who are you?", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(24.dp))

        Button(onClick = onResidentClick, modifier = Modifier.widthIn(min = 200.dp)) {
            Text("I am a Resident")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onStaffClick, modifier = Modifier.widthIn(min = 200.dp)) {
            Text("I am Staff")
        }

        Spacer(Modifier.height(32.dp))

        helloCard()
    }
}

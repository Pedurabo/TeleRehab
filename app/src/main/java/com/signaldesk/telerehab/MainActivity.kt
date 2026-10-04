package com.signaldesk.telerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.ui.theme.TeleRehabTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appClock: AppClock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startedAt = appClock.now()

        enableEdgeToEdge()

        setContent {
            TeleRehabTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    FoundationScreen(
                        startedAt = startedAt.toString(),
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun FoundationScreen(
    startedAt: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "TeleRehab")
        Text(text = "Hilt injection ready")
        Text(text = "Started: $startedAt")
    }
}

@Preview(showBackground = true)
@Composable
private fun FoundationPreview() {
    TeleRehabTheme {
        FoundationScreen(
            startedAt = "2026-10-04T00:00:00Z",
        )
    }
}

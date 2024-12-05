package org.unifiedpush.example.activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.unifiedpush.example.Urgency
import org.unifiedpush.example.activities.AppBarViewModel
import org.unifiedpush.example.activities.CheckViewModel
import org.unifiedpush.example.activities.Events

@Composable
fun CheckUi(appBarViewModel: AppBarViewModel, viewModel: CheckViewModel) {
    Scaffold(
        topBar = { AppBarUi(appBarViewModel) }
    ) { innerPadding ->
        CheckUiContent(innerPadding, viewModel)
    }
}

@Composable
fun CheckUiContent(innerPadding: PaddingValues, viewModel: CheckViewModel) {
    var showUrgencyDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                16.dp,
                innerPadding.calculateTopPadding() + 16.dp,
                16.dp,
                innerPadding.calculateBottomPadding() + 16.dp
            ),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                style = MaterialTheme.typography.labelMedium,
                text = "Endpoint"
            )
            SelectionContainer { Text(viewModel.uiState.endpoint) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                style = MaterialTheme.typography.labelMedium,
                text = "Auth"
            )
            SelectionContainer { Text(viewModel.uiState.auth) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                style = MaterialTheme.typography.labelMedium,
                text = "P256dh"
            )
            SelectionContainer { Text(viewModel.uiState.p256dh) }
        }

        if (viewModel.uiState.showVapid) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    style = MaterialTheme.typography.labelMedium,
                    text = "VAPID"
                )
                SelectionContainer { Text(viewModel.uiState.vapid) }
            }
        }

        HorizontalDivider(thickness = 1.dp)

        viewModel.uiState.error?.let {
            Text(it)
        }

        val buttonsList = mutableListOf<@Composable () -> Unit>(
            {
                Button(
                    onClick = {
                        Events.emit(Events.Type.Unregister)
                    }
                ) {
                    Text("Unregister")
                }
            },
            {
                Button(
                    onClick = {
                        Events.emit(Events.Type.SendNotification)
                    }
                ) {
                    Text("Send notification")
                }
            }
        )
        if (viewModel.uiState.devMode) {
            buttonsList.addAll(
                listOf(
                    {
                        Button(
                            onClick =
                            {
                                Events.emit(Events.Type.Reregister)
                            }
                        ) {
                            Text("Reregister")
                        }
                    },
                    {
                        Button(
                            enabled = viewModel.uiState.hasForegroundService,
                            onClick = {
                                Events.emit(Events.Type.StopForegroundService)
                            }
                        ) {
                            Text("Stop Foreground Service")
                        }
                    },
                    {
                        Button(
                            onClick = {
                                Events.emit(Events.Type.DeepLink)
                            }
                        ) {
                            Text("Deep link")
                        }
                    },
                    {
                        Button(
                            onClick = {
                                Events.emit(Events.Type.ChangeDistributor)
                            }
                        ) {
                            Text("Change distributor")
                        }
                    },
                    {
                        Button(
                            enabled = !viewModel.uiState.sendCleartext,
                            onClick = {
                                showUrgencyDialog = true
                            }
                        ) {
                            Text("Set urgency")
                        }
                    },
                    {
                        Button(
                            onClick = {
                                Events.emit(Events.Type.UpdateVapidKey)
                            }
                        ) {
                            Text("Update VAPID key")
                        }
                    },
                    {
                        Button(
                            enabled = !viewModel.uiState.sendCleartext,
                            onClick = {
                                Events.emit(Events.Type.TestTopic)
                            }
                        ) {
                            Text("Test topic")
                        }
                    },
                    {
                        Button(
                            enabled = !viewModel.uiState.sendCleartext,
                            onClick = {
                                Events.emit(Events.Type.TestTTL)
                            }
                        ) {
                            Text("Test TTL")
                        }
                    },
                    {
                        Button(
                            onClick = {
                                Events.emit(Events.Type.TestInBackground)
                            }
                        ) {
                            Text("Test in background")
                        }
                    }
                )
            )
        }

        TwoColumns(buttonsList)

        if (showUrgencyDialog) {
            SetUrgencyDialog(
                urgency = viewModel.uiState.urgency,
                onDismissRequest = { showUrgencyDialog = false },
                onConfirmation = {
                    viewModel.setUrgency(it)
                    showUrgencyDialog = false
                }
            )
        }
    }
}

@Composable
fun TwoColumns(list: List<@Composable () -> Unit>) {
    list.chunked(2).forEach { item ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            item[0]()
            if (item.size > 1) {
                item[1]()
            }
        }
    }
}

@Preview
@Composable
fun CheckUiPreview() {
    CheckUiContent(
        PaddingValues(0.dp),
        CheckViewModel(
            CheckUiState(
                error = "error!",
                devMode = true,
                hasForegroundService = false,
                sendCleartext = true,
                endpoint = "https://my.endpoint.tld",
                auth = "Auth_random",
                p256dh = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                showVapid = true,
                vapid = "vapid t=eyAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                urgency = Urgency.NORMAL
            )
        )
    )
}

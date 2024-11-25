package org.unifiedpush.example.activities.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.unifiedpush.example.R
import org.unifiedpush.example.activities.AppBarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarUi(viewModel: AppBarViewModel) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults
            .topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ),
        title = {
            Text(
                stringResource(R.string.app_name)
            )
        },
        actions = {
            IconButton(
                onClick = {
                    expanded = !expanded
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Actions"
                )
            }
            Dropdown(
                expanded,
                viewModel
            ) {
                expanded = false
            }
        }
    )
}

@Composable
fun Dropdown(
    expanded: Boolean,
    viewModel: AppBarViewModel,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() }
    ) {
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Developer mode")
                    Spacer(Modifier.weight(1f))
                    Checkbox(
                        viewModel.uiState.devMode,
                        onCheckedChange = { viewModel.toggleDevMode() }
                    )
                }
            },
            onClick = { },
        )
        if (viewModel.uiState.devMode) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Error if decryption fails"
                        )
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            viewModel.uiState.errorIfDecryptionFail,
                            onCheckedChange = { viewModel.toggleErrorIfDecryptionFail() }
                        )
                    }
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Use VAPID"
                        )
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            viewModel.uiState.useVapid,
                            onCheckedChange = { viewModel.toggleUseVapid() }
                        )
                    }
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Send cleartext test"
                        )
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            viewModel.uiState.sendClearTextTests,
                            onCheckedChange = { viewModel.toggleSendClearTextTests() }
                        )
                    }
                },
                onClick = {}
            )
            DropdownMenuItem(
                enabled = viewModel.uiState.useVapid,
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Use wrong VAPID keys"
                        )
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            viewModel.uiState.useVapid
                                    && viewModel.uiState.useWrongVapidKeys,
                            onCheckedChange = { viewModel.toggleUseWrongVapidKeys() }
                        )
                    }
                },
                onClick = {}
            )
            DropdownMenuItem(
                enabled = !viewModel.uiState.sendClearTextTests,
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Use wrong encryption keys"
                        )
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            !viewModel.uiState.sendClearTextTests
                                    && viewModel.uiState.useWrongEncryptionKeys,
                            onCheckedChange = { viewModel.toggleUseWrongEncryptionKeys() }
                        )
                    }
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Foreground service on message"
                        )
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            viewModel.uiState.startForegroundServiceOnMessage,
                            onCheckedChange = { viewModel.toggleStartForegroundServiceOnMessage() }
                        )
                    }
                },
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
fun AppBarPreview() {
    AppBarUi(
        AppBarViewModel(
        uiState = AppBarUiState(
            devMode = true,
            errorIfDecryptionFail = true,
            useVapid = true,
            sendClearTextTests = true,
            useWrongVapidKeys = true,
            useWrongEncryptionKeys = true,
            startForegroundServiceOnMessage = true
        )
    )
    )
}

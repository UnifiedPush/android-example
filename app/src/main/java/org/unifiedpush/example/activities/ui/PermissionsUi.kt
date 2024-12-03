package org.unifiedpush.example.activities.ui

import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun PermissionsUi(onDone: () -> Unit = {}) {
    if (Build.VERSION.SDK_INT < 33) {
        onDone()
    }
    val notificationsPermissionState =
        rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    if (!notificationsPermissionState.status.isGranted) {
        AlertDialog(
            title = {
                Text("Permissions")
            },
            text = {
                Text("This application requires notifications permission to work.")
            },
            onDismissRequest = {
                onDone()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationsPermissionState.launchPermissionRequest()
                        onDone()
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {}
        )
    }
}

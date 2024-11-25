package org.unifiedpush.example.activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.unifiedpush.example.Urgency

@Preview
@Composable
fun SetUrgencyDialog(urgency: Urgency = Urgency.NORMAL, onDismissRequest: () -> Unit = {}, onConfirmation: (Urgency) -> Unit = {}) {
    AlertDialog(
        title = {
            Text("Urgency")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Urgency.entries.forEach { entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { onConfirmation(entry) }
                        ) {
                            Text(
                                text = entry.value,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Checkbox(
                            entry == urgency,
                            onCheckedChange = { onConfirmation(entry) }
                        )
                    }
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = { },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
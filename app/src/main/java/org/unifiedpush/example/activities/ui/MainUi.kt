package org.unifiedpush.example.activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.unifiedpush.example.R
import org.unifiedpush.example.activities.AppBarViewModel
import org.unifiedpush.example.activities.Events

@Composable
fun MainUi(viewModel: AppBarViewModel) {
    Scaffold(
        topBar = { AppBarUi(viewModel) }
    ) { innerPadding ->
        MainUiContent(innerPadding)
    }
}

@Composable
fun MainUiContent(innerPadding: PaddingValues) {
    var showPermissionDialog by remember { mutableStateOf(true) }

    if (showPermissionDialog) {
        PermissionsUi {
            showPermissionDialog = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                16.dp,
                innerPadding.calculateTopPadding(),
                16.dp,
                innerPadding.calculateBottomPadding()
            ),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.about))
        Button(
            onClick = {
                Events.emit(Events.Type.Register)
            }
        ) { 
            Text("Register")
        }
    }
}
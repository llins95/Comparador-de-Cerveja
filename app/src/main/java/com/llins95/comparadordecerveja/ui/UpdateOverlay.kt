package com.llins95.comparadordecerveja.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun UpdateOverlay(
    viewModel: AppUpdateViewModel,
    manualOpenRequest: Int = 0,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var dismissedVersionCode by rememberSaveable { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        viewModel.checkAutomaticallyOnce()
    }

    LaunchedEffect(manualOpenRequest) {
        if (manualOpenRequest > 0) showDialog = true
    }

    LaunchedEffect(state.status, state.availableUpdate?.versionCode) {
        val available = state.availableUpdate
        if (state.status == AppUpdateStatus.Available &&
            available != null &&
            available.versionCode != dismissedVersionCode
        ) {
            showDialog = true
        }
        if (state.status == AppUpdateStatus.OpeningDownload) {
            showDialog = false
        }
    }

    if (showDialog) {
        AppUpdateDialog(
            state = state,
            onCheck = viewModel::checkForUpdates,
            onDownload = viewModel::openDownload,
            onDismiss = {
                state.availableUpdate?.let { dismissedVersionCode = it.versionCode }
                if (state.status != AppUpdateStatus.Checking) {
                    showDialog = false
                }
            },
        )
    }
}

@Composable
private fun AppUpdateDialog(
    state: AppUpdateUiState,
    onCheck: () -> Unit,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    val available = state.availableUpdate
    val busy = state.status == AppUpdateStatus.Checking

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Atualização do aplicativo") },
        text = {
            Column {
                Text(
                    "Versão instalada: ${state.installedVersionName} (${state.installedVersionCode})",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = when (state.status) {
                        AppUpdateStatus.Idle -> "A Cerva verifica novas versões publicadas no GitHub."
                        AppUpdateStatus.Checking -> "Verificando a versão mais recente…"
                        AppUpdateStatus.UpToDate -> "Você já está usando a versão mais recente."
                        AppUpdateStatus.Available ->
                            "Nova versão disponível: ${available?.versionName} (${available?.versionCode}). O download será aberto no navegador para manter o app sem permissão de instalar pacotes."
                        AppUpdateStatus.OpeningDownload ->
                            "Abrindo o download da atualização no navegador…"
                        AppUpdateStatus.Error ->
                            state.errorMessage ?: "Não foi possível verificar a atualização."
                    },
                    modifier = Modifier.padding(top = 12.dp),
                )
                if (state.status == AppUpdateStatus.Checking) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                if (state.status == AppUpdateStatus.Available &&
                    !available?.releaseNotes.isNullOrBlank()
                ) {
                    Text(
                        "Novidades",
                        modifier = Modifier.padding(top = 16.dp),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        available!!.releaseNotes.take(600),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            when (state.status) {
                AppUpdateStatus.Available -> TextButton(onClick = onDownload) {
                    Text("Baixar no navegador")
                }
                AppUpdateStatus.Checking,
                AppUpdateStatus.OpeningDownload -> Unit
                else -> TextButton(onClick = onCheck) {
                    Text("Verificar atualizações")
                }
            }
        },
        dismissButton = {
            if (!busy && state.status != AppUpdateStatus.OpeningDownload) {
                TextButton(onClick = onDismiss) {
                    Text(if (state.status == AppUpdateStatus.Available) "Depois" else "Fechar")
                }
            }
        },
    )
}

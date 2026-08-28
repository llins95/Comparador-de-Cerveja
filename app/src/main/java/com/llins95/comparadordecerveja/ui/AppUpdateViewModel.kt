package com.llins95.comparadordecerveja.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.llins95.comparadordecerveja.BuildConfig
import com.llins95.comparadordecerveja.update.AppUpdateInfo
import com.llins95.comparadordecerveja.update.AppUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppUpdateStatus {
    Idle,
    Checking,
    UpToDate,
    Available,
    OpeningDownload,
    Error,
}

data class AppUpdateUiState(
    val status: AppUpdateStatus = AppUpdateStatus.Idle,
    val installedVersionName: String = BuildConfig.VERSION_NAME,
    val installedVersionCode: Int = BuildConfig.VERSION_CODE,
    val availableUpdate: AppUpdateInfo? = null,
    val errorMessage: String? = null,
)

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val manager = AppUpdateManager(application.applicationContext)
    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    private var automaticCheckStarted = false

    fun checkAutomaticallyOnce() {
        if (automaticCheckStarted) return
        automaticCheckStarted = true
        checkForUpdates()
    }

    fun checkForUpdates() {
        if (_uiState.value.status == AppUpdateStatus.Checking) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(status = AppUpdateStatus.Checking, errorMessage = null)
            }
            runCatching { manager.checkForUpdate() }
                .onSuccess { update ->
                    _uiState.update {
                        if (update == null) {
                            it.copy(
                                status = AppUpdateStatus.UpToDate,
                                availableUpdate = null,
                                errorMessage = null,
                            )
                        } else {
                            it.copy(
                                status = AppUpdateStatus.Available,
                                availableUpdate = update,
                                errorMessage = null,
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            status = AppUpdateStatus.Error,
                            errorMessage = friendlyMessage(error),
                        )
                    }
                }
        }
    }

    fun openDownload() {
        val update = _uiState.value.availableUpdate ?: return
        _uiState.update {
            it.copy(status = AppUpdateStatus.OpeningDownload, errorMessage = null)
        }
        runCatching { manager.openDownload(update) }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        status = AppUpdateStatus.Error,
                        errorMessage = friendlyMessage(error),
                    )
                }
            }
    }

    private fun friendlyMessage(error: Throwable): String {
        return error.message?.takeIf { it.isNotBlank() }
            ?: "Não foi possível concluir a atualização."
    }
}

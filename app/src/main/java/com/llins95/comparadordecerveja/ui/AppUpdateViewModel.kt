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
    WaitingForInstallPermission,
    Downloading,
    OpeningInstaller,
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
        if (_uiState.value.status == AppUpdateStatus.Checking ||
            _uiState.value.status == AppUpdateStatus.Downloading
        ) return

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

    fun downloadAndInstall() {
        val update = _uiState.value.availableUpdate ?: return
        if (_uiState.value.status == AppUpdateStatus.Downloading) return

        if (!manager.canRequestPackageInstalls()) {
            _uiState.update {
                it.copy(
                    status = AppUpdateStatus.WaitingForInstallPermission,
                    errorMessage = null,
                )
            }
            manager.openInstallPermissionSettings()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(status = AppUpdateStatus.Downloading, errorMessage = null)
            }
            runCatching { manager.downloadAndInstall(update) }
                .onSuccess {
                    _uiState.update {
                        it.copy(status = AppUpdateStatus.OpeningInstaller, errorMessage = null)
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

    fun handleAppResumed() {
        if (_uiState.value.status != AppUpdateStatus.WaitingForInstallPermission) return
        if (manager.canRequestPackageInstalls()) {
            downloadAndInstall()
        }
    }

    private fun friendlyMessage(error: Throwable): String {
        return error.message?.takeIf { it.isNotBlank() }
            ?: "Não foi possível concluir a atualização."
    }
}

package com.llins95.comparadordecerveja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llins95.comparadordecerveja.data.AppDatabase
import com.llins95.comparadordecerveja.data.AppSettingsRepository
import com.llins95.comparadordecerveja.data.BeerRepository
import com.llins95.comparadordecerveja.ui.AppUpdateViewModel
import com.llins95.comparadordecerveja.ui.BeerApp
import com.llins95.comparadordecerveja.ui.BeerViewModel
import com.llins95.comparadordecerveja.ui.SettingsViewModel
import com.llins95.comparadordecerveja.ui.UpdateOverlay
import com.llins95.comparadordecerveja.ui.theme.CervaTheme

class MainActivity : ComponentActivity() {
    private val appUpdateViewModel: AppUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = BeerRepository(database.beerOfferDao())
        val settingsRepository = AppSettingsRepository(applicationContext)

        setContent {
            CervaTheme {
                val beerViewModel: BeerViewModel = viewModel(
                    factory = BeerViewModel.factory(repository)
                )
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(settingsRepository)
                )
                var updateDialogRequest by remember { mutableIntStateOf(0) }
                Box(modifier = Modifier.fillMaxSize()) {
                    BeerApp(
                        viewModel = beerViewModel,
                        settingsViewModel = settingsViewModel,
                        onOpenUpdate = {
                            updateDialogRequest += 1
                            appUpdateViewModel.checkForUpdates()
                        },
                    )
                    UpdateOverlay(
                        viewModel = appUpdateViewModel,
                        manualOpenRequest = updateDialogRequest,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateViewModel.handleAppResumed()
    }
}

package com.llins95.comparadordecerveja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llins95.comparadordecerveja.data.AppDatabase
import com.llins95.comparadordecerveja.data.BeerRepository
import com.llins95.comparadordecerveja.ui.BeerApp
import com.llins95.comparadordecerveja.ui.BeerViewModel
import com.llins95.comparadordecerveja.ui.theme.CervaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = BeerRepository(database.beerOfferDao())

        setContent {
            CervaTheme {
                val beerViewModel: BeerViewModel = viewModel(
                    factory = BeerViewModel.factory(repository)
                )
                BeerApp(viewModel = beerViewModel)
            }
        }
    }
}

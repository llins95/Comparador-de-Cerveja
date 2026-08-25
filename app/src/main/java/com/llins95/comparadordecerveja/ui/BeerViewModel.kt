package com.llins95.comparadordecerveja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.llins95.comparadordecerveja.data.BeerOfferEntity
import com.llins95.comparadordecerveja.data.BeerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BeerViewModel(private val repository: BeerRepository) : ViewModel() {
    val offers = repository.observeOffers()
        .map { items -> items.sortedBy { it.pricePerLiter } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addOffer(
        brand: String,
        packageType: String,
        volumeMl: Int,
        quantity: Int,
        totalPrice: Double,
        store: String,
        hasReturnableBottle: Boolean
    ) {
        viewModelScope.launch {
            repository.addOffer(
                BeerOfferEntity(
                    brand = brand.trim(),
                    packageType = packageType.trim(),
                    volumeMl = volumeMl,
                    quantity = quantity,
                    totalPrice = totalPrice,
                    store = store.trim(),
                    hasReturnableBottle = hasReturnableBottle
                )
            )
        }
    }

    fun deleteOffer(offer: BeerOfferEntity) {
        viewModelScope.launch { repository.deleteOffer(offer) }
    }

    companion object {
        fun factory(repository: BeerRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BeerViewModel(repository) as T
                }
            }
    }
}

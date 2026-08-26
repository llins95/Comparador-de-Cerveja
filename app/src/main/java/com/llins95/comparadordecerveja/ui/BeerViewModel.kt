package com.llins95.comparadordecerveja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.llins95.comparadordecerveja.data.BeerOfferEntity
import com.llins95.comparadordecerveja.data.BeerRepository
import com.llins95.comparadordecerveja.domain.BeerPriceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BeerViewModel(private val repository: BeerRepository) : ViewModel() {
    val offers = repository.observeOffers()
        .map { items ->
            items.sortedWith(
                compareBy<BeerOfferEntity> { it.pricePerLiter }
                    .thenBy { it.totalPrice }
                    .thenBy { it.quantity }
            )
        }
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
        enteredPrice: Double,
        priceIsPerUnit: Boolean,
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
                    totalPrice = BeerPriceCalculator.promotionTotalPrice(
                        enteredPrice,
                        quantity,
                        priceIsPerUnit,
                    ),
                    priceIsPerUnit = priceIsPerUnit,
                    store = store.trim(),
                    hasReturnableBottle = hasReturnableBottle
                )
            )
        }
    }

    fun updateOffer(
        offer: BeerOfferEntity,
        brand: String,
        packageType: String,
        volumeMl: Int,
        quantity: Int,
        enteredPrice: Double,
        priceIsPerUnit: Boolean,
        store: String,
        hasReturnableBottle: Boolean
    ) {
        viewModelScope.launch {
            repository.addOffer(
                offer.copy(
                    brand = brand.trim(),
                    packageType = packageType.trim(),
                    volumeMl = volumeMl,
                    quantity = quantity,
                    totalPrice = BeerPriceCalculator.promotionTotalPrice(
                        enteredPrice,
                        quantity,
                        priceIsPerUnit,
                    ),
                    priceIsPerUnit = priceIsPerUnit,
                    store = store.trim(),
                    hasReturnableBottle = hasReturnableBottle
                )
            )
        }
    }

    suspend fun deleteOffer(offer: BeerOfferEntity) = repository.deleteOffer(offer.id)

    suspend fun restoreOffer(offer: BeerOfferEntity) = repository.addOffer(offer)

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

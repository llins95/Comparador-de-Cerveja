package com.llins95.comparadordecerveja.domain

object BeerPriceCalculator {
    fun totalVolumeMl(volumeMl: Int, quantity: Int): Int =
        (volumeMl.coerceAtLeast(0) * quantity.coerceAtLeast(0))

    fun pricePerLiter(totalPrice: Double, volumeMl: Int, quantity: Int): Double {
        val totalVolume = totalVolumeMl(volumeMl, quantity)
        if (totalPrice < 0 || totalVolume <= 0) return 0.0
        return totalPrice / totalVolume * 1000.0
    }

    fun promotionTotalPrice(
        enteredPrice: Double,
        quantity: Int,
        priceIsPerUnit: Boolean,
    ): Double {
        if (enteredPrice < 0 || quantity <= 0) return 0.0
        return if (priceIsPerUnit) enteredPrice * quantity else enteredPrice
    }

    fun pricePerUnit(totalPrice: Double, quantity: Int): Double {
        if (totalPrice < 0 || quantity <= 0) return 0.0
        return totalPrice / quantity
    }

    fun purchasableVolumeMl(
        budget: Double,
        packPrice: Double,
        volumeMl: Int,
        quantity: Int
    ): Long {
        if (budget <= 0 || packPrice <= 0) return 0L
        val packs = kotlin.math.floor(budget / packPrice).toLong()
        return packs * totalVolumeMl(volumeMl, quantity)
    }

    fun purchasableUnits(
        budget: Double,
        packPrice: Double,
        quantity: Int
    ): Long {
        if (budget <= 0 || packPrice <= 0 || quantity <= 0) return 0L
        val packs = kotlin.math.floor(budget / packPrice).toLong()
        return packs * quantity
    }
}

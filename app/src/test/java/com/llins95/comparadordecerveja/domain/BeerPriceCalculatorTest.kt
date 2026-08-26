package com.llins95.comparadordecerveja.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BeerPriceCalculatorTest {
    @Test
    fun packQuantityIsIncludedInPricePerLiter() {
        val result = BeerPriceCalculator.pricePerLiter(
            totalPrice = 39.90,
            volumeMl = 350,
            quantity = 12
        )

        assertEquals(9.50, result, 0.001)
    }

    @Test
    fun unitPricePromotionUsesRequiredQuantityToCalculateMinimumPurchase() {
        val total = BeerPriceCalculator.promotionTotalPrice(
            enteredPrice = 5.49,
            quantity = 21,
            priceIsPerUnit = true,
        )

        assertEquals(115.29, total, 0.001)
        assertEquals(5.49, BeerPriceCalculator.pricePerLiter(total, 1_000, 21), 0.001)
    }

    @Test
    fun totalPromotionPriceIsNotMultipliedAgain() {
        val total = BeerPriceCalculator.promotionTotalPrice(
            enteredPrice = 39.90,
            quantity = 12,
            priceIsPerUnit = false,
        )

        assertEquals(39.90, total, 0.001)
        assertEquals(3.325, BeerPriceCalculator.pricePerUnit(total, 12), 0.001)
    }

    @Test
    fun simulatorUsesWholePacks() {
        val volume = BeerPriceCalculator.purchasableVolumeMl(
            budget = 100.0,
            packPrice = 39.90,
            volumeMl = 350,
            quantity = 12
        )

        assertEquals(8400L, volume)
    }

    @Test
    fun simulatorReportsIndividualPackageCount() {
        val units = BeerPriceCalculator.purchasableUnits(
            budget = 100.0,
            packPrice = 39.90,
            quantity = 12
        )

        assertEquals(24L, units)
    }
}

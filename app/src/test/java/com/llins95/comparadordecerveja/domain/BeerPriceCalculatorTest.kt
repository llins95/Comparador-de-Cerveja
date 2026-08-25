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
    fun simulatorUsesWholePacks() {
        val volume = BeerPriceCalculator.purchasableVolumeMl(
            budget = 100.0,
            packPrice = 39.90,
            volumeMl = 350,
            quantity = 12
        )

        assertEquals(8400L, volume)
    }
}

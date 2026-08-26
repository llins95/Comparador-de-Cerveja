package com.llins95.comparadordecerveja.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.llins95.comparadordecerveja.domain.BeerPriceCalculator

@Entity(tableName = "beer_offers")
data class BeerOfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val packageType: String,
    val volumeMl: Int,
    val quantity: Int,
    val totalPrice: Double,
    @ColumnInfo(defaultValue = "0") val priceIsPerUnit: Boolean = false,
    val store: String,
    val hasReturnableBottle: Boolean = false,
    val source: String = "MANUAL",
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalVolumeMl: Int
        get() = BeerPriceCalculator.totalVolumeMl(volumeMl, quantity)

    val pricePerLiter: Double
        get() = BeerPriceCalculator.pricePerLiter(totalPrice, volumeMl, quantity)

    val pricePerUnit: Double
        get() = BeerPriceCalculator.pricePerUnit(totalPrice, quantity)
}

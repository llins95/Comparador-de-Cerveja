package com.llins95.comparadordecerveja.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeerOfferDao {
    @Query("SELECT * FROM beer_offers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BeerOfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: BeerOfferEntity)

    @Query("DELETE FROM beer_offers WHERE id = :offerId")
    suspend fun deleteById(offerId: Long)
}

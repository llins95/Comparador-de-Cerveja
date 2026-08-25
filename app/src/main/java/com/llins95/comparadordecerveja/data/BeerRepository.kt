package com.llins95.comparadordecerveja.data

class BeerRepository(private val dao: BeerOfferDao) {
    fun observeOffers() = dao.observeAll()

    suspend fun addOffer(offer: BeerOfferEntity) = dao.insert(offer)

    suspend fun deleteOffer(offer: BeerOfferEntity) = dao.delete(offer)
}

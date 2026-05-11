package com.example.saulifeapp.data.remote.model

data class PharmacyProduct(
    val title: String,
    val price: String
)

data class PharmacyOffer(
    val pharmacyName: String,
    val address: String,
    val phone: String,
    val workingHours: String,
    val products: List<PharmacyProduct>
)

data class PharmacySearchResult(
    val medicineName: String,
    val pageUrl: String,
    val averagePrice: String,
    val priceRange: String,
    val offersCount: String,
    val pharmaciesCount: String,
    val offers: List<PharmacyOffer>
)

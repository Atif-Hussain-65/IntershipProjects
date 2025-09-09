package com.neighbortrade.data

// ✅ Data classes for Firestore don't need special annotations
// if the variable names match the field names in the database.

data class ListingItem(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val location: Location? = null
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
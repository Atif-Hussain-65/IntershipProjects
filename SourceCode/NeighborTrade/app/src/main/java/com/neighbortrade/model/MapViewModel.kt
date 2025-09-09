package com.neighbortrade.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.neighbortrade.data.ListingItem
import com.neighbortrade.data.Location
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel : ViewModel() {

    val listingsState = mutableStateOf<List<ListingItem>>(emptyList())

    init {
        fetchListingsFromFirestore()
    }

    private fun fetchListingsFromFirestore() {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val result = db.collection("listings").get().await()
                val listings = result.documents.mapNotNull { document ->
                    document.toObject(ListingItem::class.java)
                }

                if (listings.isEmpty()) {
                    seedInitialData()
                } else {
                    listingsState.value = listings
                }

            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching from Firestore", e)
            }
        }
    }

    /**
     * Adds a list of sample items to the 'listings' collection if it's empty.
     */
    private fun seedInitialData() {
        viewModelScope.launch {
            Log.d("MapViewModel", "Database is empty. Seeding initial data...")

            // ✅ --- LIST OF 12 SAMPLE ITEMS --- ✅
            val sampleItems = listOf(
                ListingItem(1, "Used Google Pixel 6", "128GB, unlocked. Good condition.", 300.50, "Electronics", Location(33.6944, 73.0579)),
                ListingItem(2, "Vintage Leather Sofa", "Comfortable 3-seater. Minor scuffs.", 250.00, "Furniture", Location(33.6844, 73.0479)),
                ListingItem(3, "Men's Denim Jacket", "Brand new, size Large. Never worn.", 45.00, "Clothing", Location(33.6744, 73.0379)),
                ListingItem(4, "Old Wooden Bookshelf", "5 shelves, solid wood. Needs polish.", 75.00, "Furniture", Location(33.7011, 73.0654)),
                ListingItem(5, "Samsung 24-inch Monitor", "Full HD monitor, works perfectly.", 120.00, "Electronics", Location(33.6987, 73.0492)),
                ListingItem(6, "Women's Running Shoes", "Size 8, used twice.", 35.25, "Clothing", Location(33.6811, 73.0555)),
                ListingItem(7, "Antique Coffee Table", "Glass top with wooden frame.", 110.00, "Furniture", Location(33.6905, 73.0610)),
                ListingItem(8, "Bluetooth Headphones", "Sony WH-1000XM4, like new.", 220.00, "Electronics", Location(33.6789, 73.0699)),
                ListingItem(9, "Designer Handbag", "Leather, barely used.", 150.75, "Clothing", Location(33.7050, 73.0531)),
                ListingItem(10, "Office Chair", "Ergonomic, with lumbar support.", 85.00, "Furniture", Location(33.6858, 73.0312)),
                ListingItem(11, "Apple AirPods Pro", "First generation, with case.", 90.00, "Electronics", Location(33.6912, 73.0723)),
                ListingItem(12, "Summer Dress", "Floral pattern, size Medium.", 25.00, "Clothing", Location(33.6715, 73.0488))
            )

            val db = Firebase.firestore
            val listingsCollection = db.collection("listings")

            // Add each item to a batch write
            val batch = db.batch()
            sampleItems.forEach { item ->
                val docRef = listingsCollection.document() // Create a new document with a random ID
                batch.set(docRef, item)
            }

            // Commit the batch
            batch.commit()
                .addOnSuccessListener {
                    Log.d("MapViewModel", "Successfully seeded 12 items. Refetching...")
                    fetchListingsFromFirestore()
                }
                .addOnFailureListener { e ->
                    Log.e("MapViewModel", "Error seeding data", e)
                }
        }
    }
}
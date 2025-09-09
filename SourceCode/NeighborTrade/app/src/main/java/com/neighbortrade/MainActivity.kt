package com.neighbortrade

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.neighbortrade.data.ListingItem
import com.neighbortrade.model.MapViewModel
import com.neighbortrade.ui.theme.NeighborTradeTheme

class MainActivity : ComponentActivity() {

    // Initialize the ViewModel
    private val viewModel: MapViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is the entry point for your UI
        setContent {
            NeighborTradeTheme {
                // Get the list of items from the ViewModel's state
                val listings = viewModel.listingsState.value
                MainScreen(listings = listings)
            }
        }
    }
}

/**
 * The main screen composable that holds the map and the list.
 * @param listings The list of items to display.
 */
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MainScreen(listings: List<ListingItem>) {
    // A Column arranges its children vertically
    Column(modifier = Modifier.fillMaxSize()) {

        // --- 1. The Google Map ---
        // Set the initial camera position to Rawalpindi
        val rawalpindi = LatLng(33.6844, 73.0479)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(rawalpindi, 12f)
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Takes up the top 50% of the screen
            cameraPositionState = cameraPositionState
        ) {
            // Add a marker for each item in the list
            listings.forEach { item ->
                // ✅ Check if the location is not null before using it
                item.location?.let { location ->
                    Marker(
                        state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                        title = item.title,
                        snippet = "Price: $${item.price}"
                    )
                }
            }
        }

        // --- 2. The Listings List ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Takes up the bottom 50% of the screen
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Adds space between items
        ) {
            items(listings) { item ->
                ListingItemCard(item = item)
            }
        }
    }
}

/**
 * A card composable to display a single item in the list.
 * @param item The ListingItem data to display.
 */
@Composable
fun ListingItemCard(item: ListingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Price: $${item.price}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${item.category}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * A preview function to see your UI in Android Studio without running the app.
 */
@RequiresApi(Build.VERSION_CODES.N)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NeighborTradeTheme {
        // ✅ Fix is here: We specify the type of the list.
        val mockListings: List<ListingItem> = listOf()
        MainScreen(listings = mockListings)
    }
}
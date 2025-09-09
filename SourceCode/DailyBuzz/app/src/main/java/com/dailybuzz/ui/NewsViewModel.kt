package com.dailybuzz.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailybuzz.data.AppDatabase
import com.dailybuzz.data.ArticleEntity
import com.dailybuzz.data.NewsRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NewsRepository

    init {
        val articleDao = AppDatabase.getDatabase(application).articleDao()
        repository = NewsRepository(articleDao)

        // Check if we need to add default data
        checkForInitialData()
    }

    val allArticles = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedArticles = repository.savedArticles
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun checkForInitialData() {
        viewModelScope.launch {
            val db = Firebase.firestore
            val collection = db.collection("articles")
            val snapshot = collection.limit(1).get().await()

            if (snapshot.isEmpty) {
                // If the collection is empty, seed it with default articles
                seedDatabase()
            } else {
                // Otherwise, just refresh from existing online data
                repository.refreshNews()
            }
        }
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            Log.d("NewsViewModel", "Firestore is empty. Seeding database...")
            val sampleArticles = listOf(
                ArticleEntity(101, "The Future of AI", "Jane Doe", "2025-09-08T10:00:00Z", "Tech", "Artificial intelligence is evolving at an unprecedented rate...", false),
                ArticleEntity(201, "10 Minimalist Habits", "John Smith", "2025-09-07T14:30:00Z", "Lifestyle", "Living a simpler life can reduce stress and increase happiness...", false),
                ArticleEntity(301, "Local Team Wins Championship", "Alex Ray", "2025-09-08T18:45:00Z", "Sports", "In an incredible turn of events, the home team has clinched the title...", false),
                ArticleEntity(102, "New Quantum Computer Unveiled", "Jane Doe", "2025-09-06T11:00:00Z", "Tech", "Scientists have unveiled a new quantum computer that promises to solve complex problems...", false),
                ArticleEntity(202, "The Rise of Slow Travel", "Emily White", "2025-09-05T16:20:00Z", "Lifestyle", "More travelers are embracing 'slow travel', focusing on connection over a packed itinerary...", false),
                ArticleEntity(103, "Cybersecurity Threats in 2025", "Sam Brown", "2025-09-04T09:00:00Z", "Tech", "Experts warn of new sophisticated cybersecurity threats targeting smart home devices...", false),
                ArticleEntity(302, "Athlete Breaks World Record", "Chris Green", "2025-09-03T20:00:00Z", "Sports", "A new world record was set today in the 100-meter dash...", false),
                ArticleEntity(203, "Urban Gardening Guide", "Maria Garcia", "2025-09-02T13:00:00Z", "Lifestyle", "You don't need a large yard to grow your own food. Here's how to start an urban garden...", false)
            )

            val db = Firebase.firestore
            val batch = db.batch()
            sampleArticles.forEach { article ->
                val docRef = db.collection("articles").document(article.id.toString())
                batch.set(docRef, article)
            }

            batch.commit().addOnSuccessListener {
                Log.d("NewsViewModel", "Database seeded successfully. Refreshing news...")
                viewModelScope.launch {
                    repository.refreshNews()
                }
            }.addOnFailureListener { e ->
                Log.e("NewsViewModel", "Error seeding database", e)
            }
        }
    }

    fun toggleSavedState(article: ArticleEntity) {
        viewModelScope.launch {
            val updatedArticle = article.copy(isSaved = !article.isSaved)
            repository.updateArticle(updatedArticle)
        }
    }
    // Add this function inside your NewsViewModel class

    fun getArticleById(articleId: Int): ArticleEntity? {
        // This is a simple way to find the article in the current list.
        // A more advanced version might query the database directly.
        return allArticles.value.find { it.id == articleId }
    }
}
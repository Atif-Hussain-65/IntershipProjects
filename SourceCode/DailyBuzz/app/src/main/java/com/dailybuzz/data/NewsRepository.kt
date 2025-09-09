package com.dailybuzz.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.tasks.await

class NewsRepository(private val articleDao: ArticleDao) {

    // The UI will observe this Flow to get live updates from the database.
    val allArticles = articleDao.getAllArticles()
    val savedArticles = articleDao.getSavedArticles()

    /**
     * Fetches the latest articles from Firestore and saves them into the Room database.
     */
    suspend fun refreshNews() {
        try {
            // Fetch from Firestore
            val db = Firebase.firestore
            val result = db.collection("articles").get().await() // Use a collection named "articles"
            val articlesFromNetwork = result.documents.mapNotNull { doc ->
                // Map Firestore document to our Room Entity
                doc.toObject(ArticleEntity::class.java)
            }

            // Save the new articles into the Room database
            articleDao.insertAll(articlesFromNetwork)
            Log.d("NewsRepository", "Successfully fetched and saved ${articlesFromNetwork.size} articles.")

        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching news from Firestore", e)
        }
    }

    /**
     * Updates an article in the database (e.g., to save or unsave it).
     */
    suspend fun updateArticle(article: ArticleEntity) {
        articleDao.updateArticle(article)
    }
}
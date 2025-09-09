package com.dailybuzz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    // ✅ Add @JvmSuppressWildcards annotation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: @JvmSuppressWildcards List<ArticleEntity>)

    @Query("SELECT * FROM articles ORDER BY publicationDate DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isSaved = 1 ORDER BY publicationDate DESC")
    fun getSavedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :articleId")
    suspend fun getArticleById(articleId: Int): ArticleEntity?

    @Update
    suspend fun updateArticle(article: ArticleEntity)
}
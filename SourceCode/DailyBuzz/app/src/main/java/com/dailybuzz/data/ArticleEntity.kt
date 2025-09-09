package com.dailybuzz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int = 0,
    val title: String = "",
    val author: String = "",
    val publicationDate: String = "",
    val category: String = "",
    val content: String = "",
    var isSaved: Boolean = false
)
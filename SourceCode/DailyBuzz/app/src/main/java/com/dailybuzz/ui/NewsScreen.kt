package com.dailybuzz.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailybuzz.data.ArticleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel, onArticleClick: (Int) -> Unit) {
    val allArticles by viewModel.allArticles.collectAsState()
    val savedArticles by viewModel.savedArticles.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All News", "Saved Articles")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("DailyBuzz") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> ArticleList(articles = allArticles, onSaveToggle = viewModel::toggleSavedState, onArticleClick = onArticleClick)
                1 -> ArticleList(articles = savedArticles, onSaveToggle = viewModel::toggleSavedState, onArticleClick = onArticleClick)
            }
        }
    }
}

@Composable
fun ArticleList(
    articles: List<ArticleEntity>,
    onSaveToggle: (ArticleEntity) -> Unit,
    onArticleClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(articles) { article ->
            ArticleCard(article = article, onSaveToggle = onSaveToggle, onArticleClick = onArticleClick)
        }
    }
}

@Composable
fun ArticleCard(
    article: ArticleEntity,
    onSaveToggle: (ArticleEntity) -> Unit,
    onArticleClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArticleClick(article.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = article.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "By ${article.author} | ${article.category}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = article.content, style = MaterialTheme.typography.bodyLarge, maxLines = 3)
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(
                onClick = { onSaveToggle(article) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (article.isSaved) Icons.Filled.Check else Icons.Outlined.CheckCircle,
                    contentDescription = "Save Article",
                    tint = if (article.isSaved) MaterialTheme.typography.bodyLarge.color else Color.Gray
                )
            }
        }
    }
}
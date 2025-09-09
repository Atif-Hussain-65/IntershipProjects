package com.dailybuzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dailybuzz.ui.ArticleDetailScreen
import com.dailybuzz.ui.NewsScreen
import com.dailybuzz.ui.NewsViewModel
import com.dailybuzz.ui.theme.DailyBuzzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyBuzzTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val newsViewModel: NewsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        // Route for the main news list screen
        composable("home") {
            NewsScreen(
                viewModel = newsViewModel,
                onArticleClick = { articleId ->
                    navController.navigate("detail/$articleId")
                }
            )
        }
        // Route for the article detail screen
        composable(
            route = "detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId")
            if (articleId != null) {
                ArticleDetailScreen(articleId = articleId, viewModel = newsViewModel)
            }
        }
    }
}
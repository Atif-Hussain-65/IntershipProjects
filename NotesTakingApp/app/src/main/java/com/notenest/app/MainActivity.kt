package com.notenest.app

import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.notenest.app.ui.theme.NoteNestTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notenest.app.ui.AddEditNoteScreen
import com.notenest.app.ui.NoteListScreen
import com.notenest.app.viewmodel.NoteViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteNestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: NoteViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "noteList") {
                        composable("noteList") {
                            NoteListScreen(
                                viewModel = viewModel,
                                onNoteClick = { noteId ->
                                    navController.navigate("addEditNote/$noteId")
                                },
                                onAddNoteClick = {
                                    navController.navigate("addEditNote/-1")
                                }
                            )
                        }
                        composable(
                            route = "addEditNote/{noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                            AddEditNoteScreen(
                                viewModel = viewModel,
                                noteId = noteId,
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoteNestTheme {
        Greeting("Android")
    }
}
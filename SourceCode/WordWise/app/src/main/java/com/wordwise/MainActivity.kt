package com.wordwise // Make sure this matches your project's package name

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.random.Random

// --- THEME COLORS ---
val LightBlue = Color(0xFFE0F7FA)
val DarkTeal = Color(0xFF004D40)
val CardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF0F0F0))
)
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(LightBlue, Color(0xFFB2EBF2))
)

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported!")
                }
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }

        setContent {
            // Applying a basic theme
            MaterialTheme {
                WordWiseApp(tts)
            }
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}

data class VocabularyWord(
    val word: String,
    val definition: String,
)

@Composable
fun WordWiseApp(tts: TextToSpeech) {
    val initialVocabulary = remember {
        listOf(
            VocabularyWord("Serendipity", "A fortunate discovery made by accident."),
            VocabularyWord("Ephemeral", "Lasting for a very short time."),
            VocabularyWord("Luminous", "Emitting or reflecting bright light."),
            VocabularyWord("Mellifluous", "A sound that is sweet and pleasant to hear."),
            VocabularyWord("Ubiquitous", "Present, appearing, or found everywhere.")
        )
    }

    var vocabulary by remember { mutableStateOf(initialVocabulary) }
    var currentCardIndex by remember { mutableStateOf(0) }
    val currentWord = vocabulary[currentCardIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- LOGO AND APP TITLE ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            WordWiseLogo()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WordWise",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = DarkTeal
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- FLASHCARD ---
        Flashcard(
            word = currentWord.word,
            translation = currentWord.definition,
            modifier = Modifier.height(250.dp) // Increased card height
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PROGRESS INDICATOR ---
        Text(
            text = "${currentCardIndex + 1} / ${vocabulary.size}",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkTeal.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.weight(1f))


        // --- CONTROLS ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { tts.speak(currentWord.word, TextToSpeech.QUEUE_FLUSH, null, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTeal),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Speak Word", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { if (currentCardIndex > 0) currentCardIndex-- }, enabled = currentCardIndex > 0) { Text("Previous") }

                // --- SHUFFLE BUTTON ---
                IconButton(onClick = {
                    vocabulary = vocabulary.shuffled()
                    currentCardIndex = 0
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Shuffle deck", tint = DarkTeal)
                }

                Button(onClick = { if (currentCardIndex < vocabulary.size - 1) currentCardIndex++ }, enabled = currentCardIndex < vocabulary.size - 1) { Text("Next") }
            }
        }
    }
}

@Composable
fun Flashcard(word: String, translation: String, modifier: Modifier = Modifier) {
    var isFlipped by remember { mutableStateOf(false) }

    val rotationY by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12 * density
            }
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(24.dp), // More rounded corners
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardGradient), // Applying gradient to card
            contentAlignment = Alignment.Center
        ) {
            if (rotationY <= 90f) {
                Text(
                    text = word,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = DarkTeal,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = translation,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = DarkTeal.copy(alpha = 0.8f),
                    modifier = Modifier
                        .graphicsLayer { rotationY >= 180f }
                        .padding(16.dp)
                )
            }
        }
    }
}

// --- SVG LOGO COMPOSABLE ---
@Composable
fun WordWiseLogo() {
    // A simple book/dialog icon logo
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(DarkTeal)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // This is a simple representation. For a real app, you'd use an SVG.
        Text("W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}


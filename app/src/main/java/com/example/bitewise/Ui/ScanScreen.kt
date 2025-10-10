package com.example.bitewise.Ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bitewise.data.LocalRecipes
import com.example.bitewise.data.Recipe
import com.example.bitewise.ml.TFLiteClassifier
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// Function to calculate score between ingredients and recipe
fun score(ingredientsSet: Set<String>, recipe: Recipe): Int {
    return recipe.ingredients.count { it.lowercase() in ingredientsSet }
}

@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val classifier = remember { TFLiteClassifier(context) }

    // Permission
    var hasCamera by remember { mutableStateOf(false) }
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamera = granted }

    LaunchedEffect(Unit) { requestPermission.launch(android.Manifest.permission.CAMERA) }

    // CameraX state
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder().setTargetResolution(Size(1080, 1920)).build()
    }

    // Recognition state
    var isProcessing by remember { mutableStateOf(false) }
    var recognized by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    // Known ingredients from your recipe seed
    val knownIngredients = remember { LocalRecipes.flatMap { it.ingredients }.toSet() }

    // Suggestions
    val recognizedSet = remember(recognized) { recognized.map { it.lowercase() }.toSet() }
    val suggestions = remember(recognizedSet) {
        LocalRecipes
            .asSequence()
            .map { it to score(recognizedSet, it) }
            .filter { it.second > 0 }
            .sortedWith(
                compareByDescending<Pair<Recipe, Int>> { it.second }
                    .thenBy { it.first.timeMins }
            )
            .map { it.first }
            .toList()
    }

    Column(Modifier.fillMaxSize()) {
        Text("Scan Ingredients", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))

        if (!hasCamera) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Camera permission is required.")
            }
        } else {
            CameraPreview(previewView, imageCapture, lifecycleOwner)

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    error = null
                    isProcessing = true
                    captureAndDetect(
                        context = context,
                        imageCapture = imageCapture,
                        onResult = { labels ->
                            isProcessing = false
                            recognized = labels.map { it.lowercase() }
                                .filter { it in knownIngredients }
                                .distinct()
                        },
                        onError = { msg ->
                            isProcessing = false
                            error = msg
                        },
                        classifier = classifier // Pass classifier here
                    )
                }) { Text(if (isProcessing) "Processing..." else "Capture") }

                OutlinedButton(onClick = {
                    recognized = emptyList()
                    error = null
                }) { Text("Clear") }
            }
        }

        if (recognized.isNotEmpty() || error != null) {
            Divider()
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (recognized.isNotEmpty()) {
                    item {
                        Text("Detected ingredients:", fontWeight = FontWeight.SemiBold)
                        Text(recognized.joinToString(", "))
                        Spacer(Modifier.height(8.dp))
                        Text("Recipe suggestions", style = MaterialTheme.typography.titleMedium)
                    }
                    items(suggestions, key = { it.id }) { r -> RecipeCardCompact(r) }
                    if (suggestions.isEmpty()) {
                        item { Text("No matching recipes in local set. Try another photo or add more recipes.") }
                    }
                }
                error?.let { e ->
                    item { Text("Error: $e", color = MaterialTheme.colorScheme.error) }
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                "Tip: aim at 1–3 items (e.g., banana, oats, milk).",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Camera preview setup
@Composable
private fun CameraPreview(
    previewView: PreviewView,
    imageCapture: ImageCapture,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
    }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f))
}

// Capture image and detect labels
private fun captureAndDetect(
    context: Context,
    imageCapture: ImageCapture,
    onResult: (List<String>) -> Unit,
    onError: (String) -> Unit,
    classifier: TFLiteClassifier // Add this line to accept classifier
) {
    val outputDir = File(context.cacheDir, "captures").apply { mkdirs() }
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val file = File(outputDir, "$name.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                onError("Capture failed: ${exc.message}")
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val bmp = loadBitmap(context, Uri.fromFile(file)) ?: run {
                    onError("Could not decode image")
                    return
                }
                runLabeler(bmp, onResult, onError, classifier) // Pass classifier here
            }
        }
    )
}

// Helper function to load a bitmap from URI
private fun loadBitmap(context: Context, uri: Uri): Bitmap? = try {
    if (Build.VERSION.SDK_INT >= 28) {
        val src = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
        android.graphics.ImageDecoder.decodeBitmap(src)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
} catch (_: Exception) { null }

// Function to run the labeler (classifier)
private fun runLabeler(
    bitmap: Bitmap,
    onResult: (List<String>) -> Unit,
    onError: (String) -> Unit,
    classifier: TFLiteClassifier
) {
    try {
        val label = classifier.classify(bitmap) // ✅ CORRECT: Remove numClasses parameter
        onResult(listOf(label))
    } catch (e: Exception) {
        onError("Classification failed: ${e.message}")
    }
}

// Recipe card compact display
@Composable
private fun RecipeCardCompact(r: Recipe) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(r.name, style = MaterialTheme.typography.titleMedium)
            Text("${r.timeMins} min • Ingredients: ${r.ingredients.joinToString(", ")}")
        }
    }
}

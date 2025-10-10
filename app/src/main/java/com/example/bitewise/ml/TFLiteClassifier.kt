package com.example.bitewise.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel.MapMode

val modelName = "ingredients_model.tflite"  // Ensure the correct model path

// Load model from assets
fun loadModelFile(context: Context): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)  // Use modelName for dynamic path
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.length)
}

class TFLiteClassifier(context: Context) {
    private val interpreter: Interpreter = Interpreter(loadModelFile(context))  // Load the model using Interpreter

    // Function to classify a bitmap image using the model
    fun classify(bitmap: Bitmap): String {
        val byteBuffer = convertBitmapToByteBuffer(bitmap)

        // Prepare input tensor
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Prepare output tensor (adjust size based on model)
        val outputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1001), DataType.FLOAT32)  // Example output size

        // Run inference using the interpreter
        interpreter.run(inputFeature0.buffer, outputFeature0.buffer)

        // Extract the result
        return extractResult(outputFeature0)
    }

    // Convert the bitmap to a byte buffer for input to the model
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3) // 4 bytes for each pixel
        byteBuffer.order(ByteOrder.nativeOrder())

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val pixels = IntArray(224 * 224)
        resizedBitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        // Normalize the pixel values (0-255 to 0-1 range)
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            byteBuffer.putFloat(r / 255.0f)
            byteBuffer.putFloat(g / 255.0f)
            byteBuffer.putFloat(b / 255.0f)
        }

        return byteBuffer
    }

    // Extract the result from the output tensor (max index as result)
    private fun extractResult(outputFeature0: TensorBuffer): String {
        val probabilities = outputFeature0.floatArray
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        return "Class: $maxIndex, Probability: ${probabilities[maxIndex]}"
    }

    // Close the interpreter to release resources
    fun close() {
        interpreter.close()
    }
}

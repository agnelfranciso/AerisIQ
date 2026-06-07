package com.example.aerisiq.ai

import android.content.Context
import android.os.Environment
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class AiManager private constructor(private val context: Context) {

    private var llmInference: LlmInference? = null

    companion object {
        @Volatile
        private var instance: AiManager? = null

        fun getInstance(context: Context): AiManager {
            return instance ?: synchronized(this) {
                instance ?: AiManager(context).also { instance = it }
            }
        }
    }

    fun initializeModel() {
        if (llmInference != null) return

        val modelFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), ModelDownloader.MODEL_FILENAME)
        if (!modelFile.exists() || modelFile.length() < 100_000_000L) {
            throw IllegalStateException("Model file is missing or corrupted. Please restart the app to redownload.")
        }

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(512) // Reduced to prevent OOM
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun generateResponse(prompt: String): String {
        return try {
            if (llmInference == null) {
                initializeModel()
            }
            llmInference?.generateResponse(prompt) ?: "Error: Model not initialized."
        } catch (e: Throwable) {
            "AI Error: ${e.message}"
        }
    }
}

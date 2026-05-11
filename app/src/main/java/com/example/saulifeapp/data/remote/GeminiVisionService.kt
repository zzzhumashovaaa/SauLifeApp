package com.example.saulifeapp.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiVisionService {

    private val apiKey = "AIzaSyAlB_fnTPxplvDzolbcWO5PCBez_Y7HdUE"

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun extractMedicineName(bitmap: Bitmap): Result<String> {
        return try {
            val prompt = """
                На изображении упаковка лекарства или рецепт.
                Найди главное название лекарства.
                
                Ответь только названием лекарства на русском языке.
                Без объяснений, без кавычек, без дозировки.
                
                Пример:
                диклофенак
            """.trimIndent()

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val medicineName = response.text
                ?.trim()
                ?.replace("\"", "")
                ?.replace("*", "")
                ?.lowercase()
                ?: ""

            if (medicineName.isBlank()) {
                Result.failure(Exception("Название лекарства не найдено"))
            } else {
                Result.success(medicineName)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
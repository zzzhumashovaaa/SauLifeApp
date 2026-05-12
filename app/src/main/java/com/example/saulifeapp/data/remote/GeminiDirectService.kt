package com.example.saulifeapp.data.remote

import com.example.saulifeapp.BuildConfig
import com.example.saulifeapp.ui.profile.UserProfile
import com.example.saulifeapp.ui.chat.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel

class GeminiDirectService {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = apiKey
    )

    suspend fun ask(
        message: String,
        profile: UserProfile?,
        history: List<ChatMessage>
    ): Result<String> {
        return try {
            val historyText = history.takeLast(8).joinToString("\n") {
                if (it.isUser) "Пользователь: ${it.text}" else "AI: ${it.text}"
            }

            val prompt = """
                Ты — AI-ассистент мобильного приложения SauLife.
                Помогаешь пользователю разбираться с лекарствами, аналогами, приёмом препаратов и общими вопросами здоровья.

                ВАЖНЫЕ ПРАВИЛА:
                1. Не ставь диагнозы.
                2. Не назначай лечение как врач.
                3. Не говори уверенно, что препарат точно подходит.
                4. Если есть риск, советуй обратиться к врачу или фармацевту.
                5. Учитывай профиль пользователя.
                6. Отвечай коротко, понятно и дружелюбно.
                7. Не используй Markdown: без **, #, списков со сложной разметкой.
                8. Если информации мало — задай уточняющий вопрос.

                ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ:
                Имя: ${profile?.fullName ?: "не указано"}
                Возраст: ${profile?.age ?: "не указано"}
                Пол: ${profile?.gender ?: "не указано"}
                Беременность: ${
                when (profile?.isPregnant) {
                    true -> "да"
                    false -> "нет"
                    null -> "не указано"
                }
            }
                Аллергии: ${profile?.allergies?.ifBlank { "не указано" } ?: "не указано"}
                Хронические состояния: ${profile?.chronicDiseases?.ifBlank { "не указано" } ?: "не указано"}
                Текущие лекарства: ${profile?.currentMedications?.ifBlank { "не указано" } ?: "не указано"}

                ИСТОРИЯ ДИАЛОГА:
                $historyText

                НОВЫЙ ВОПРОС:
                $message

                Ответь как осторожный медицинский AI-ассистент SauLife.
            """.trimIndent()

            val response = model.generateContent(prompt)

            val cleanedText = response.text
                ?.replace("**", "")
                ?.replace("#", "")
                ?.trim()
                ?: "Не удалось получить ответ."

            Result.success(cleanedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
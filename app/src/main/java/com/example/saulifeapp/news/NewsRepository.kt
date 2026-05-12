package com.example.saulifeapp.news

import com.example.saulifeapp.BuildConfig


object NewsRepository {

    private const val API_KEY = "e152fe01685c7b922a58e66930f9988c"

    suspend fun getHealthNews(): List<NewsItem> {
        val queries = listOf(
            "диетолог OR здоровье OR врач",
            "медицина OR здоровье",
            "лекарства OR аптека"
        )

        val response = GNewsRetrofitClient.api.searchNews(
            query = queries.random(),
            lang = "ru",
            country = "ru",
            max = 10,
            sortBy = "publishedAt",
            apiKey = API_KEY
        )

        return response.articles
            .filter { article ->
                val text = "${article.title} ${article.description}".lowercase()

                article.title?.isNotBlank() == true &&
                        !text.contains("секс") &&
                        !text.contains("ораль") &&
                        !text.contains("футбол") &&
                        !text.contains("стрельб")
            }
            .map { article ->
                NewsItem(
                    title = article.title ?: "Медицинская новость",
                    preview = article.description
                        ?: "Нажмите «Читать», чтобы открыть новость.",
                    content = article.content
                        ?: article.description
                        ?: "Полный текст доступен по ссылке источника.",
                    url = article.url ?: ""
                )
            }
            .shuffled()
            .take(10)
    }
}
package com.example.saulifeapp.news

data class GNewsResponse(
    val totalArticles: Int = 0,
    val articles: List<GNewsArticle> = emptyList()
)

data class GNewsArticle(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val url: String? = null,
    val image: String? = null,
    val publishedAt: String? = null,
    val source: GNewsSource? = null
)

data class GNewsSource(
    val name: String? = null,
    val url: String? = null
)
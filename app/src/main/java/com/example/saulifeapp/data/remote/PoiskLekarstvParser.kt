package com.example.saulifeapp.data.remote

import com.example.saulifeapp.data.remote.model.PharmacyOffer
import com.example.saulifeapp.data.remote.model.PharmacyProduct
import com.example.saulifeapp.data.remote.model.PharmacySearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class PoiskLekarstvParser {

    suspend fun searchMedicine(query: String): Result<PharmacySearchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val slug = buildSlug(query)
            val url = "https://poisklekarstv.kz/$slug"

            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) SauLifeApp/1.0")
                .timeout(20000)
                .get()

            parseDocument(doc, url)
        }
    }

    private fun buildSlug(query: String): String {
        val translitMap = mapOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g",
            'д' to "d", 'е' to "e", 'ё' to "e", 'ж' to "zh",
            'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k",
            'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o",
            'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t",
            'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "c",
            'ч' to "ch", 'ш' to "sh", 'щ' to "sh",
            'ы' to "y", 'э' to "e", 'ю' to "yu", 'я' to "ya"
        )

        return query
            .lowercase()
            .map { translitMap[it] ?: it.toString() }
            .joinToString("")
            .replace(Regex("""[^a-z0-9\s-]"""), "")
            .replace(Regex("""\s+"""), "-")
    }

    private fun parseDocument(doc: Document, url: String): PharmacySearchResult {
        val bodyText = doc.body().text()
        val medicineName = doc.selectFirst("h1")?.text()?.trim().orEmpty()

        val averagePrice = Regex("""Средняя цена\s*(\d[\d\s]*)""")
            .find(bodyText)
            ?.groupValues?.getOrNull(1)
            ?.replace(Regex("""\s+"""), " ")
            ?.plus(" тг")
            .orEmpty()

        val priceRange = Regex("""(\d[\d\s]*)\s*[–-]\s*(\d[\d\s]*)\s*тг""")
            .find(bodyText)
            ?.value
            ?.replace(Regex("""\s+"""), " ")
            .orEmpty()

        val countsMatch = Regex(
            """найдено\s+(\d+)\s+предложени[йея]\s+и\s+(\d+)\s+аптек""",
            RegexOption.IGNORE_CASE
        ).find(bodyText)

        val offersCount = countsMatch?.groupValues?.getOrNull(1).orEmpty()
        val pharmaciesCount = countsMatch?.groupValues?.getOrNull(2).orEmpty()

        val offers = mutableListOf<PharmacyOffer>()
        val headers = doc.select("h2")

        for (header in headers) {
            val title = header.text().trim()
            if (title.isBlank()) continue
            if (
                title.contains("Инструкция", true) ||
                title.contains("Отзывы", true) ||
                title.contains("Аналог", true)
            ) continue

            val lines = mutableListOf<String>()
            var next = header.nextElementSibling()
            var counter = 0

            while (next != null && next.tagName() != "h2" && counter < 25) {
                val text = next.text().trim()
                if (text.isNotBlank()) lines += text
                next = next.nextElementSibling()
                counter++
            }

            val address = lines.firstOrNull { it.startsWith("Адрес:") }
                ?.removePrefix("Адрес:")
                ?.trim()
                .orEmpty()

            val phone = lines.firstOrNull { it.startsWith("Телефон:") }
                ?.removePrefix("Телефон:")
                ?.trim()
                .orEmpty()

            val workingHours = lines.firstOrNull { it.startsWith("Время работы:") }
                ?.removePrefix("Время работы:")
                ?.trim()
                .orEmpty()

            val products = lines.mapNotNull { line ->
                val priceMatch = Regex("""\d[\d\s]*\s*тг\.?""", RegexOption.IGNORE_CASE).find(line)
                if (priceMatch != null) {
                    val price = priceMatch.value
                        .replace(Regex("""\s+"""), " ")
                        .trim()

                    val name = line.substringBefore(priceMatch.value).trim().ifBlank { line }
                    PharmacyProduct(title = name, price = price)
                } else {
                    null
                }
            }.distinctBy { it.title + it.price }

            if (address.isNotBlank() || products.isNotEmpty()) {
                offers += PharmacyOffer(
                    pharmacyName = title,
                    address = address,
                    phone = phone,
                    workingHours = workingHours,
                    products = products
                )
            }
        }

        return PharmacySearchResult(
            medicineName = medicineName.ifBlank { doc.title() },
            pageUrl = url,
            averagePrice = averagePrice,
            priceRange = priceRange,
            offersCount = offersCount,
            pharmaciesCount = pharmaciesCount,
            offers = offers
        )
    }
}
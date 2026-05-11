package com.example.saulifeapp.pharmacy

import org.jsoup.Jsoup
import java.net.URLEncoder

object PoiskLekarstvRepository {

    fun searchMedicine(query: String): List<PharmacyResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val encodedLocation = URLEncoder.encode("Казахстан, Алматы", "UTF-8")

        val url =
            "https://www.poisklekarstv.kz/search?lat=43.273564&lng=76.914851&q=$encodedQuery&location=$encodedLocation"

        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .referrer("https://www.google.com")
            .timeout(20000)
            .get()

        val text = doc.body().text()

        val results = mutableListOf<PharmacyResult>()

        val priceRegex = Regex("""(\d[\d\s]{2,})\s*₸|(\d[\d\s]{2,})\s*тг""")
        val prices = priceRegex.findAll(text).toList()

        prices.forEachIndexed { index, match ->
            val priceText = match.value
                .replace("₸", "")
                .replace("тг", "")
                .replace(" ", "")
                .trim()

            val price = priceText.toDoubleOrNull() ?: return@forEachIndexed

            val start = (match.range.first - 250).coerceAtLeast(0)
            val end = (match.range.last + 350).coerceAtMost(text.length)
            val block = text.substring(start, end)

            val pharmacyName = extractPharmacyName(block)
            val address = extractAddress(block)

            results.add(
                PharmacyResult(
                    medicineName = query,
                    dosage = extractDosage(block),
                    pharmacyName = pharmacyName.ifBlank { "Аптека №${index + 1}" },
                    address = address.ifBlank { "Адрес не указан" },
                    price = price,
                    distance = "—",
                    available = true
                )
            )
        }

        return results
            .distinctBy { "${it.pharmacyName}-${it.address}-${it.price}" }
            .sortedBy { it.price }
            .take(30)
    }

    private fun extractDosage(text: String): String {
        val regex = Regex(
            """\d+([,.]\d+)?\s?(мг|мл|г|%)|таблетки|капсулы|сироп|раствор|суспензия|капли|спрей""",
            RegexOption.IGNORE_CASE
        )
        return regex.find(text)?.value ?: "Дозировка не указана"
    }

    private fun extractPharmacyName(text: String): String {
        val known = listOf(
            "Europharma",
            "Зерде-Фарм",
            "Аптека от склада",
            "Рауза",
            "Биосфера",
            "Мелисса",
            "Забота",
            "Аптека"
        )

        return known.firstOrNull { text.contains(it, ignoreCase = true) } ?: ""
    }

    private fun extractAddress(text: String): String {
        val addressRegex = Regex(
            """(г\.?\s?Алматы[^₸тг]{5,120}|Алматы[^₸тг]{5,120}|ул\.[^₸тг]{5,120}|пр\.[^₸тг]{5,120})""",
            RegexOption.IGNORE_CASE
        )

        return addressRegex.find(text)
            ?.value
            ?.replace("Показать на карте", "")
            ?.replace("Позвонить", "")
            ?.trim()
            ?: ""
    }
}
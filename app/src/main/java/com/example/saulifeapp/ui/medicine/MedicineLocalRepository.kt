package com.example.saulifeapp.ui.medicine

object MedicineLocalRepository {

    val medicines = listOf(
        Medicine(
            id = 1,
            name = "Парацетамол",
            type = "Таблетки",
            dosage = "500 мг",
            forWhom = "Взрослым и детям старше 12 лет",
            purpose = "При температуре, головной боли, боли в мышцах",
            whenToTake = "После еды, запивая водой",
            instruction = "Принимать по 1 таблетке 2–3 раза в день. Не превышать дозировку.",
            contraindications = "Проблемы с печенью, аллергия на парацетамол, алкогольная зависимость.",
            price = 450.0
        ),
        Medicine(
            id = 2,
            name = "Парацетамол",
            type = "Сироп",
            dosage = "120 мг / 5 мл",
            forWhom = "Детям",
            purpose = "Для снижения температуры и боли",
            whenToTake = "После еды. Дозировка зависит от возраста и веса.",
            instruction = "Использовать мерную ложку. Перед применением взболтать.",
            contraindications = "Аллергия, болезни печени. Детям до 3 месяцев — только после врача.",
            price = 850.0
        ),
        Medicine(
            id = 3,
            name = "Ибупрофен",
            type = "Капсулы",
            dosage = "200 мг",
            forWhom = "Взрослым и подросткам",
            purpose = "Боль, воспаление, температура",
            whenToTake = "После еды",
            instruction = "Принимать по инструкции. Не сочетать с другими НПВС.",
            contraindications = "Язва желудка, болезни почек, беременность, аллергия.",
            price = 1200.0
        )
    )

    fun search(query: String): List<Medicine> {
        return medicines.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun getById(id: Int): Medicine? {
        return medicines.find { it.id == id }
    }
}
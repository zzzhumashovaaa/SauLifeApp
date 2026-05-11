package com.example.saulifeapp.ui.pharmacy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.R
import com.example.saulifeapp.data.remote.PoiskLekarstvParser
import kotlinx.coroutines.launch

class PharmacyOffersActivity : AppCompatActivity() {

    private lateinit var tvMedicineName: TextView
    private lateinit var tvPriceInfo: TextView
    private lateinit var tvMetaInfo: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvOffers: RecyclerView
    private lateinit var btnBack: Button

    private val parser = PoiskLekarstvParser()
    private val adapter = PharmacyOfferAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pharmacy_offers)

        tvMedicineName = findViewById(R.id.tvMedicineName)
        tvPriceInfo = findViewById(R.id.tvPriceInfo)
        tvMetaInfo = findViewById(R.id.tvMetaInfo)
        progressBar = findViewById(R.id.progressBar)
        rvOffers = findViewById(R.id.rvOffers)
        btnBack = findViewById(R.id.btnBack)

        rvOffers.layoutManager = LinearLayoutManager(this)
        rvOffers.adapter = adapter

        btnBack.setOnClickListener { finish() }

        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME).orEmpty()
        if (medicineName.isBlank()) {
            Toast.makeText(this, "Название лекарства не передано", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvMedicineName.text = medicineName
        loadOffers(medicineName)
    }

    private fun loadOffers(medicineName: String) {
        progressBar.visibility = View.VISIBLE
        tvPriceInfo.text = "Ищем цены и аптеки..."
        tvMetaInfo.text = ""

        lifecycleScope.launch {
            val result = parser.searchMedicine(medicineName)
            progressBar.visibility = View.GONE

            result.onSuccess { data ->
                tvMedicineName.text = data.medicineName.ifBlank { medicineName }
                val priceParts = listOf(data.averagePrice, data.priceRange).filter { it.isNotBlank() }
                tvPriceInfo.text = if (priceParts.isEmpty()) "Цена не найдена" else priceParts.joinToString(" • ")

                val metaParts = mutableListOf<String>()
                if (data.offersCount.isNotBlank()) metaParts += "Предложений: ${data.offersCount}"
                if (data.pharmaciesCount.isNotBlank()) metaParts += "Аптек: ${data.pharmaciesCount}"
                tvMetaInfo.text = metaParts.joinToString(" • ")

                adapter.updateData(data.offers)

                if (data.offers.isEmpty()) {
                    Toast.makeText(this@PharmacyOffersActivity, "На странице не найдено предложений", Toast.LENGTH_LONG).show()
                }
            }.onFailure { error ->
                tvPriceInfo.text = "Не удалось загрузить данные"
                tvMetaInfo.text = error.message ?: "Ошибка парсинга"
                Toast.makeText(this@PharmacyOffersActivity, error.message ?: "Ошибка", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_MEDICINE_NAME = "medicine_name"
    }
}

package com.example.saulifeapp.ui.scanresult

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.ui.pharmacy.PharmacyOffersActivity

class ScanResultActivity : AppCompatActivity() {

    private lateinit var etRawText: EditText
    private lateinit var btnProcessText: Button
    private lateinit var btnFindInPharmacies: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        etRawText = findViewById(R.id.etRawText)
        btnProcessText = findViewById(R.id.btnProcessText)
        btnFindInPharmacies = findViewById(R.id.btnFindInPharmacies)

        val rawText = intent.getStringExtra("ocr_text").orEmpty()
        etRawText.setText(rawText)

        btnProcessText.setOnClickListener {
            val text = etRawText.text.toString().trim()
            if (text.isBlank()) {
                Toast.makeText(this, "Нет текста для обработки", Toast.LENGTH_SHORT).show()
            } else {
                etRawText.setText(text.lowercase())
            }
        }

        btnFindInPharmacies.setOnClickListener {
            val medicineName = etRawText.text.toString().trim()

            if (medicineName.isBlank()) {
                Toast.makeText(this, "Введите название лекарства", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, PharmacyOffersActivity::class.java)
            intent.putExtra("medicine_name", medicineName)
            startActivity(intent)
        }
    }
}
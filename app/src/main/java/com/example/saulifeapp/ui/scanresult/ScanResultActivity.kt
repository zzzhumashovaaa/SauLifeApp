package com.example.saulifeapp.ui.scanresult

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.CameraActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.ui.pharmacy.PharmacyOffersActivity

class ScanResultActivity : AppCompatActivity() {

    private lateinit var etRawText: EditText
    private lateinit var btnFindInPharmacies: Button
    private lateinit var btnScanAgain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        etRawText = findViewById(R.id.etRawText)
        btnFindInPharmacies = findViewById(R.id.btnFindInPharmacies)
        btnScanAgain = findViewById(R.id.btnScanAgain)

        val rawText = intent.getStringExtra("ocr_text").orEmpty()
        val cleanedText = rawText.trim()

        etRawText.setText(cleanedText)

        if (cleanedText.isBlank()) {
            etRawText.hint = "Введите название лекарства вручную"
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

        btnScanAgain.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
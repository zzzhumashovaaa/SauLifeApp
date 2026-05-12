package com.example.saulifeapp.ui.medicine

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.Product
import com.example.saulifeapp.R
import com.example.saulifeapp.databinding.ActivityMedicineDetailBinding

class MedicineDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineDetailBinding
    private val medicineCartRepository = MedicineCartRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val medicineId = intent.getIntExtra("medicine_id", -1)
        val medicine = MedicineLocalRepository.getById(medicineId)

        if (medicine == null) {
            Toast.makeText(this, "Лекарство не найдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.textMedicineName.text = medicine.name
        binding.textMedicineType.text = "${medicine.type} • ${medicine.dosage}"
        binding.textForWhom.text = medicine.forWhom
        binding.textPurpose.text = medicine.purpose
        binding.textWhenToTake.text = medicine.whenToTake
        binding.textInstruction.text = medicine.instruction
        binding.textContraindications.text = medicine.contraindications
        binding.textPrice.text = "${medicine.price.toInt()} ₸"

        binding.btnAddToCart.setOnClickListener {

            medicineCartRepository.addMedicineToCart(medicine) { success ->

                Toast.makeText(
                    this,
                    if (success) "Дәрі себетке қосылды" else "Себетке қосу мүмкін болмады",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
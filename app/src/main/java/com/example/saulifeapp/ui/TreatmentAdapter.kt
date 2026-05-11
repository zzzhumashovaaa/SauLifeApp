package com.example.saulifeapp.ui.treatment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemTreatmentMedicineBinding

class TreatmentAdapter(
    private var medicines: MutableList<TreatmentMedicine>
) : RecyclerView.Adapter<TreatmentAdapter.TreatmentViewHolder>() {

    inner class TreatmentViewHolder(
        private val binding: ItemTreatmentMedicineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: TreatmentMedicine) {
            binding.textMedicineName.text = medicine.name
            binding.textDosage.text = medicine.dosage.ifBlank { "Доза көрсетілмеген" }
            binding.textCategory.text = medicine.category.ifBlank { "Medicine" }
            binding.textQuantity.text = "${medicine.quantity} дана"
            binding.textExpiry.text = medicine.expiryDate.ifBlank { "Көрсетілмеген" }

            binding.textAiWarning.text = when {
                medicine.name.contains("ibuprofen", ignoreCase = true) ||
                        medicine.name.contains("ибупрофен", ignoreCase = true) ->
                    "AI ескертуі: асқазанға әсер етуі мүмкін. Тамақтан кейін қабылдаған дұрыс."

                medicine.name.contains("paracetamol", ignoreCase = true) ||
                        medicine.name.contains("парацетамол", ignoreCase = true) ->
                    "AI ескертуі: дозаны асырмаңыз. Бауырға әсер етуі мүмкін."

                medicine.name.contains("aspirin", ignoreCase = true) ||
                        medicine.name.contains("аспирин", ignoreCase = true) ->
                    "AI ескертуі: қан сұйылту әсері болуы мүмкін."

                else ->
                    "AI ескертуі: дәріні нұсқаулық бойынша қабылдаңыз."
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TreatmentViewHolder {
        val binding = ItemTreatmentMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TreatmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TreatmentViewHolder, position: Int) {
        holder.bind(medicines[position])
    }

    override fun getItemCount(): Int = medicines.size

    fun updateData(newList: List<TreatmentMedicine>) {
        medicines.clear()
        medicines.addAll(newList)
        notifyDataSetChanged()
    }
}
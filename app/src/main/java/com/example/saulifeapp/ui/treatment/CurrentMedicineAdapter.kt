package com.example.saulifeapp.ui.treatment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemCurrentMedicineBinding

class CurrentMedicineAdapter(
    private var medicines: MutableList<TreatmentMedicine>
) : RecyclerView.Adapter<CurrentMedicineAdapter.CurrentMedicineViewHolder>() {

    inner class CurrentMedicineViewHolder(
        private val binding: ItemCurrentMedicineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: TreatmentMedicine) {

            binding.textMedicineName.text = medicine.name
            binding.textDosage.text = medicine.dosage

            binding.textQuantity.text =
                "Қалды: ${medicine.quantity}"

            binding.textTime.text =
                if (medicine.time.isNotBlank())
                    "Келесі: ${medicine.time}"
                else
                    "Уақыты көрсетілмеген"

            binding.textStatus.text =
                if (medicine.isCurrent)
                    "Белсенді"
                else
                    "Аяқталған"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CurrentMedicineViewHolder {

        val binding = ItemCurrentMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CurrentMedicineViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CurrentMedicineViewHolder,
        position: Int
    ) {
        holder.bind(medicines[position])
    }

    override fun getItemCount(): Int = medicines.size

    fun updateData(newList: List<TreatmentMedicine>) {
        medicines.clear()
        medicines.addAll(newList)
        notifyDataSetChanged()
    }
}
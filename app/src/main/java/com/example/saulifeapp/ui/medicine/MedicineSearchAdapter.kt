package com.example.saulifeapp.ui.medicine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemMedicineSearchBinding

class MedicineSearchAdapter(
    private var items: List<Medicine>,
    private val onClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicineSearchAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(
        private val binding: ItemMedicineSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) {
            binding.textMedicineName.text = medicine.name
            binding.textMedicineType.text = "${medicine.type} • ${medicine.dosage}"
            binding.textMedicinePurpose.text = medicine.purpose
            binding.root.setOnClickListener {
                onClick(medicine)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<Medicine>) {
        items = newItems
        notifyDataSetChanged()
    }
}
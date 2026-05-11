package com.example.saulifeapp.pharmacy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemPharmacyResultBinding

class PharmacyResultAdapter(
    private var items: List<PharmacyResult>
) : RecyclerView.Adapter<PharmacyResultAdapter.PharmacyViewHolder>() {

    inner class PharmacyViewHolder(
        private val binding: ItemPharmacyResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PharmacyResult) {
            binding.textMedicineName.text = item.medicineName
            binding.textDosage.text = item.dosage
            binding.textPharmacyName.text = item.pharmacyName
            binding.textAddress.text = item.address
            binding.textPrice.text = "${item.price} ₸"
            binding.textDistance.text = item.distance
            binding.textAvailability.text = if (item.available) "В наличии" else "Нет в наличии"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PharmacyViewHolder {
        val binding = ItemPharmacyResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PharmacyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PharmacyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<PharmacyResult>) {
        items = newItems
        notifyDataSetChanged()
    }
}
package com.example.saulifeapp.ui.pharmacy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.R
import com.example.saulifeapp.data.remote.model.PharmacyOffer

class PharmacyOfferAdapter(
    private val items: MutableList<PharmacyOffer>
) : RecyclerView.Adapter<PharmacyOfferAdapter.OfferViewHolder>() {

    inner class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPharmacyName: TextView = itemView.findViewById(R.id.tvPharmacyName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvHours: TextView = itemView.findViewById(R.id.tvHours)
        val tvProducts: TextView = itemView.findViewById(R.id.tvProducts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pharmacy_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val item = items[position]
        holder.tvPharmacyName.text = item.pharmacyName
        holder.tvAddress.text = if (item.address.isBlank()) "Адрес не указан" else "Адрес: ${item.address}"
        holder.tvPhone.text = if (item.phone.isBlank()) "" else "Телефон: ${item.phone}"
        holder.tvPhone.visibility = if (item.phone.isBlank()) View.GONE else View.VISIBLE
        holder.tvHours.text = if (item.workingHours.isBlank()) "" else "Время работы: ${item.workingHours}"
        holder.tvHours.visibility = if (item.workingHours.isBlank()) View.GONE else View.VISIBLE

        holder.tvProducts.text = if (item.products.isEmpty()) {
            "Нет найденных цен"
        } else {
            item.products.take(5).joinToString("") { "• ${it.title} — ${it.price}" }
        };
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<PharmacyOffer>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

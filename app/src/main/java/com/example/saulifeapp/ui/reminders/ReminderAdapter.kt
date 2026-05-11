package com.example.saulifeapp.ui.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemReminderBinding
import java.util.Locale

class ReminderAdapter(
    private val items: MutableList<ReminderItem>,
    private val onDelete: (ReminderItem) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvMedicineName.text = item.medicineName
        holder.binding.tvDosage.text = if (item.dosage.isBlank()) "Без дозировки" else item.dosage
        holder.binding.tvTime.text = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            item.hour,
            item.minute
        )

        holder.binding.btnDelete.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
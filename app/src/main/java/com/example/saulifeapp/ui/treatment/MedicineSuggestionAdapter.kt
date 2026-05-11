package com.example.saulifeapp.ui.treatment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemMedicineSuggestionBinding

class MedicineSuggestionAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<MedicineSuggestionAdapter.SuggestionViewHolder>() {

    private val items = mutableListOf<String>()

    inner class SuggestionViewHolder(
        private val binding: ItemMedicineSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String) {
            binding.textSuggestion.text = text

            binding.root.setOnClickListener {
                onClick(text)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SuggestionViewHolder {

        val binding = ItemMedicineSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
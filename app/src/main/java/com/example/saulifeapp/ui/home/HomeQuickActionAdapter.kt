package com.example.saulifeapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemQuickActionBinding

class HomeQuickActionAdapter(
    private val items: List<HomeQuickAction>,
    private val onClick: (HomeQuickAction) -> Unit
) : RecyclerView.Adapter<HomeQuickActionAdapter.ActionViewHolder>() {

    inner class ActionViewHolder(
        private val binding: ItemQuickActionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeQuickAction) {
            binding.iconAction.setImageResource(item.iconRes)
            binding.textActionTitle.text = item.title
            binding.textActionSubtitle.text = item.subtitle
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding = ItemQuickActionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

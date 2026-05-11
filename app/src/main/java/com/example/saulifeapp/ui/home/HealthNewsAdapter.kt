package com.example.saulifeapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.databinding.ItemHealthNewsBinding

class HealthNewsAdapter(
    private val items: List<HealthNewsItem>,
    private val onClick: (HealthNewsItem) -> Unit
) : RecyclerView.Adapter<HealthNewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(
        private val binding: ItemHealthNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HealthNewsItem) {
            binding.textNewsTag.text = item.tag
            binding.textNewsTitle.text = item.title
            binding.textNewsDescription.text = item.description
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemHealthNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

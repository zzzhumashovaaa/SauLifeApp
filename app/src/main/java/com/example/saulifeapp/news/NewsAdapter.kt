package com.example.saulifeapp.news

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.NewsArticleActivity
import com.example.saulifeapp.databinding.ItemNewsCardBinding

class NewsAdapter(
    private var items: List<NewsItem>
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(
        private val binding: ItemNewsCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NewsItem) {
            binding.textNewsTitle.text = item.title
            binding.textNewsPreview.text = item.preview

            binding.btnReadNews.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, NewsArticleActivity::class.java)
                intent.putExtra("title", item.title)
                intent.putExtra("content", item.content)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsCardBinding.inflate(
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

    fun updateNews(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
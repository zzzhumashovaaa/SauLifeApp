package com.example.saulifeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.databinding.ActivityNewsArticleBinding

class NewsArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title") ?: "Medical News"
        val content = intent.getStringExtra("content") ?: ""

        binding.textArticleTitle.text = title
        binding.textArticleContent.text = content

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
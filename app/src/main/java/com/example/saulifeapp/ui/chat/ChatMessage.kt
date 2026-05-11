package com.example.saulifeapp.ui.chat

data class ChatMessage(
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
package com.example.saulifeapp.ui.chat

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saulifeapp.R
import com.example.saulifeapp.data.remote.GeminiDirectService
import com.example.saulifeapp.ui.profile.UserProfile
import com.example.saulifeapp.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private val aiService = GeminiDirectService()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var userProfile: UserProfile? = null
    private val chatId = "main"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setupRecycler()
        setupQuickActions()
        setupSend()

        loadProfileAndHistory()
    }

    private fun setupRecycler() {
        adapter = ChatAdapter(messages)
        binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChat.adapter = adapter
    }

    private fun setupQuickActions() {
        binding.chipPregnancy.setOnClickListener {
            sendUserMessage("Можно ли этот препарат беременным?")
        }

        binding.chipAnalog.setOnClickListener {
            sendUserMessage("Подбери аналог препарата")
        }

        binding.chipHowToTake.setOnClickListener {
            sendUserMessage("Как правильно принимать это лекарство?")
        }

        binding.chipFindDrug.setOnClickListener {
            sendUserMessage("Помоги найти подходящий препарат")
        }
    }

    private fun setupSend() {
        binding.btnSend.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()

            if (text.isNotBlank()) {
                binding.editMessage.text?.clear()
                sendUserMessage(text)
            }
        }
    }

    private fun loadProfileAndHistory() {
        val uid = auth.currentUser?.uid ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profileDoc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                userProfile = profileDoc.toObject(UserProfile::class.java)

                val historyDocs = firestore.collection("users")
                    .document(uid)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .await()

                messages.clear()

                for (doc in historyDocs.documents) {
                    val message = doc.toObject(ChatMessage::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }

                if (messages.isEmpty()) {
                    messages.add(
                        ChatMessage(
                            text = "Здравствуйте! Я AI-ассистент SauLife. Я могу помочь разобраться с лекарствами, их приёмом, аналогами и общими вопросами.",
                            isUser = false
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                scrollToBottom()

            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        text = "Не удалось загрузить историю чата.",
                        isUser = false
                    )
                )
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun sendUserMessage(text: String) {
        binding.layoutQuickActions.isVisible = false

        val userMessage = ChatMessage(
            text = text,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )

        messages.add(userMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        scrollToBottom()

        saveMessageToFirestore(userMessage)

        val loadingMessage = ChatMessage(
            text = "AI печатает...",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )

        messages.add(loadingMessage)
        val loadingIndex = messages.lastIndex
        adapter.notifyItemInserted(loadingIndex)
        scrollToBottom()

        viewLifecycleOwner.lifecycleScope.launch {
            val result = aiService.ask(
                message = text,
                profile = userProfile,
                history = messages.filter { it.text != "AI печатает..." }
            )

            if (loadingIndex in messages.indices) {
                messages.removeAt(loadingIndex)
                adapter.notifyItemRemoved(loadingIndex)
            }

            val replyText = result.getOrElse {
                "Не удалось получить ответ AI. Проверьте интернет или API ключ."
            }

            val assistantMessage = ChatMessage(
                text = replyText,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )

            messages.add(assistantMessage)
            adapter.notifyItemInserted(messages.lastIndex)
            scrollToBottom()

            saveMessageToFirestore(assistantMessage)
            updateChatMeta(replyText)
        }
    }

    private fun saveMessageToFirestore(message: ChatMessage) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)

        updateChatMeta(message.text)
    }

    private fun updateChatMeta(lastMessage: String) {
        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "title" to "AI Ассистент SauLife",
            "lastMessage" to lastMessage,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .collection("chats")
            .document(chatId)
            .set(data)
    }

    private fun scrollToBottom() {
        binding.recyclerChat.post {
            if (messages.isNotEmpty()) {
                binding.recyclerChat.scrollToPosition(messages.lastIndex)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
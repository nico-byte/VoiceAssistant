package com.example.voiceassistant

import android.content.Context
import com.aallam.openai.api.BetaOpenAI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.aallam.openai.api.chat.ChatMessage

class ChatHistoryManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("chat_histories", Context.MODE_PRIVATE)
    private val gson = Gson()
    @OptIn(BetaOpenAI::class)
    val chatHistories: MutableMap<String, MutableList<ChatMessage>> = mutableMapOf()

    @OptIn(BetaOpenAI::class)
    fun saveChatHistories() {
        val serializedChatHistories = gson.toJson(chatHistories)
        sharedPreferences.edit().putString("chat_histories", serializedChatHistories).apply()
    }

    @OptIn(BetaOpenAI::class)
    fun loadChatHistories() {
        val serializedChatHistories = sharedPreferences.getString("chat_histories", null)
        if (serializedChatHistories != null) {
            val type = object : TypeToken<MutableMap<String, MutableList<ChatMessage>>>() {}.type
            chatHistories.clear()
            chatHistories.putAll(gson.fromJson(serializedChatHistories, type))
        }
    }

    init {
        loadChatHistories()
    }
}

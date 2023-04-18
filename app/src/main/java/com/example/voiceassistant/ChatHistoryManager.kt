package com.example.voiceassistant

import android.content.Context
import com.aallam.openai.api.BetaOpenAI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.aallam.openai.api.chat.ChatMessage

data class ChatData @OptIn(BetaOpenAI::class) constructor(val chatHistoryName: String, val chatHistory: MutableList<ChatMessage>)

@OptIn(BetaOpenAI::class)
class ChatHistoryManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("chat_histories", Context.MODE_PRIVATE)
    private val gson = Gson()
    @OptIn(BetaOpenAI::class)
    val chatDataList: MutableList<ChatData> = mutableListOf()

    @OptIn(BetaOpenAI::class)
    fun saveChatHistories() {
        val serializedChatDataList = gson.toJson(chatDataList)
        sharedPreferences.edit().putString("chat_data_list", serializedChatDataList).apply()
    }

    @OptIn(BetaOpenAI::class)
    fun loadChatHistories() {
        val serializedChatDataList = sharedPreferences.getString("chat_data_list", null)
        if (serializedChatDataList != null) {
            val type = object : TypeToken<MutableList<ChatData>>() {}.type
            chatDataList.clear()
            chatDataList.addAll(gson.fromJson(serializedChatDataList, type))
        }
    }

    init {
        loadChatHistories()

        // Add a default entry if the chatDataList is empty
        if (chatDataList.isEmpty()) {
            val defaultChatHistory = ChatData(
                chatHistoryName = "Default Chat",
                chatHistory = mutableListOf()
            )
            chatDataList.add(defaultChatHistory)
            saveChatHistories() // Save the updated chatDataList with the default entry
        }
    }
}
package com.example.voiceassistant

import android.content.Context
import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import okio.FileSystem
import okio.Path.Companion.toPath

class Models(chatHistoryManager: ChatHistoryManager, userManager: UserManager,
             context: Context) {
    private val user = userManager.loadUsers()
    private val apiKey = user[0].apiKey
    private val openAI = OpenAI(apiKey)
    private val chatHistoryManager = ChatHistoryManager(context)

    init {
        chatHistoryManager.loadChatHistories()
    }

    @OptIn(BetaOpenAI::class)
    suspend fun whisper(audioFilePath: String): String {
        val transcriptionRequest = TranscriptionRequest(
            audio = FileSource(path = audioFilePath.toPath(), fileSystem = FileSystem.SYSTEM),
            model = ModelId("whisper-1")
        )
        val transcriptionResult = openAI.transcription(transcriptionRequest)
        return transcriptionResult.text
    }

    @OptIn(BetaOpenAI::class)
    private val chatHistories = mutableMapOf<String, MutableList<ChatMessage>>()

    @OptIn(BetaOpenAI::class)
    suspend fun chatGpt(prompt: String, documentId: String): String {
        val chatHistory = chatHistoryManager.chatHistories.getOrPut(documentId) { mutableListOf() }

        val messages = chatHistory + ChatMessage(role = ChatRole.User, content = prompt)
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = messages
        )
        val response = openAI.chatCompletion(chatCompletionRequest)
        val chatMessage = response.choices.single().message!!

        chatHistory.add(chatMessage)
        chatHistoryManager.saveChatHistories()

        return chatMessage.content
    }
}
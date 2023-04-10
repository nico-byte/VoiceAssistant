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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.launchIn
import okio.FileSystem
import okio.Path.Companion.toPath

class Models(chatHistoryManager: ChatHistoryManager, userManager: UserManager,
             private val context: Context) {
    private val user = userManager.loadUsers()
    private val apiKey = user[0].apiKey
    private val openAI = OpenAI(apiKey)

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
    private fun getOrCreateChatHistory(documentId: String): MutableList<ChatMessage> {
        return chatHistories.getOrPut(documentId) {
            mutableListOf(ChatMessage(role = ChatRole.System, content = "You are knowledge!"))
        }
    }

    @OptIn(BetaOpenAI::class)
    suspend fun chatGpt(prompt: String, documentId: String): String {
        val conversationHistory = getOrCreateChatHistory(documentId)

        // Add the user message to the conversation history
        conversationHistory.add(ChatMessage(role = ChatRole.User, content = prompt))

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = conversationHistory
        )
        val chatResponse = openAI.chatCompletion(chatCompletionRequest)
        val chatMessage = chatResponse.choices.first().message

        // Add the AI response to the conversation history
        chatMessage?.let { conversationHistory.add(it) }

        return chatMessage?.content.orEmpty()
    }
}
package com.example.voiceassistant

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.io.FileOutputStream

class Models(chatHistoryManager: ChatHistoryManager, userManager: UserManager,
             context: Context) {
    private val user = userManager.loadUsers()
    private val apiKey = if (user.isNotEmpty()) user[0].apiKey else ""
    private val openAI = if (apiKey.isNotBlank()) OpenAI(apiKey) else null
    private val chatHistoryManager = ChatHistoryManager(context)

    init {
        chatHistoryManager.loadChatHistories()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @OptIn(BetaOpenAI::class)
    suspend fun whisper(isRecording: MutableState<Boolean>, tempAudioFile: File): String {
        val audioSource = MediaRecorder.AudioSource.MIC
        val sampleRateInHz = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

        val audioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat,
            minBufferSize)
        val audioBuffer = ByteArray(minBufferSize)

        // Create a temporary file to store the recorded audio
        val outputStream = withContext(Dispatchers.IO) {
            FileOutputStream(tempAudioFile)
        }

        audioRecord.startRecording()

        val transcriptionBuffer = StringBuilder()

        while (isRecording.value) {
            val bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.size)

            if (bytesRead > 0) {
                // Save the recorded audio to the temporary file
                withContext(Dispatchers.IO) {
                    outputStream.write(audioBuffer, 0, bytesRead)
                }

                val transcription = withContext(Dispatchers.IO) {
                    val transcriptionRequest = TranscriptionRequest(
                        audio = FileSource(
                            path = tempAudioFile.toOkioPath(),
                            fileSystem = FileSystem.SYSTEM
                        ),
                        model = ModelId("whisper-1")
                    )
                    val transcriptionResult = openAI?.transcription(transcriptionRequest)
                    transcriptionResult?.text
                }
                transcriptionBuffer.append(transcription)
            }
        }

        // Close the output stream and delete the temporary file
        withContext(Dispatchers.IO) {
            outputStream.close()
        }
        tempAudioFile.delete()

        audioRecord.stop()
        audioRecord.release()

        return transcriptionBuffer.toString()
    }


    @OptIn(BetaOpenAI::class)
    suspend fun chatGpt(prompt: String, documentId: String): String {
        val chatData = chatHistoryManager.chatDataList.firstOrNull { it.chatHistoryName == documentId }

        // Create a new ChatData entry if it doesn't exist
        if (chatData == null) {
            val newChatData = ChatData(chatHistoryName = documentId, chatHistory = mutableListOf())
            chatHistoryManager.chatDataList.add(newChatData)
            chatHistoryManager.saveChatHistories()
            return chatGpt(prompt, documentId) // Recursive call with the newly created ChatData entry
        }

        val messages = chatData.chatHistory + ChatMessage(role = ChatRole.User, content = prompt)
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = messages
        )
        val response = openAI?.chatCompletion(chatCompletionRequest)
        val chatMessage = response?.choices?.single()?.message!!

        chatData.chatHistory.add(chatMessage)
        chatHistoryManager.saveChatHistories()

        return chatMessage.content
    }
}
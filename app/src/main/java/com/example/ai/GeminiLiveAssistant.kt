package com.example.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "GeminiLiveAssistant"

sealed interface LiveVoiceStatus {
    data object Idle : LiveVoiceStatus
    data object Connecting : LiveVoiceStatus
    data object Active : LiveVoiceStatus
    data class Error(val message: String) : LiveVoiceStatus
}

data class LiveTranscriptLine(
    val isUser: Boolean,
    val text: String
)

@OptIn(PublicPreviewAPI::class)
class GeminiLiveAssistant {

    private var activeSession: LiveSession? = null

    private val _status = MutableStateFlow<LiveVoiceStatus>(LiveVoiceStatus.Idle)
    val status: StateFlow<LiveVoiceStatus> = _status.asStateFlow()

    private val _transcripts = MutableStateFlow<List<LiveTranscriptLine>>(emptyList())
    val transcripts: StateFlow<List<LiveTranscriptLine>> = _transcripts.asStateFlow()

    suspend fun startSession(
        context: Context,
        tripContextSummary: String
    ) {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            _status.value = LiveVoiceStatus.Error(
                "Microphone permission is required for real-time voice conversations."
            )
            return
        }

        if (_status.value is LiveVoiceStatus.Connecting || _status.value is LiveVoiceStatus.Active) {
            return
        }

        _status.value = LiveVoiceStatus.Connecting
        try {
            val systemInstruction = content {
                text(
                    "You are TripBudget Live, a friendly, concise real-time travel voice assistant. " +
                        "Help the traveler with local tips, phrases, transport, food recommendations, " +
                        "and budget advice. Keep spoken responses crisp and conversational (2-3 sentences). " +
                        "Active trip context: $tripContextSummary"
                )
            }

            val liveModel = Firebase.ai.liveModel(
                modelName = "gemini-3.8-live",
                generationConfig = liveGenerationConfig {
                    responseModality = ResponseModality.AUDIO
                    speechConfig = SpeechConfig(voice = Voice("AOEDE"))
                    inputAudioTranscription = AudioTranscriptionConfig()
                    outputAudioTranscription = AudioTranscriptionConfig()
                },
                systemInstruction = systemInstruction
            )

            val session = liveModel.connect()
            activeSession = session
            _status.value = LiveVoiceStatus.Active
            _transcripts.value = listOf(
                LiveTranscriptLine(
                    isUser = false,
                    text = "Connected to Gemini Live! Ask anything about your trip, local phrases, or budget."
                )
            )

            session.startAudioConversation(
                transcriptHandler = { inputTranscript, outputTranscript ->
                    val userText = inputTranscript?.text?.trim()
                    val modelText = outputTranscript?.text?.trim()
                    if (!userText.isNullOrEmpty()) {
                        appendTranscript(isUser = true, text = userText)
                    }
                    if (!modelText.isNullOrEmpty()) {
                        appendTranscript(isUser = false, text = modelText)
                    }
                },
                enableInterruptions = true
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini Live session error", e)
            _status.value = LiveVoiceStatus.Error(
                e.localizedMessage ?: "Failed to start Gemini Live voice session."
            )
        }
    }

    suspend fun sendTextMessage(message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return
        val session = activeSession ?: return
        try {
            appendTranscript(isUser = true, text = trimmed)
            session.send(trimmed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message to LiveSession", e)
            _status.value = LiveVoiceStatus.Error(e.localizedMessage ?: "Failed to send message.")
        }
    }

    suspend fun stopSession() {
        try {
            activeSession?.stopAudioConversation()
            activeSession?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing LiveSession", e)
        } finally {
            activeSession = null
            _status.value = LiveVoiceStatus.Idle
        }
    }

    fun clearError() {
        if (_status.value is LiveVoiceStatus.Error) {
            _status.value = LiveVoiceStatus.Idle
        }
    }

    private fun appendTranscript(isUser: Boolean, text: String) {
        val current = _transcripts.value.toMutableList()
        val last = current.lastOrNull()
        if (last != null && last.isUser == isUser && !last.text.startsWith("Connected to Gemini Live")) {
            val combined = (last.text + " " + text).trim()
            current[current.lastIndex] = last.copy(text = combined)
        } else {
            current += LiveTranscriptLine(isUser = isUser, text = text)
        }
        _transcripts.value = current.takeLast(24)
    }
}

package com.example.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

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
class GeminiLiveAssistant(
    private val onExecuteCommand: (String) -> String = { "Command processed" }
) {

    private var activeSession: LiveSession? = null

    private val _status = MutableStateFlow<LiveVoiceStatus>(LiveVoiceStatus.Idle)
    val status: StateFlow<LiveVoiceStatus> = _status.asStateFlow()

    private val _transcripts = MutableStateFlow<List<LiveTranscriptLine>>(emptyList())
    val transcripts: StateFlow<List<LiveTranscriptLine>> = _transcripts.asStateFlow()

    private val executeCommandDeclaration = FunctionDeclaration(
        name = "run_tripbudget_command",
        description = "Execute a TripBudget app voice command such as opening Explore, My Trips, Budget, or Vault tab, " +
            "switching currency (USD, INR, EUR, GBP), toggling dark/light mode, adding an expense, adding a packing item, or planning a destination.",
        parameters = mapOf(
            "command" to Schema.string(
                description = "Plain-text command to run in the app, e.g. 'open budget', 'switch to INR', 'add expense dinner 500', 'add packing umbrella'"
            )
        )
    )

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
        val systemInstruction = content {
            text(
                "You are TripBudget Live, a friendly, concise real-time travel voice assistant. " +
                    "Help the traveler with local tips, phrases, transport, food recommendations, and budget advice. " +
                    "When the user asks to navigate tabs, switch currency, toggle theme, add an expense, or add a packing item, " +
                    "call run_tripbudget_command with the requested action. Keep spoken replies brief (1-3 sentences). " +
                    "Active trip context: $tripContextSummary"
            )
        }

        val liveModels = listOf(
            "gemini-3.1-flash-live-preview",
            "gemini-2.5-flash-native-audio-preview-12-2025"
        )

        var lastError: Exception? = null
        var connectedSession: LiveSession? = null

        for (modelId in liveModels) {
            try {
                val liveModel = Firebase.ai(backend = GenerativeBackend.googleAI()).liveModel(
                    modelName = modelId,
                    generationConfig = liveGenerationConfig {
                        responseModality = ResponseModality.AUDIO
                        speechConfig = SpeechConfig(voice = Voice("AOEDE"))
                        inputAudioTranscription = AudioTranscriptionConfig()
                        outputAudioTranscription = AudioTranscriptionConfig()
                    },
                    tools = listOf(Tool.functionDeclarations(listOf(executeCommandDeclaration))),
                    systemInstruction = systemInstruction
                )
                connectedSession = liveModel.connect()
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Live model $modelId failed to connect", e)
                lastError = e
            }
        }

        if (connectedSession == null) {
            val detail = lastError?.localizedMessage
                ?: "Unable to connect to Gemini Live. Use Voice Command Mic below for app control."
            _status.value = LiveVoiceStatus.Error(detail)
            return
        }

        try {
            activeSession = connectedSession
            _status.value = LiveVoiceStatus.Active
            addSystemOrTranscriptLine(
                isUser = false,
                text = "Connected to Gemini Live! Speak naturally or give a voice command."
            )

            connectedSession.startAudioConversation(
                functionCallHandler = { functionCall -> handleToolCall(functionCall) },
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
            Log.e(TAG, "Gemini Live audio conversation error", e)
            _status.value = LiveVoiceStatus.Error(
                e.localizedMessage ?: "Live audio stream interrupted."
            )
        }
    }

    private fun handleToolCall(functionCall: FunctionCallPart): FunctionResponsePart {
        return when (functionCall.name) {
            "run_tripbudget_command" -> {
                val commandText = functionCall.args["command"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val outcome = if (commandText.isNotBlank()) {
                    onExecuteCommand(commandText)
                } else {
                    "No command provided."
                }
                addSystemOrTranscriptLine(isUser = false, text = outcome)
                FunctionResponsePart(
                    name = functionCall.name,
                    response = JsonObject(
                        mapOf(
                            "success" to JsonPrimitive(true),
                            "result" to JsonPrimitive(outcome)
                        )
                    ),
                    id = functionCall.id
                )
            }
            else -> FunctionResponsePart(
                name = functionCall.name,
                response = JsonObject(mapOf("error" to JsonPrimitive("Unsupported function"))),
                id = functionCall.id
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

    fun addLocalExchange(userText: String, replyText: String) {
        val updated = _transcripts.value.toMutableList()
        if (userText.isNotBlank()) {
            updated += LiveTranscriptLine(isUser = true, text = userText.trim())
        }
        if (replyText.isNotBlank()) {
            updated += LiveTranscriptLine(isUser = false, text = replyText.trim())
        }
        _transcripts.value = updated.takeLast(24)
    }

    fun clearError() {
        if (_status.value is LiveVoiceStatus.Error) {
            _status.value = LiveVoiceStatus.Idle
        }
    }

    private fun addSystemOrTranscriptLine(isUser: Boolean, text: String) {
        val updated = _transcripts.value.toMutableList()
        updated += LiveTranscriptLine(isUser = isUser, text = text)
        _transcripts.value = updated.takeLast(24)
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

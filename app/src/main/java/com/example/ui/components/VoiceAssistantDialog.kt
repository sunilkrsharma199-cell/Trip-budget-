package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ai.LiveTranscriptLine
import com.example.ai.LiveVoiceStatus
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepOceanTeal
import com.example.ui.theme.VibrantSkyBlue
import com.example.ui.theme.WarmSunsetCoral
import java.util.Locale

private enum class PendingMicAction {
    NONE,
    SPEECH_COMMAND,
    GEMINI_LIVE
}

@Composable
fun VoiceAssistantDialog(
    destinationLabel: String,
    status: LiveVoiceStatus,
    transcripts: List<LiveTranscriptLine>,
    onStartVoiceSession: () -> Unit,
    onStopVoiceSession: () -> Unit,
    onSendText: (String) -> Unit,
    onRunVoiceCommand: (String, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var messageInput by remember { mutableStateOf("") }
    var permissionDeniedNotice by remember { mutableStateOf(false) }
    var isListeningSpeech by remember { mutableStateOf(false) }
    var speechStatusNote by remember { mutableStateOf<String?>(null) }
    var pendingAction by remember { mutableStateOf(PendingMicAction.NONE) }
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context.applicationContext) { code ->
            if (code == TextToSpeech.SUCCESS) {
                instance?.language = Locale.US
                ttsReady = true
            }
        }
        instance
    }

    val speakOut: (String) -> Unit = remember(tts) {
        { reply ->
            if (ttsReady) {
                tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "tripbudget_voice")
            }
        }
    }

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    val activitySpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningSpeech = false
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spoken = matches?.firstOrNull()?.trim()
        if (!spoken.isNullOrEmpty()) {
            speechStatusNote = "Heard: \"$spoken\""
            onRunVoiceCommand(spoken, speakOut)
        } else {
            speechStatusNote = "Did not catch speech. Try again or tap a command chip."
        }
    }

    val startSpeechCommand: () -> Unit = remember(speechRecognizer, context) {
        {
            if (speechRecognizer != null) {
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListeningSpeech = true
                        speechStatusNote = "Listening... speak a command (e.g. 'Open Budget' or 'Switch to INR')"
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListeningSpeech = false
                    }

                    override fun onError(error: Int) {
                        isListeningSpeech = false
                        speechStatusNote = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "No speech detected. Tap Voice Command Mic and speak clearly."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission required."
                            else ->
                                "Speech service unavailable (code $error). Launching voice popup or use text input."
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListeningSpeech = false
                        val spoken = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                        if (!spoken.isNullOrEmpty()) {
                            speechStatusNote = "Heard: \"$spoken\""
                            onRunVoiceCommand(spoken, speakOut)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            speechStatusNote = "Hearing: \"$partial\""
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(
                        RecognizerIntent.EXTRA_PROMPT,
                        "Say e.g. 'Open Budget', 'Switch to INR', 'Add expense Lunch 250'"
                    )
                }
                speechRecognizer.startListening(intent)
            } else {
                // Fallback to system Google Speech activity if inline recognizer isn't bound
                val fallbackIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a TripBudget voice command")
                }
                runCatching {
                    isListeningSpeech = true
                    activitySpeechLauncher.launch(fallbackIntent)
                }.onFailure {
                    isListeningSpeech = false
                    speechStatusNote = "Voice input not supported on this device. Type a command below."
                }
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedNotice = false
            when (pendingAction) {
                PendingMicAction.SPEECH_COMMAND -> startSpeechCommand()
                PendingMicAction.GEMINI_LIVE -> onStartVoiceSession()
                PendingMicAction.NONE -> Unit
            }
        } else {
            permissionDeniedNotice = true
        }
        pendingAction = PendingMicAction.NONE
    }

    val quickCommands = remember(destinationLabel) {
        listOf(
            "Kolkata to Jaipur",
            "Open Budget",
            "Switch to INR",
            "Open Vault",
            "Add packing Umbrella",
            "Plan trip to Goa"
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { speechRecognizer?.destroy() }
            runCatching {
                tts.stop()
                tts.shutdown()
            }
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val isActiveVisual = isListeningSpeech || status is LiveVoiceStatus.Active

    AlertDialog(
        onDismissRequest = {
            runCatching { speechRecognizer?.stopListening() }
            onStopVoiceSession()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = WarmSunsetCoral
                )
                Column {
                    Text(
                        text = "Voice Commands & Live AI",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Speak commands or chat • $destinationLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(if (isActiveVisual) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListeningSpeech -> VibrantSkyBlue
                                        status is LiveVoiceStatus.Active -> WarmSunsetCoral
                                        status is LiveVoiceStatus.Connecting -> MaterialTheme.colorScheme.secondary
                                        else -> DeepOceanTeal
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (status is LiveVoiceStatus.Connecting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Voice status",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        val statusLabel = when {
                            isListeningSpeech -> speechStatusNote ?: "Listening for voice command..."
                            status is LiveVoiceStatus.Connecting -> "Connecting to Gemini Live..."
                            status is LiveVoiceStatus.Active -> "Gemini Live Active — Speak naturally"
                            speechStatusNote != null -> speechStatusNote!!
                            status is LiveVoiceStatus.Error -> status.message
                            else -> "Tap Voice Command Mic to speak commands like 'Open Budget' or 'Switch to INR'"
                        }

                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status is LiveVoiceStatus.Error && !isListeningSpeech && speechStatusNote == null) {
                                DangerRed
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )

                        if (permissionDeniedNotice) {
                            Text(
                                text = "Microphone permission was denied. Enable Microphone in Android App Settings.",
                                style = MaterialTheme.typography.labelSmall,
                                color = DangerRed
                            )
                        }

                        // Primary Button 1: On-Device Voice Command Mic (Works on any Android phone)
                        Button(
                            onClick = {
                                if (isListeningSpeech) {
                                    speechRecognizer?.stopListening()
                                    isListeningSpeech = false
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        permissionDeniedNotice = false
                                        startSpeechCommand()
                                    } else {
                                        pendingAction = PendingMicAction.SPEECH_COMMAND
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListeningSpeech) DangerRed else WarmSunsetCoral,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_command_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isListeningSpeech) Icons.Rounded.Stop else Icons.Rounded.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isListeningSpeech) "Stop Listening" else "Voice Command (Speak)")
                        }

                        // Secondary Button 2: Gemini Live Real-Time Audio Session
                        if (status is LiveVoiceStatus.Active || status is LiveVoiceStatus.Connecting) {
                            OutlinedButton(
                                onClick = onStopVoiceSession,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stop_voice_session_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Gemini Live Audio")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        permissionDeniedNotice = false
                                        onStartVoiceSession()
                                    } else {
                                        pendingAction = PendingMicAction.GEMINI_LIVE
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("start_voice_session_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Gemini Live Audio")
                            }
                        }
                    }
                }

                // Transcript Feed
                if (transcripts.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(transcripts) { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (line.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (line.isUser) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (line.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Voice Command Chips (always clickable)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickCommands) { cmd ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (status is LiveVoiceStatus.Active) {
                                    onSendText(cmd)
                                } else {
                                    onRunVoiceCommand(cmd, speakOut)
                                }
                            },
                            label = {
                                Text(
                                    text = cmd,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Text Command / Prompt Input (always usable)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Command or ask travel AI...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                val text = messageInput
                                messageInput = ""
                                if (status is LiveVoiceStatus.Active) {
                                    onSendText(text)
                                } else {
                                    onRunVoiceCommand(text, speakOut)
                                }
                            }
                        },
                        enabled = messageInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "Send command",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    runCatching { speechRecognizer?.stopListening() }
                    onStopVoiceSession()
                    onDismiss()
                }
            ) {
                Text("Close")
            }
        }
    )
}

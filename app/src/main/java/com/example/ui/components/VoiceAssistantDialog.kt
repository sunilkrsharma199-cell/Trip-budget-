package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.WarmSunsetCoral
import kotlinx.coroutines.launch

@Composable
fun VoiceAssistantDialog(
    destinationLabel: String,
    status: LiveVoiceStatus,
    transcripts: List<LiveTranscriptLine>,
    onStartVoiceSession: () -> Unit,
    onStopVoiceSession: () -> Unit,
    onSendText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messageInput by remember { mutableStateOf("") }
    var permissionDeniedNotice by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedNotice = false
            onStartVoiceSession()
        } else {
            permissionDeniedNotice = true
        }
    }

    val quickPrompts = remember(destinationLabel) {
        listOf(
            "Teach me 3 local phrases in $destinationLabel",
            "Best budget street food in $destinationLabel?",
            "How to avoid tourist scams here?"
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            // Keep session cleanup safe when dialog closes
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

    AlertDialog(
        onDismissRequest = {
            scope.launch { onStopVoiceSession() }
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
                        text = "TripBudget Live Voice",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Gemini 3.8 Live • $destinationLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Voice Control Orb
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
                                .size(68.dp)
                                .scale(if (status is LiveVoiceStatus.Active) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    when (status) {
                                        is LiveVoiceStatus.Active -> WarmSunsetCoral
                                        is LiveVoiceStatus.Connecting -> MaterialTheme.colorScheme.secondary
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
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        val statusLabel = when (status) {
                            is LiveVoiceStatus.Idle -> "Tap Start Voice Chat for real-time audio"
                            is LiveVoiceStatus.Connecting -> "Connecting to Gemini Live..."
                            is LiveVoiceStatus.Active -> "Live Audio Active — Speak naturally"
                            is LiveVoiceStatus.Error -> status.message
                        }

                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status is LiveVoiceStatus.Error) DangerRed
                            else MaterialTheme.colorScheme.onSurface
                        )

                        if (permissionDeniedNotice) {
                            Text(
                                text = "Microphone permission was denied. Enable it in settings to use live voice.",
                                style = MaterialTheme.typography.labelSmall,
                                color = DangerRed
                            )
                        }

                        if (status is LiveVoiceStatus.Active || status is LiveVoiceStatus.Connecting) {
                            OutlinedButton(
                                onClick = onStopVoiceSession,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("stop_voice_session_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Voice Session")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        permissionDeniedNotice = false
                                        onStartVoiceSession()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarmSunsetCoral,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("start_voice_session_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Live Voice Chat")
                            }
                        }
                    }
                }

                // Live Transcript Feed
                if (transcripts.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 170.dp),
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
                                        color = if (line.isUser) Color.White
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (status is LiveVoiceStatus.Active) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickPrompts) { prompt ->
                            FilterChip(
                                selected = false,
                                onClick = { onSendText(prompt) },
                                label = { Text(prompt, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = { Text("Or type to Gemini Live...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (messageInput.isNotBlank()) {
                                    onSendText(messageInput)
                                    messageInput = ""
                                }
                            },
                            enabled = messageInput.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = "Send text to Live session",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onStopVoiceSession()
                    onDismiss()
                }
            ) {
                Text("Close")
            }
        }
    )
}

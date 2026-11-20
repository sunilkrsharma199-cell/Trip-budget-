package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ActivityEntity
import com.example.data.local.BookingEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackingItemEntity
import com.example.data.local.TravelerEntity
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.domain.StoryCardGenerator
import com.example.domain.TripExporter
import com.example.model.AppCurrency
import com.example.ui.theme.WarmSunsetCoral
import kotlinx.coroutines.launch

@Composable
fun ShareTripModal(
    trip: TripEntity,
    days: List<TripDayEntity>,
    activities: List<ActivityEntity>,
    travelers: List<TravelerEntity>,
    expenses: List<ExpenseEntity>,
    bookings: List<BookingEntity>,
    packingItems: List<PackingItemEntity>,
    currency: AppCurrency,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var storyBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Style,
                    contentDescription = null,
                    tint = WarmSunsetCoral,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share & Social Story Card")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create a vertical 9:16 travel story card for ${trip.destinationName} or share your itinerary & expense report.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        scope.launch {
                            isGenerating = true
                            storyBitmap = StoryCardGenerator.generateStoryBitmap(
                                context = context,
                                trip = trip,
                                days = days,
                                activities = activities,
                                currency = currency
                            )
                            isGenerating = false
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmSunsetCoral,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_story_card_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering Story Card...")
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_generate_story_card))
                    }
                }

                val currentBitmap = storyBitmap
                if (currentBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                Image(
                                    bitmap = currentBitmap.asImageBitmap(),
                                    contentDescription = "Generated trip social story card",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val saved = StoryCardGenerator.saveStoryCardToGallery(
                                                context = context,
                                                bitmap = currentBitmap,
                                                tripTitle = trip.title
                                            )
                                            onShowMessage(
                                                if (saved) "Story Card saved to Pictures/TripBudget!"
                                                else "Could not save Story Card to gallery."
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            StoryCardGenerator.shareStoryCard(
                                                context = context,
                                                bitmap = currentBitmap,
                                                tripTitle = trip.title
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share PNG")
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val summary = TripExporter.buildTextSummary(
                                trip = trip,
                                days = days,
                                activities = activities,
                                travelers = travelers,
                                expenses = expenses,
                                bookings = bookings,
                                packingItems = packingItems,
                                currency = currency
                            )
                            TripExporter.shareText(
                                context = context,
                                subject = "${trip.title} — TripBudget Plan",
                                body = summary
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Text Plan")
                    }

                    OutlinedButton(
                        onClick = {
                            val json = TripExporter.buildJsonBackup(
                                trip = trip,
                                days = days,
                                activities = activities,
                                expenses = expenses,
                                bookings = bookings
                            )
                            clipboard.setText(AnnotatedString(json))
                            onShowMessage("JSON backup copied to clipboard!")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy JSON")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

package com.example.domain

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import com.example.data.local.ActivityEntity
import com.example.data.local.TripDayEntity
import com.example.data.local.TripEntity
import com.example.model.AppCurrency
import com.example.model.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object StoryCardGenerator {

    suspend fun generateStoryBitmap(
        context: Context,
        trip: TripEntity,
        days: List<TripDayEntity>,
        activities: List<ActivityEntity>,
        currency: AppCurrency
    ): Bitmap = withContext(Dispatchers.IO) {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Rich ocean-teal to midnight-slate background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    Color.parseColor("#0B1120"),
                    Color.parseColor("#0F766E"),
                    Color.parseColor("#0284C7")
                ),
                floatArrayOf(0f, 0.58f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Destination Hero Photo at top (920px tall)
        val photoBitmap = loadPhotoBitmap(context, trip.imageUrl)
        val photoRect = RectF(48f, 130f, width - 48f, 1020f)
        val clipPath = android.graphics.Path().apply {
            addRoundRect(photoRect, 44f, 44f, android.graphics.Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)
        if (photoBitmap != null) {
            val srcRect = centerCropRect(photoBitmap.width, photoBitmap.height, photoRect.width(), photoRect.height())
            canvas.drawBitmap(photoBitmap, srcRect, photoRect, Paint(Paint.FILTER_BITMAP_FLAG))
        }
        // Dark gradient scrim over photo for sharp white text
        val scrimPaint = Paint().apply {
            shader = LinearGradient(
                0f, photoRect.top, 0f, photoRect.bottom,
                intArrayOf(
                    Color.argb(40, 0, 0, 0),
                    Color.argb(60, 0, 0, 0),
                    Color.argb(220, 11, 17, 32)
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(photoRect, scrimPaint)
        canvas.restore()

        // Photo border stroke
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(75, 255, 255, 255)
        }
        canvas.drawRoundRect(photoRect, 44f, 44f, borderPaint)

        // 3. Top Header Branding Pill
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F97316")
        }
        val brandRect = RectF(48f, 52f, 415f, 106f)
        canvas.drawRoundRect(brandRect, 27f, 27f, pillPaint)

        val brandTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("TRIPBUDGET STORY", 78f, 89f, brandTextPaint)

        val dateBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 255, 255, 255)
            textSize = 28f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("${trip.startDate} – ${trip.endDate}", width - 56f, 89f, dateBadgePaint)

        // 4. Country badge & Trip Title overlaid on bottom of photo
        val countryPill = RectF(84f, 750f, 360f, 804f)
        val countryPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
        }
        canvas.drawRoundRect(countryPill, 27f, 27f, countryPillPaint)
        val countryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(trip.country.uppercase(), 112f, 786f, countryTextPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val displayTitle = if (trip.title.length > 22) trip.title.take(21) + "…" else trip.title
        canvas.drawText(displayTitle, 84f, 886f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#BAE6FD")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("${trip.destinationName} • ${trip.vibe}", 84f, 948f, subtitlePaint)

        // 5. Key Metrics Row (Days | Travelers | Total Budget)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(238, 255, 255, 255)
        }
        val statsRect = RectF(48f, 1060f, width - 48f, 1265f)
        canvas.drawRoundRect(statsRect, 32f, 32f, cardPaint)

        val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val statValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val statBudgetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val colW = statsRect.width() / 3f
        val x1 = statsRect.left + 44f
        val x2 = statsRect.left + colW + 30f
        val x3 = statsRect.left + colW * 2f + 16f

        canvas.drawText("DURATION", x1, 1126f, statLabelPaint)
        canvas.drawText("${days.size.coerceAtLeast(1)} Days", x1, 1192f, statValuePaint)

        canvas.drawText("TRAVELERS", x2, 1126f, statLabelPaint)
        canvas.drawText("${trip.travelerCount} People", x2, 1192f, statValuePaint)

        canvas.drawText("TRIP BUDGET", x3, 1126f, statLabelPaint)
        canvas.drawText(
            CurrencyFormatter.formatUsd(trip.totalBudgetUsd, currency),
            x3,
            1192f,
            statBudgetPaint
        )

        // 6. Top Curated Highlights Card
        val highlightsRect = RectF(48f, 1300f, width - 48f, 1745f)
        val glassDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(175, 15, 23, 42)
        }
        canvas.drawRoundRect(highlightsRect, 32f, 32f, glassDarkPaint)
        canvas.drawRoundRect(highlightsRect, 32f, 32f, borderPaint)

        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FB923C")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.06f
        }
        canvas.drawText("ITINERARY HIGHLIGHTS", 88f, 1364f, sectionPaint)

        val highlightNames = activities.map { it.placeName }.distinct().take(4).ifEmpty {
            listOf("Explore ${trip.destinationName}", "Local Culinary Trail", "Sunset Viewpoint")
        }
        val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2DD4BF")
        }

        highlightNames.forEachIndexed { idx, place ->
            val yPos = 1445f + (idx * 78f)
            canvas.drawCircle(104f, yPos - 11f, 10f, dotPaint)
            val safePlace = if (place.length > 34) place.take(33) + "…" else place
            canvas.drawText(safePlace, 132f, yPos, itemPaint)
        }

        // 7. Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            "Planned & Budgeted with TripBudget",
            width / 2f,
            1835f,
            footerPaint
        )

        bitmap
    }

    private suspend fun loadPhotoBitmap(context: Context, imageUrl: String): Bitmap? {
        return try {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            val drawable = (result as? SuccessResult)?.drawable
            if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.img_hero_travel)
            }
        } catch (_: Exception) {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_hero_travel)
        }
    }

    private fun centerCropRect(srcW: Int, srcH: Int, dstW: Float, dstH: Float): Rect {
        val targetRatio = dstW / dstH
        val srcRatio = srcW.toFloat() / srcH.toFloat()
        return if (srcRatio > targetRatio) {
            val croppedW = (srcH * targetRatio).toInt()
            val left = (srcW - croppedW) / 2
            Rect(left, 0, left + croppedW, srcH)
        } else {
            val croppedH = (srcW / targetRatio).toInt()
            val top = (srcH - croppedH) / 2
            Rect(0, top, srcW, top + croppedH)
        }
    }

    suspend fun saveStoryCardToGallery(
        context: Context,
        bitmap: Bitmap,
        tripTitle: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeName = "TripBudget_${tripTitle.replace(Regex("[^A-Za-z0-9]"), "_")}_${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, safeName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/TripBudget")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext false
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun shareStoryCard(
        context: Context,
        bitmap: Bitmap,
        tripTitle: String
    ): Unit = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "story_cards").apply { mkdirs() }
        val file = File(dir, "tripbudget_story_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        withContext(Dispatchers.Main) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "$tripTitle — Planned with TripBudget")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Trip Story Card"))
        }
    }
}

package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TripBudgetLightColorScheme = lightColorScheme(
    primary = DeepOceanTeal,
    onPrimary = Color.White,
    primaryContainer = TealLightContainer,
    onPrimaryContainer = TealOnLightContainer,
    secondary = VibrantSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = SkyLightContainer,
    onSecondaryContainer = SkyOnLightContainer,
    tertiary = WarmSunsetCoral,
    onTertiary = Color.White,
    tertiaryContainer = CoralLightContainer,
    onTertiaryContainer = CoralOnLightContainer,
    background = CloudSlateBackground,
    onBackground = DeepSlateInk,
    surface = PureWhiteSurface,
    onSurface = DeepSlateInk,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = MutedSlateInk,
    outline = SlateOutline,
    outlineVariant = SlateOutline,
    error = DangerRed,
    onError = Color.White
)

private val TripBudgetDarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = TealDarkContainer,
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = SkyDarkSecondary,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = SkyDarkContainer,
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = CoralDarkTertiary,
    onTertiary = Color(0xFF431407),
    tertiaryContainer = CoralDarkContainer,
    onTertiaryContainer = Color(0xFFFFEDD5),
    background = MidnightSlateBackground,
    onBackground = CrispLightInk,
    surface = ElevatedSlateSurface,
    onSurface = CrispLightInk,
    surfaceVariant = SlateDarkVariant,
    onSurfaceVariant = MutedLightInk,
    outline = SlateDarkOutline,
    outlineVariant = SlateDarkOutline,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val TripBudgetShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TripBudgetTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TripBudgetDarkColorScheme else TripBudgetLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TripBudgetTypography,
        shapes = TripBudgetShapes,
        content = content
    )
}

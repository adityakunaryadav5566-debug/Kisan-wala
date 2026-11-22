package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val KisanEmeraldColorScheme = darkColorScheme(
    primary = FreshGreen,
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldElevated,
    onPrimaryContainer = FreshGreenSoft,
    secondary = HarvestGoldBright,
    onSecondary = EmeraldDeep,
    secondaryContainer = WarningAmberBg,
    onSecondaryContainer = HarvestGoldSoft,
    tertiary = FreshGreenBright,
    onTertiary = EmeraldDeep,
    background = EmeraldDeep,
    onBackground = SoftMintWhite,
    surface = EmeraldNight,
    onSurface = SoftMintWhite,
    surfaceVariant = EmeraldCard,
    onSurfaceVariant = MutedSage,
    outline = EmeraldBorder,
    outlineVariant = EmeraldElevated,
    error = AlertRed,
    onError = PureWhite,
    errorContainer = AlertRedBg,
    onErrorContainer = ColorTokens.ErrorSoft
)

private object ColorTokens {
    val ErrorSoft = androidx.compose.ui.graphics.Color(0xFFFCA5A5)
}

val KisanShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun KisanPlantAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KisanEmeraldColorScheme,
        typography = KisanTypography,
        shapes = KisanShapes,
        content = content
    )
}

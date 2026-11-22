package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CropReportEntity
import com.example.data.model.ReportType
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.HarvestGoldSoft
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarningAmberBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FirebaseRequiredNoticeBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = WarningAmberBg.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Sample AI Notice",
                tint = HarvestGoldBright,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sample_ai_notice_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = HarvestGoldBright
                )
                Text(
                    text = stringResource(R.string.sample_ai_notice_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HarvestGoldSoft
                )
            }
        }
    }
}

@Composable
fun TopScreenHeader(
    titleHindi: String,
    subtitleEnglish: String,
    onBackClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            Surface(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick)
                    .testTag("back_button"),
                color = EmeraldCard,
                border = BorderStroke(1.dp, EmeraldBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "वापस जाएं",
                        tint = PureWhite
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleHindi,
                style = MaterialTheme.typography.headlineSmall,
                color = PureWhite
            )
            Text(
                text = subtitleEnglish,
                style = MaterialTheme.typography.labelMedium,
                color = FreshGreenBright
            )
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
fun KisanGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = EmeraldBorder,
    containerColor: Color = EmeraldCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlowingEmeraldButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isGoldAccent: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "glow_button")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val containerColors = if (isGoldAccent) {
        listOf(HarvestGoldBright, HarvestGold)
    } else {
        listOf(FreshGreenBright, FreshGreen)
    }
    val glowShadowColor = if (isGoldAccent) HarvestGoldBright else FreshGreenBright

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = glowShadowColor.copy(alpha = glowAlpha),
                spotColor = glowShadowColor.copy(alpha = glowAlpha)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = EmeraldDeep,
            disabledContainerColor = EmeraldElevated,
            disabledContentColor = MutedSage
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(containerColors)
                    } else {
                        Brush.horizontalGradient(listOf(EmeraldElevated, EmeraldElevated))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun CropQuickSelectorRow(
    selectedCrop: String,
    onCropSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val crops = listOf(
        "गेहूँ (Wheat)",
        "धान (Rice)",
        "मक्का (Maize)",
        "टमाटर (Tomato)",
        "आलू (Potato)",
        "सरसों (Mustard)",
        "गन्ना (Sugarcane)"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        crops.forEach { crop ->
            val selected = selectedCrop.equals(crop, ignoreCase = true)
            FilterChip(
                selected = selected,
                onClick = { onCropSelected(crop) },
                label = {
                    Text(
                        text = crop,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = EmeraldNightCompat.CardBg,
                    labelColor = FreshGreenSoft,
                    selectedContainerColor = FreshGreen,
                    selectedLabelColor = EmeraldDeep,
                    selectedLeadingIconColor = EmeraldDeep
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = EmeraldBorder,
                    selectedBorderColor = FreshGreenBright
                )
            )
        }
    }
}

private object EmeraldNightCompat {
    val CardBg = Color(0xFF07261B)
}

@Composable
fun ReportDetailDialog(
    report: CropReportEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val type = ReportType.fromName(report.reportType)
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        .format(Date(report.createdAtMillis))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EmeraldCard,
        titleContentColor = PureWhite,
        textContentColor = FreshGreenSoft,
        icon = {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = HarvestGoldBright
            )
        },
        title = {
            Column {
                Text(
                    text = "${type.labelHindi} • ${report.cropName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = HarvestGoldBright
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.titleHindi,
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSage
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = report.detailsHindi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.92f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें", color = FreshGreenBright)
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("हटाएं", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

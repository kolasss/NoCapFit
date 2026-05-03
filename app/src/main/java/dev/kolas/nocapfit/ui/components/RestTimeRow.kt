package dev.kolas.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kolas.nocapfit.util.SECONDS_PER_MINUTE
import dev.kolas.nocapfit.util.ceilSecondsFromMs
import dev.kolas.nocapfit.util.formatMmSs
import dev.kolas.nocapfit.util.restTimerFillProgress

/**
 * Parse mm:ss digit string to total seconds.
 * "130" → 90s (1 min 30 sec), "634" → 394s (6 min 34 sec), "60" → 60s
 */
fun parseMmSsToSeconds(digits: String): Int {
    if (digits.isEmpty()) return 0
    return if (digits.length <= 2) {
        digits.toIntOrNull() ?: 0
    } else {
        val seconds = digits.takeLast(2).toIntOrNull() ?: 0
        val minutes = digits.dropLast(2).toIntOrNull() ?: 0
        minutes * SECONDS_PER_MINUTE + seconds
    }
}

/**
 * Convert total seconds to raw digit string for mm:ss input.
 * 90 → "130", 394 → "634", 60 → "100"
 */
fun secondsToMmSsDigits(totalSeconds: Int): String {
    if (totalSeconds <= 0) return ""
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return if (minutes == 0) {
        seconds.toString()
    } else {
        "$minutes${seconds.toString().padStart(2, '0')}"
    }
}

@Composable
fun RestTimeRow(
    restTimeSeconds: Int,
    onRestTimeChange: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    isTimerActive: Boolean = false,
    timerEndAtEpochMs: Long = 0L,
    timerTotalMs: Long = 0L,
    isCompleted: Boolean = false,
    onCancelTimer: (() -> Unit)? = null,
    contentHorizontalPadding: Dp = 8.dp
) {
    val remainingMs = rememberTimerRemainingMs(isTimerActive, timerEndAtEpochMs)
    val fillProgress = if (isTimerActive) {
        restTimerFillProgress(remainingMs, timerTotalMs)
    } else {
        0f
    }

    val completedColor = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val progressColor = MaterialTheme.colorScheme.tertiaryContainer

    var rowWindowLeft by remember { mutableFloatStateOf(0f) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val positionTracker = if (isTimerActive) {
        Modifier.onGloballyPositioned { coords ->
            rowWindowLeft = coords.positionInWindow().x
            rowWidthPx = coords.size.width
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .then(positionTracker)
            .drawBehind {
                drawRect(
                    if (isCompleted && !isTimerActive) completedColor else surfaceColor
                )
                if (fillProgress > 0f) {
                    drawRect(
                        color = progressColor,
                        size = Size(size.width * fillProgress, size.height)
                    )
                }
            }
    ) {
        val accentTint = when {
            isTimerActive -> MaterialTheme.colorScheme.tertiary
            isCompleted -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        if (isTimerActive) {
            ActiveFillCenterContent(
                timerEndAtEpochMs = timerEndAtEpochMs,
                remainingMs = remainingMs,
                fillProgress = fillProgress,
                rowWindowLeft = rowWindowLeft,
                rowWidthPx = rowWidthPx,
                accentTint = accentTint,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            StaticCenterContent(
                isCompleted = isCompleted,
                onRestTimeChange = onRestTimeChange,
                restTimeSeconds = restTimeSeconds,
                accentTint = accentTint,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (isTimerActive && onCancelTimer != null) {
            TextButton(
                onClick = onCancelTimer,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = contentHorizontalPadding)
            ) {
                Text("Skip")
            }
        }
    }
}

@Composable
private fun StaticCenterContent(
    isCompleted: Boolean,
    onRestTimeChange: ((Int) -> Unit)?,
    restTimeSeconds: Int,
    accentTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = "Rest time",
            tint = accentTint,
            modifier = Modifier.size(18.dp)
        )
        if (!isCompleted && onRestTimeChange != null) {
            RestTimeInput(
                restTimeSeconds = restTimeSeconds,
                onRestTimeChange = onRestTimeChange,
                modifier = Modifier.width(60.dp)
            )
        } else {
            Text(
                text = formatMmSs(restTimeSeconds),
                style = MaterialTheme.typography.bodyMedium,
                color = accentTint
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ActiveFillCenterContent(
    timerEndAtEpochMs: Long,
    remainingMs: Long,
    fillProgress: Float,
    rowWindowLeft: Float,
    rowWidthPx: Int,
    accentTint: Color,
    modifier: Modifier = Modifier
) {
    val coveredColor = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current
    val transitionHalfPx = remember(density) { with(density) { 4.dp.toPx() } }

    var iconWindowLeft by remember { mutableFloatStateOf(0f) }
    var iconWidthPx by remember { mutableIntStateOf(0) }

    val fillParams = FillOverlayParams(
        fillProgress = fillProgress,
        rowWindowLeft = rowWindowLeft,
        rowWidthPx = rowWidthPx,
        coveredColor = coveredColor,
        transitionHalfPx = transitionHalfPx
    )
    val iconBrush = remember(fillParams, iconWindowLeft, iconWidthPx, accentTint) {
        buildFillGradientBrush(
            params = fillParams,
            childWindowLeft = iconWindowLeft,
            childWidthPx = iconWidthPx,
            originalColor = accentTint
        )
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = "Rest time",
            tint = accentTint,
            modifier = Modifier
                .size(18.dp)
                .onGloballyPositioned { iconWindowLeft = it.positionInWindow().x }
                .onSizeChanged { iconWidthPx = it.width }
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(brush = iconBrush, blendMode = BlendMode.SrcAtop)
                }
        )
        if (timerEndAtEpochMs > 0) {
            RestTimerCountdown(
                remainingMs = remainingMs,
                color = accentTint,
                fillParams = fillParams
            )
        }
    }
}

@Composable
fun RestTimeInput(
    restTimeSeconds: Int,
    onRestTimeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var digits by remember { mutableStateOf(secondsToMmSsDigits(restTimeSeconds)) }
    var lastExternalSeconds by remember { mutableIntStateOf(restTimeSeconds) }
    var lastSentSeconds by remember { mutableIntStateOf(restTimeSeconds) }
    if (restTimeSeconds != lastExternalSeconds) {
        lastExternalSeconds = restTimeSeconds
        if (restTimeSeconds != lastSentSeconds) {
            digits = secondsToMmSsDigits(restTimeSeconds)
            lastSentSeconds = restTimeSeconds
        }
    }
    CompactInput(
        value = digits,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(4)
            digits = filtered
            val seconds = parseMmSsToSeconds(filtered)
            lastSentSeconds = seconds
            onRestTimeChange(seconds)
        },
        keyboardType = KeyboardType.Number,
        visualTransformation = mmSsTransformation,
        modifier = modifier
    )
}

@Composable
private fun RestTimerCountdown(
    remainingMs: Long,
    color: Color,
    fillParams: FillOverlayParams
) {
    var textWindowLeft by remember { mutableFloatStateOf(0f) }
    var textWidthPx by remember { mutableIntStateOf(0) }
    val brush = remember(fillParams, textWindowLeft, textWidthPx, color) {
        buildFillGradientBrush(
            params = fillParams,
            childWindowLeft = textWindowLeft,
            childWidthPx = textWidthPx,
            originalColor = color
        )
    }
    Text(
        text = formatMmSs(ceilSecondsFromMs(remainingMs)),
        style = MaterialTheme.typography.bodyLarge.copy(brush = brush),
        modifier = Modifier
            .onGloballyPositioned { textWindowLeft = it.positionInWindow().x }
            .onSizeChanged { textWidthPx = it.width }
    )
}

private val mmSsTransformation = MmSsVisualTransformation()

private data class FillOverlayParams(
    val fillProgress: Float,
    val rowWindowLeft: Float,
    val rowWidthPx: Int,
    val coveredColor: Color,
    val transitionHalfPx: Float
)

private fun buildFillGradientBrush(
    params: FillOverlayParams,
    childWindowLeft: Float,
    childWidthPx: Int,
    originalColor: Color
): Brush {
    if (params.fillProgress <= 0f || childWidthPx <= 0 || params.rowWidthPx <= 0) {
        return SolidColor(originalColor)
    }
    val fillEndInWindow = params.rowWindowLeft + params.rowWidthPx * params.fillProgress
    val fillEndLocal = fillEndInWindow - childWindowLeft
    val halfFraction = params.transitionHalfPx / childWidthPx
    val fraction = fillEndLocal / childWidthPx
    return when {
        fraction + halfFraction <= 0f -> SolidColor(originalColor)
        fraction - halfFraction >= 1f -> SolidColor(params.coveredColor)
        else -> Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to params.coveredColor,
                (fraction - halfFraction).coerceIn(0f, 1f) to params.coveredColor,
                (fraction + halfFraction).coerceIn(0f, 1f) to originalColor,
                1f to originalColor
            )
        )
    }
}

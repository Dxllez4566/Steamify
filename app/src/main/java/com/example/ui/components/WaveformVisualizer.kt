package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpotifyGreen
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    baseColor: Color = SpotifyGreen,
    accentColor: Color = NeonCyan,
    height: Dp = 48.dp,
    progress: Float = 0f // 0f to 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(3f)
        val spacing = barWidth * 0.5f

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            val x = i * (barWidth + spacing)

            // Calculate animated height
            val wave = if (isPlaying) {
                val sinVal = (sin((normalizedIndex * 5f + phase).toDouble()).toFloat() + 1f) / 2f
                val cosVal = (kotlin.math.cos((normalizedIndex * 3f - phase * 1.5f).toDouble()).toFloat() + 1f) / 2f
                (sinVal * 0.6f + cosVal * 0.4f).coerceIn(0.15f, 1f)
            } else {
                // Static natural waveform profile
                (sin((normalizedIndex * 3.14f).toDouble()).toFloat() * 0.7f + 0.2f).coerceIn(0.15f, 0.9f)
            }

            val barHeight = canvasHeight * wave * 0.85f
            val top = (canvasHeight - barHeight) / 2f

            val isPassed = normalizedIndex <= progress
            val activeBrush = if (isPassed) {
                Brush.verticalGradient(
                    colors = listOf(accentColor, baseColor)
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(Color.Gray.copy(alpha = 0.5f), Color.DarkGray.copy(alpha = 0.3f))
                )
            }

            drawRoundRect(
                brush = activeBrush,
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

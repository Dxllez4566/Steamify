package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyStreamPoint
import com.example.model.RetentionPoint
import com.example.model.TrafficSource
import com.example.ui.theme.*

@Composable
fun StreamVelocityChart(
    streamHistory: List<DailyStreamPoint>,
    modifier: Modifier = Modifier
) {
    if (streamHistory.isEmpty()) return
    val maxStreams = streamHistory.maxOf { it.streams }.coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spotify Stream Velocity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "Past 7 days stream acceleration",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary
                    )
                }
                Text(
                    text = "+46% this week",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpotifyGreenBright,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas Chart with bars and smooth line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val width = size.width
                val height = size.height
                val barCount = streamHistory.size
                val itemWidth = width / barCount
                val barWidth = itemWidth * 0.45f

                // Draw background grid lines
                for (step in 1..3) {
                    val y = height * (step / 4f)
                    drawLine(
                        color = StudioBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                val linePath = Path()
                val linePoints = mutableListOf<Offset>()

                // Draw Bars
                streamHistory.forEachIndexed { index, point ->
                    val fraction = point.streams.toFloat() / maxStreams
                    val barHeight = height * fraction * 0.85f
                    val x = index * itemWidth + (itemWidth - barWidth) / 2f
                    val y = height - barHeight

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(SpotifyGreenBright, SpotifyGreen.copy(alpha = 0.3f))
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    linePoints.add(Offset(x + barWidth / 2, y))
                }

                // Connect with glowing line
                if (linePoints.isNotEmpty()) {
                    linePath.moveTo(linePoints.first().x, linePoints.first().y)
                    for (i in 1 until linePoints.size) {
                        val prev = linePoints[i - 1]
                        val curr = linePoints[i]
                        val midX = (prev.x + curr.x) / 2
                        linePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }

                    drawPath(
                        path = linePath,
                        color = NeonCyan,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    linePoints.forEach { pt ->
                        drawCircle(color = StudioDarkBg, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = NeonCyan, radius = 3.dp.toPx(), center = pt)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                streamHistory.forEach { point ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = point.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGraySecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${point.streams}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RetentionDropOffChart(
    retentionCurve: List<RetentionPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Listener Retention Curve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "Crucial 30s stream verification zone",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SpotifyGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "88% at 30s",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpotifyGreenBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val width = size.width
                val height = size.height

                // Draw 30-sec threshold indicator line
                val thresholdX = width * 0.25f // Representing 30s on ~2min track
                drawLine(
                    color = AmberOrange.copy(alpha = 0.7f),
                    start = Offset(thresholdX, 0f),
                    end = Offset(thresholdX, height),
                    strokeWidth = 2.dp.toPx()
                )

                val path = Path()
                val areaPath = Path()

                val points = retentionCurve.mapIndexed { index, pt ->
                    val x = (index.toFloat() / (retentionCurve.size - 1)) * width
                    val y = height * (1f - (pt.retentionPercent / 100f))
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    areaPath.moveTo(points.first().x, height)
                    areaPath.lineTo(points.first().x, points.first().y)

                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2
                        path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        areaPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }

                    areaPath.lineTo(width, height)
                    areaPath.close()

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(SpotifyGreen.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = path,
                        color = SpotifyGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0:00 Intro", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("0:30 (Spotify Count)", style = MaterialTheme.typography.labelSmall, color = AmberOrange, fontWeight = FontWeight.Bold)
                Text("1:00 Chorus", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("3:00 Outro", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun TrafficSourceBreakdown(
    sources: List<TrafficSource>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Organic Stream Sources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )
            Text(
                text = "Algorithm vs community exchange impact",
                style = MaterialTheme.typography.bodySmall,
                color = TextGraySecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented horizontal progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                sources.forEachIndexed { index, source ->
                    val shape = when {
                        index == 0 -> RoundedCornerShape(topStart = 7.dp, bottomStart = 7.dp)
                        index == sources.size - 1 -> RoundedCornerShape(topEnd = 7.dp, bottomEnd = 7.dp)
                        else -> RoundedCornerShape(0.dp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(source.percent.coerceAtLeast(1f))
                            .fillMaxHeight()
                            .background(Color(source.colorHex), shape = shape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEach { source ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(source.colorHex), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = source.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhitePrimary
                            )
                        }
                        Text(
                            text = "${source.percent.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                    }
                }
            }
        }
    }
}

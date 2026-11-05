package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcousticFingerprint
import com.example.ui.theme.*

@Composable
fun AcousticFingerprintCard(
    fingerprint: AcousticFingerprint,
    algorithmScore: Int,
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
                        text = "Spotify Audio Fingerprint",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "Acoustic features analyzed for Spotify algorithm",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary
                    )
                }

                // Algorithm Score Circular Badge
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = StudioBorder,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawArc(
                            color = SpotifyGreenBright,
                            startAngle = -90f,
                            sweepAngle = 360f * (algorithmScore / 100f),
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$algorithmScore",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = SpotifyGreenBright
                        )
                        Text(
                            text = "INDEX",
                            fontSize = 7.sp,
                            color = TextGraySecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key / BPM / Loudness Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "BPM",
                    value = "${fingerprint.bpm}",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "KEY",
                    value = fingerprint.musicalKey,
                    color = ElectricPurple,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "LOUDNESS",
                    value = "${fingerprint.loudnessLufs} LUFS",
                    color = AmberOrange,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bars for Energy, Danceability, Valence, Acousticness
            AcousticBarItem(
                label = "Energy",
                score = fingerprint.energyScore,
                color = CoralRed
            )
            AcousticBarItem(
                label = "Danceability",
                score = fingerprint.danceabilityScore,
                color = SpotifyGreenBright
            )
            AcousticBarItem(
                label = "Mood Valence",
                score = fingerprint.valenceScore,
                color = GoldYellow
            )
            AcousticBarItem(
                label = "Acousticness",
                score = fingerprint.acousticnessScore,
                color = NeonCyan
            )
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )
        }
    }
}

@Composable
private fun AcousticBarItem(
    label: String,
    score: Int,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextWhitePrimary
            )
            Text(
                text = "$score%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = StudioBorder
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioStem
import com.example.model.StemType
import com.example.ui.theme.*

@Composable
fun StemMixerStrip(
    stem: AudioStem,
    isPlaying: Boolean,
    isMuted: Boolean,
    isSolo: Boolean,
    isAudible: Boolean,
    volume: Float,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val stemColor = Color(stem.stemType.badgeColorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAudible && isPlaying) StudioCardElevated else StudioCardSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSolo) {
            androidx.compose.foundation.BorderStroke(1.5.dp, GoldYellow)
        } else if (isAudible && isPlaying) {
            androidx.compose.foundation.BorderStroke(1.dp, stemColor.copy(alpha = 0.6f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Stem Type Icon, Name, Key & BPM
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(stemColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (stem.stemType) {
                            StemType.VOCALS -> Icons.Default.Mic
                            StemType.DRUMS -> Icons.Default.Speed
                            StemType.BASS -> Icons.Default.GraphicEq
                            StemType.SYNTH_LEAD -> Icons.Default.MusicNote
                            StemType.GUITAR -> Icons.Default.MusicNote
                            StemType.FX_PERC -> Icons.Default.GraphicEq
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = stem.stemType.displayName,
                            tint = stemColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stem.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAudible) TextWhitePrimary else TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stem.stemType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = stemColor
                            )
                            Text(
                                text = " • ${stem.musicalKey} • ${stem.bpm} BPM",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGraySecondary
                            )
                        }
                    }
                }

                // Controls: Solo & Mute Studio Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Solo Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSolo) GoldYellow else StudioBorder.copy(alpha = 0.5f))
                            .clickable { onToggleSolo() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (isSolo) StudioBlack else TextGraySecondary
                        )
                    }

                    // Mute Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMuted) CoralRed else StudioBorder.copy(alpha = 0.5f))
                            .clickable { onToggleMute() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (isMuted) StudioBlack else TextGraySecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Waveform meter & volume slider row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live mini waveform
                Box(
                    modifier = Modifier
                        .weight(0.55f)
                        .height(26.dp)
                ) {
                    WaveformVisualizer(
                        isPlaying = isPlaying && isAudible,
                        barCount = 18,
                        baseColor = if (isAudible) stemColor else TextMuted,
                        accentColor = if (isAudible) NeonCyan else Color.DarkGray,
                        height = 26.dp,
                        progress = if (isPlaying && isAudible) 0.8f else 0.2f
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Volume slider
                Row(
                    modifier = Modifier.weight(0.45f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isAudible) stemColor else TextMuted,
                            activeTrackColor = if (isAudible) stemColor else StudioBorder,
                            inactiveTrackColor = StudioBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAudible) TextWhitePrimary else TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

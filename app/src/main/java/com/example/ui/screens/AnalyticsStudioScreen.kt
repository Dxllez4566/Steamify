package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StemSpireRepository
import com.example.model.ExchangeTrack
import com.example.model.TrackAnalytics
import com.example.ui.components.AcousticFingerprintCard
import com.example.ui.components.CreditSparklesPill
import com.example.ui.components.RetentionDropOffChart
import com.example.ui.components.StreamVelocityChart
import com.example.ui.components.TrafficSourceBreakdown
import com.example.ui.theme.*

@Composable
fun AnalyticsStudioScreen(
    repository: StemSpireRepository,
    tracks: List<ExchangeTrack>,
    userCredits: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedTrackId by repository.selectedAnalyticsTrackId.collectAsState()
    val analytics = remember(selectedTrackId) {
        repository.getAnalyticsForTrack(selectedTrackId)
    }

    var editorialTargetPlaylist by remember { mutableStateOf("Night Drive") }
    val playlistOptions = listOf("Night Drive", "Chill Pop", "Beast Mode", "New Music Friday", "Indie Arrivals")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Track",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = TextWhitePrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ANALYTICS",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = SpotifyGreenBright
                        )
                    }
                    Text(
                        text = "Organic algorithm metrics & acoustic intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary
                    )
                }

                CreditSparklesPill(credits = userCredits)
            }
        }

        // Track Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(tracks) { track ->
                    val isSelected = track.id == selectedTrackId
                    FilterChip(
                        selected = isSelected,
                        onClick = { repository.selectAnalyticsTrack(track.id) },
                        label = { Text(track.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpotifyGreen,
                            selectedLabelColor = StudioBlack,
                            containerColor = StudioCardSurface,
                            labelColor = TextWhitePrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SpotifyGreen else StudioBorder
                        )
                    )
                }
            }
        }

        // Key Metric KPIs Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "TOTAL STREAMS",
                        value = "${analytics.totalStreams}",
                        subtext = "+2,410 from exchange",
                        color = SpotifyGreenBright,
                        icon = Icons.Default.GraphicEq,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "MONTHLY LISTENERS",
                        value = "${analytics.monthlyListeners}",
                        subtext = "+18% this month",
                        color = NeonCyan,
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "SPOTIFY SAVE RATE",
                        value = "${analytics.saveRatePercent}%",
                        subtext = "Trigger benchmark: >10%",
                        color = GoldYellow,
                        icon = Icons.Default.BookmarkBorder,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "30s SKIP RATE",
                        value = "${analytics.skipRate30sPercent}%",
                        subtext = "Healthy threshold: <20%",
                        color = CoralRed,
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Spotify Algorithmic Readiness Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                                text = "Spotify Algorithm Index",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Probability of triggering algorithmic radio",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = SpotifyGreenBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AlgorithmMeter(
                            name = "Discover Weekly",
                            score = analytics.discoverWeeklyScore,
                            color = SpotifyGreenBright,
                            modifier = Modifier.weight(1f)
                        )
                        AlgorithmMeter(
                            name = "Release Radar",
                            score = analytics.releaseRadarScore,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Recommendations
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCardElevated, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = GoldYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ORGANIC GROWTH ADVICE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldYellow
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Save rate (${analytics.saveRatePercent}%) is above the 10% Spotify threshold! This indicates listener passion.\n• Completion rate of ${analytics.completionRatePercent}% guarantees favorable editorial playlist test ranking.\n• Continue exchanging plays and stem collabs to keep daily listener velocity high.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhitePrimary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Stream Velocity Canvas Chart
        item {
            StreamVelocityChart(streamHistory = analytics.streamHistory)
        }

        // Listener Retention Curve Chart
        item {
            RetentionDropOffChart(retentionCurve = analytics.retentionCurve)
        }

        // Organic Traffic Sources Breakdown
        item {
            TrafficSourceBreakdown(sources = analytics.listenerSources)
        }

        // Acoustic Fingerprint Radar & LUFS
        item {
            AcousticFingerprintCard(
                fingerprint = analytics.acousticFingerprint,
                algorithmScore = analytics.discoverWeeklyScore
            )
        }

        // Spotify for Artists Editorial Pitch Generator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                                text = "Spotify Editorial Pitch Generator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Formatted for Spotify for Artists editors",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Target Editorial Playlist:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(playlistOptions) { playlist ->
                            val isSel = editorialTargetPlaylist == playlist
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElectricPurple else StudioCardElevated)
                                    .clickable { editorialTargetPlaylist = playlist }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = playlist,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) StudioBlack else TextWhitePrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val generatedPitch = remember(analytics.trackTitle, editorialTargetPlaylist) {
                        "Pitch for '${analytics.trackTitle}' targeting '$editorialTargetPlaylist':\n\n" +
                        "Hi Spotify Editorial Team, '${analytics.trackTitle}' is a fresh ${analytics.acousticFingerprint.bpm} BPM track in ${analytics.acousticFingerprint.musicalKey} blending " +
                        "${if (analytics.acousticFingerprint.energyScore > 75) "driving high-energy synths" else "intimate lush melodies"} with punchy modern master (-${analytics.acousticFingerprint.loudnessLufs} LUFS). " +
                        "In early community testing, it achieved a stellar ${analytics.saveRatePercent}% save rate and 88% 30-second retention. " +
                        "Perfect fit for the mood, tempo, and vibe of '$editorialTargetPlaylist'."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioDarkBg, RoundedCornerShape(10.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = generatedPitch,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhitePrimary,
                            lineHeight = 17.sp,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Spotify Editorial Pitch", generatedPitch)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 Pitch copied! Paste in Spotify for Artists.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = StudioBlack
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Pitch for Spotify for Artists", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGraySecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = TextWhitePrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AlgorithmMeter(
    name: String,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(StudioCardElevated, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, style = MaterialTheme.typography.labelMedium, color = TextWhitePrimary)
                Text("$score%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = StudioBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (score > 85) "High Algorithmic Affinity" else "Building Momentum",
                fontSize = 9.sp,
                color = TextGraySecondary
            )
        }
    }
}

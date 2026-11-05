package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.ExchangeTrack
import com.example.model.ReviewFeedback
import com.example.ui.components.CreditSparklesPill
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.data.ExchangeResult

@Composable
fun ExchangeHubScreen(
    tracks: List<ExchangeTrack>,
    userCredits: Int,
    dailyPlaysUsed: Int,
    dailyPlaysLimit: Int,
    dailyFollowersUsed: Int,
    dailyFollowersLimit: Int,
    dailyInstagramFollowersUsed: Int,
    dailyInstagramFollowersLimit: Int,
    onCompleteReview: (String, Float, Float, Float, Float, String, Boolean, Boolean) -> ExchangeResult,
    onSubmitTrack: (String, String, String, Int, String, Int, Int, String) -> Boolean,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedGenre by remember { mutableStateOf("All") }
    val genres = com.example.model.MusicGenres.POPULAR_GENRES

    var activeExchangeTrack by remember { mutableStateOf<ExchangeTrack?>(null) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    val filteredTracks = remember(tracks, selectedGenre) {
        if (selectedGenre == "All") tracks
        else tracks.filter { track ->
            track.genre.equals(selectedGenre, ignoreCase = true) ||
            track.genre.contains(selectedGenre, ignoreCase = true) ||
            (selectedGenre == "EDM" && (track.genre.contains("EDM", ignoreCase = true) || track.genre.contains("Dubstep", ignoreCase = true) || track.genre.contains("House", ignoreCase = true))) ||
            (selectedGenre == "Dubstep" && (track.genre.contains("Dubstep", ignoreCase = true) || track.genre.contains("Riddim", ignoreCase = true) || track.genre.contains("Tearout", ignoreCase = true)))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDarkBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSubmitDialog = true },
                containerColor = SpotifyGreen,
                contentColor = StudioBlack,
                icon = { Icon(Icons.Default.Add, contentDescription = "Submit Track") },
                text = { Text("Submit Track", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
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
                                text = "Exchange",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HUB",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = SpotifyGreenBright
                            )
                        }
                        Text(
                            text = "Organic Spotify plays, feedback & stem trade",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary
                        )
                    }

                    CreditSparklesPill(
                        credits = userCredits,
                        onClick = {
                            Toast.makeText(
                                context,
                                "Listen to tracks for 30s + review to earn 15-20 Sparkles!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Notice Banner: Organic Spotify Compliance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StudioCardElevated
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Organic",
                                tint = SpotifyGreenBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% Organic Artist Discovery",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "No bots or fake streams. Real independent artists stream your music directly on Spotify for 30s+ to trigger the algorithm.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Daily Organic Limit Card (500 plays / 100 followers)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = AmberOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Organic Velocity Limits",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                            }
                            Text(
                                text = "Resets 00:00 UTC",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Daily Plays: e.g. 38 / 500
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Play Exchanges",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGraySecondary
                                )
                                Text(
                                    text = "$dailyPlaysUsed / $dailyPlaysLimit plays",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dailyPlaysUsed >= dailyPlaysLimit) CoralRed else SpotifyGreenBright
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val playFraction = (dailyPlaysUsed.toFloat() / dailyPlaysLimit.coerceAtLeast(1)).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { playFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (dailyPlaysUsed >= dailyPlaysLimit) CoralRed else SpotifyGreen,
                                trackColor = StudioBorder
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Daily Followers: e.g. 14 / 100
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spotify Follower Exchanges",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGraySecondary
                                )
                                Text(
                                    text = "$dailyFollowersUsed / $dailyFollowersLimit followers",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dailyFollowersUsed >= dailyFollowersLimit) CoralRed else NeonCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val followFraction = (dailyFollowersUsed.toFloat() / dailyFollowersLimit.coerceAtLeast(1)).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { followFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (dailyFollowersUsed >= dailyFollowersLimit) CoralRed else NeonCyan,
                                trackColor = StudioBorder
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Daily Instagram Followers (Safe Rate Limit): e.g. 8 / 50
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Instagram Follows (Anti-Block Safe Limit)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGraySecondary
                                )
                                Text(
                                    text = "$dailyInstagramFollowersUsed / $dailyInstagramFollowersLimit IG follows",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dailyInstagramFollowersUsed >= dailyInstagramFollowersLimit) CoralRed else ElectricPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val igFraction = (dailyInstagramFollowersUsed.toFloat() / dailyInstagramFollowersLimit.coerceAtLeast(1)).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { igFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (dailyInstagramFollowersUsed >= dailyInstagramFollowersLimit) CoralRed else ElectricPurple,
                                trackColor = StudioBorder
                            )
                        }
                    }
                }
            }

            // Genre filter row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(genres) { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

            // Track List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Discovery Queue (${filteredTracks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "Earn +15 per listen",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Track Items
            items(filteredTracks, key = { it.id }) { track ->
                ExchangeTrackCard(
                    track = track,
                    onOpenReview = { activeExchangeTrack = track },
                    onFavorite = { onToggleFavorite(track.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Modal Dialog: Play, Spotify Direct Launch, 30s Verification, & Review Form
    activeExchangeTrack?.let { track ->
        ListenExchangeDialog(
            track = track,
            onDismiss = { activeExchangeTrack = null },
            onSubmitReview = { overall, mix, melody, vocal, comment, followedSpotify, followedInstagram ->
                val result = onCompleteReview(track.id, overall, mix, melody, vocal, comment, followedSpotify, followedInstagram)
                activeExchangeTrack = null
                when (result) {
                    is ExchangeResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                    is ExchangeResult.LimitReached -> {
                        Toast.makeText(context, result.reason, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Modal Dialog: Submit Track to Queue
    if (showSubmitDialog) {
        SubmitTrackDialog(
            userCredits = userCredits,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { title, spotifyUrl, genre, bpm, key, duration, stake, igHandle ->
                val success = onSubmitTrack(title, spotifyUrl, genre, bpm, key, duration, stake, igHandle)
                if (success) {
                    showSubmitDialog = false
                    Toast.makeText(
                        context,
                        "🚀 Track launched to the Spotify Discovery Queue!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Insufficient Sparkles credits to stake this track.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
fun ExchangeTrackCard(
    track: ExchangeTrack,
    onOpenReview: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenReview() },
        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art / Cover with Coil
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioCardElevated)
                ) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Mini Spotify icon badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(StudioBlack.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Spotify",
                            tint = SpotifyGreenBright,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onFavorite,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (track.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (track.isFavorited) CoralRed else TextGraySecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "${track.artistName} • ${track.artistHandle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = track.genre,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            modifier = Modifier
                                .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "${track.bpm} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGraySecondary
                        )
                        Text(
                            text = "• ${track.musicalKey}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGraySecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress towards Target Plays & Listen Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Campaign Plays",
                            fontSize = 11.sp,
                            color = TextGraySecondary
                        )
                        Text(
                            text = "${track.playsReceived}/${track.targetPlays}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreenBright
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val progress = (track.playsReceived.toFloat() / track.targetPlays).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SpotifyGreen,
                        trackColor = StudioBorder
                    )
                }

                Button(
                    onClick = onOpenReview,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (track.hasReviewed) StudioCardElevated else SpotifyGreen,
                        contentColor = if (track.hasReviewed) TextGraySecondary else StudioBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = if (track.hasReviewed) Icons.Default.Check else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (track.hasReviewed) "Reviewed" else "Listen +15",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ListenExchangeDialog(
    track: ExchangeTrack,
    onDismiss: () -> Unit,
    onSubmitReview: (Float, Float, Float, Float, String, Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 30s Spotify monetization & algorithm listener countdown
    var secondsListened by remember { mutableIntStateOf(0) }
    var isTimerActive by remember { mutableStateOf(false) }
    val requiredSeconds = 30
    val isVerified = secondsListened >= requiredSeconds

    // Ratings
    var ratingOverall by remember { mutableFloatStateOf(4.5f) }
    var mixScore by remember { mutableFloatStateOf(4.5f) }
    var melodyScore by remember { mutableFloatStateOf(4.5f) }
    var vocalScore by remember { mutableFloatStateOf(4.0f) }
    var commentText by remember { mutableStateOf("") }
    var followedOnSpotify by remember { mutableStateOf(true) }
    var followedOnInstagram by remember { mutableStateOf(track.instagramHandle.isNotBlank()) }

    // Timer coroutine
    LaunchedEffect(isTimerActive) {
        while (isTimerActive && secondsListened < requiredSeconds) {
            delay(1000)
            secondsListened += 1
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = StudioDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top close & title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Play & Review Exchange",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGraySecondary)
                        }
                    }
                }

                // Track Banner card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCardSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = track.title,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = track.artistName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary
                            )
                            Text(
                                text = "${track.genre} • ${track.bpm} BPM • ${track.musicalKey}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }
                    }
                }

                // Spotify Launch Button & Verification Timer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isVerified) SpotifyGreen else StudioBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Spotify 30s Stream Verification",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) SpotifyGreenBright else TextWhitePrimary
                            )
                            Text(
                                text = "Spotify registers a genuine stream after 30 seconds of play, triggering Discover Weekly algorithms.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Verification Timer Circle / Progress
                            Box(
                                modifier = Modifier.size(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { (secondsListened.toFloat() / requiredSeconds).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (isVerified) SpotifyGreenBright else NeonCyan,
                                    trackColor = StudioBorder,
                                    strokeWidth = 6.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (isVerified) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = SpotifyGreenBright,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "VERIFIED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SpotifyGreenBright
                                        )
                                    } else {
                                        Text(
                                            text = "${requiredSeconds - secondsListened}s",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhitePrimary
                                        )
                                        Text(
                                            text = "REMAINING",
                                            fontSize = 8.sp,
                                            color = TextGraySecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Animated Waveform
                            WaveformVisualizer(
                                isPlaying = isTimerActive && !isVerified,
                                barCount = 28,
                                baseColor = if (isVerified) SpotifyGreen else NeonCyan,
                                accentColor = if (isVerified) SpotifyGreenBright else ElectricPurple,
                                height = 32.dp,
                                progress = (secondsListened.toFloat() / requiredSeconds).coerceIn(0f, 1f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dual Action: Open in Spotify app AND Start timer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Start the verification timer
                                        isTimerActive = true
                                        // Open real Spotify track via URI or web intent
                                        try {
                                            val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(track.spotifyUri))
                                            spotifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            context.startActivity(spotifyIntent)
                                        } catch (e: Exception) {
                                            // Fallback to web URL
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(track.spotifyWebUrl))
                                            context.startActivity(webIntent)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SpotifyGreen,
                                        contentColor = StudioBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Spotify", fontWeight = FontWeight.Bold)
                                }

                                if (!isTimerActive && !isVerified) {
                                    OutlinedButton(
                                        onClick = { isTimerActive = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                                    ) {
                                        Text("In-App Listen")
                                    }
                                }
                            }
                        }
                    }
                }

                // Constructive Feedback Section
                item {
                    Text(
                        text = "Artist Review & Production Ratings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RatingBarRow(label = "Mix & Master Quality", score = mixScore, onScoreChange = { mixScore = it })
                    RatingBarRow(label = "Melody & Catchiness", score = melodyScore, onScoreChange = { melodyScore = it })
                    RatingBarRow(label = "Vocal / Lead Delivery", score = vocalScore, onScoreChange = { vocalScore = it })
                }

                // Quick feedback tag chips
                item {
                    Text(
                        text = "Quick Feedback Tags",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGraySecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val tagOptions = listOf("Crisp Top End", "Great 808 Bass", "Catchy Hook", "Discover Weekly Vibe", "Wants Stems!")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagOptions.take(3).forEach { tag ->
                            SuggestionChip(
                                onClick = {
                                    commentText = if (commentText.isEmpty()) tag else "$commentText, $tag"
                                },
                                label = { Text(tag, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = StudioCardElevated,
                                    labelColor = TextWhitePrimary
                                )
                            )
                        }
                    }
                }

                // Feedback Comment Text Input
                item {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Constructive feedback for artist") },
                        placeholder = { Text("e.g. Loved the chord progression, vocal reverb fits great!") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        ),
                        maxLines = 3
                    )
                }

                // Follow Artist on Spotify Checkbox
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCardSurface, RoundedCornerShape(10.dp))
                            .clickable { followedOnSpotify = !followedOnSpotify }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = followedOnSpotify,
                            onCheckedChange = { followedOnSpotify = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SpotifyGreen,
                                checkmarkColor = StudioBlack
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Follow ${track.artistName} on Spotify",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Boosts artist follower count (+5 bonus Sparkles)",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldYellow,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Safe Follow Artist on Instagram Card
                item {
                    val igHandle = track.instagramHandle.ifBlank { "@" + track.artistName.lowercase().replace(" ", "") }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = followedOnInstagram,
                                    onCheckedChange = { followedOnInstagram = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ElectricPurple,
                                        checkmarkColor = StudioBlack
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Follow $igHandle on Instagram",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhitePrimary
                                    )
                                    Text(
                                        text = "Safe Instagram follower exchange (+5 bonus Sparkles)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElectricPurple,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        val cleanHandle = igHandle.removePrefix("@")
                                        val appUri = Uri.parse("http://instagram.com/_u/$cleanHandle")
                                        val igIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                                            setPackage("com.instagram.android")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        try {
                                            context.startActivity(igIntent)
                                        } catch (e: Exception) {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/$cleanHandle"))
                                            context.startActivity(webIntent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricPurple.copy(alpha = 0.2f),
                                        contentColor = ElectricPurple
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open IG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "🛡️ Safe IG Guarantee: Opens official Instagram app directly. No API tokens or passwords needed, keeping your account 100% safe from action blocks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Submit Button
                item {
                    val totalBonus = 15 + (if (followedOnSpotify) 5 else 0) + (if (followedOnInstagram) 5 else 0)
                    Button(
                        onClick = {
                            val overall = (mixScore + melodyScore + vocalScore) / 3f
                            onSubmitReview(overall, mixScore, melodyScore, vocalScore, commentText, followedOnSpotify, followedOnInstagram)
                        },
                        enabled = isVerified,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = StudioBlack,
                            disabledContainerColor = StudioCardElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isVerified) GoldYellow else TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVerified) "Claim +$totalBonus Sparkles" else "Listen for 30s to Unlock (${requiredSeconds - secondsListened}s left)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun RatingBarRow(
    label: String,
    score: Float,
    onScoreChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextWhitePrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= score) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$i stars",
                    tint = if (i <= score) GoldYellow else TextMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onScoreChange(i.toFloat()) }
                )
            }
        }
    }
}

@Composable
fun SubmitTrackDialog(
    userCredits: Int,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Int, String, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var spotifyLink by remember { mutableStateOf("https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT") }
    var instagramHandle by remember { mutableStateOf("@kairos_producer") }
    var genre by remember { mutableStateOf("Synthwave") }
    var bpmText by remember { mutableStateOf("124") }
    var keyText by remember { mutableStateOf("E Minor") }
    var stakeCredits by remember { mutableIntStateOf(20) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = StudioDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Submit Track to Queue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGraySecondary)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Track Title") },
                    placeholder = { Text("e.g. Moonlight Drive") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                OutlinedTextField(
                    value = spotifyLink,
                    onValueChange = { spotifyLink = it },
                    label = { Text("Spotify Track URL or URI") },
                    placeholder = { Text("https://open.spotify.com/track/...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                OutlinedTextField(
                    value = instagramHandle,
                    onValueChange = { instagramHandle = it },
                    label = { Text("Artist Instagram Handle (for IG exchange)") },
                    placeholder = { Text("@your_producer_handle") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        )
                    )
                    OutlinedTextField(
                        value = bpmText,
                        onValueChange = { bpmText = it },
                        label = { Text("BPM") },
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        )
                    )
                }

                // Quick Genre Chips for Dubstep, EDM, House, Techno, etc.
                Column {
                    Text(
                        text = "Quick Genre Presets (Tap to select):",
                        fontSize = 11.sp,
                        color = TextGraySecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickGenres = listOf(
                            "Dubstep" to "140",
                            "Melodic Dubstep" to "145",
                            "Riddim" to "140",
                            "EDM" to "128",
                            "Drum & Bass" to "174",
                            "Tech House" to "126",
                            "Melodic Techno" to "124",
                            "Future Bass" to "150",
                            "Trap" to "140",
                            "Synthwave" to "120",
                            "Lo-Fi Hip Hop" to "80"
                        )
                        items(quickGenres) { (presetGenre, presetBpm) ->
                            FilterChip(
                                selected = genre == presetGenre,
                                onClick = {
                                    genre = presetGenre
                                    bpmText = presetBpm
                                },
                                label = { Text(presetGenre, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpotifyGreen,
                                    selectedLabelColor = StudioBlack,
                                    containerColor = StudioCardSurface,
                                    labelColor = TextWhitePrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = genre == presetGenre,
                                    borderColor = if (genre == presetGenre) SpotifyGreen else StudioBorder
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bpmText,
                        onValueChange = { bpmText = it },
                        label = { Text("BPM") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        )
                    )
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        label = { Text("Musical Key") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        )
                    )
                }

                // Stake Credits Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stake Sparkles: $stakeCredits",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Text(
                            text = "Est. ${(stakeCredits * 1.5).toInt()} organic plays",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyGreenBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = stakeCredits.toFloat(),
                        onValueChange = { stakeCredits = it.toInt() },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = SpotifyGreen,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = StudioBorder
                        )
                    )
                    Text(
                        text = "Available Sparkles: $userCredits",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )
                }

                Button(
                    onClick = {
                        val finalTitle = title.ifBlank { "Untitled Single" }
                        val bpm = bpmText.toIntOrNull() ?: 124
                        onSubmit(finalTitle, spotifyLink, genre, bpm, keyText, 190, stakeCredits, instagramHandle)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = StudioBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Stake $stakeCredits Sparkles & Launch", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

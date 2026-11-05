package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.audio.StemMixerController
import com.example.model.AudioStem
import com.example.model.StemPack
import com.example.model.StemType
import com.example.ui.components.CreditSparklesPill
import com.example.ui.components.StemMixerStrip
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.*

@Composable
fun StemMarketplaceScreen(
    stemPacks: List<StemPack>,
    mixerController: StemMixerController,
    userCredits: Int,
    onUnlockPack: (String) -> Boolean,
    onUploadPack: (String, String, Int, String, String, List<AudioStem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Dubstep & Riddim", "EDM", "Melodic Techno", "Vocals", "Drums & 808", "Basslines", "Synths", "Lo-Fi")

    var previewingPack by remember { mutableStateOf<StemPack?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }

    // If active pack in mixer, open studio
    val activePack = mixerController.currentPack

    val filteredPacks = remember(stemPacks, selectedCategory) {
        if (selectedCategory == "All") stemPacks
        else stemPacks.filter { pack ->
            when (selectedCategory) {
                "Dubstep & Riddim" -> pack.genre.contains("Dubstep", ignoreCase = true) || pack.genre.contains("Riddim", ignoreCase = true) || pack.title.contains("Bass", ignoreCase = true)
                "EDM" -> pack.genre.contains("EDM", ignoreCase = true) || pack.genre.contains("Electro", ignoreCase = true) || pack.genre.contains("Festival", ignoreCase = true)
                "Melodic Techno" -> pack.genre.contains("Techno", ignoreCase = true) || pack.genre.contains("House", ignoreCase = true)
                "Vocals" -> pack.stems.any { it.stemType == StemType.VOCALS }
                "Drums & 808" -> pack.stems.any { it.stemType == StemType.DRUMS }
                "Basslines" -> pack.stems.any { it.stemType == StemType.BASS }
                "Synths" -> pack.stems.any { it.stemType == StemType.SYNTH_LEAD }
                "Lo-Fi" -> pack.genre.contains("Lo-Fi", ignoreCase = true) || pack.genre.contains("Chill", ignoreCase = true)
                else -> pack.genre.contains(selectedCategory, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDarkBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = NeonCyan,
                contentColor = StudioBlack,
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Share Stems") },
                text = { Text("Share Stems", fontWeight = FontWeight.Bold) }
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
            // Header
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
                                text = "Stem",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VAULT",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                        Text(
                            text = "Acapellas, 808s, MIDI & multi-track stems",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary
                        )
                    }

                    CreditSparklesPill(
                        credits = userCredits,
                        onClick = {
                            Toast.makeText(
                                context,
                                "Unlock stem packs with Sparkles or share your own stems!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Studio Console Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Generated studio background asset
                        AsyncImage(
                            model = "file:///app/src/main/res/drawable/banner_studio_hero_1793897934124.jpg",
                            contentDescription = "Studio console",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            StudioDarkBg.copy(alpha = 0.95f),
                                            StudioDarkBg.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterStart)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MULTI-TRACK STEM MIXER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Isolate Vocals & Remix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Mute, solo, and preview individual studio channels in real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Categories
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = StudioBlack,
                                containerColor = StudioCardSurface,
                                labelColor = TextWhitePrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else StudioBorder
                            )
                        )
                    }
                }
            }
            // Stem Packs List
            item {
                Text(
                    text = "Stem Packs & Collab Stems (${filteredPacks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
            }

            items(filteredPacks, key = { it.id }) { pack ->
                StemPackCard(
                    pack = pack,
                    onOpenStudio = {
                        mixerController.loadPack(pack)
                        previewingPack = pack
                    },
                    onUnlock = {
                        val success = onUnlockPack(pack.id)
                        if (success) {
                            Toast.makeText(context, "🎉 Stem pack unlocked for collaboration!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Insufficient Sparkles to unlock. Review tracks to earn more!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Interactive Stem Studio Modal Dialog
    if (previewingPack != null && activePack != null) {
        InteractiveStemStudioDialog(
            pack = activePack,
            controller = mixerController,
            onDismiss = {
                mixerController.pause()
                previewingPack = null
            },
            onDownload = {
                Toast.makeText(
                    context,
                    "📦 24-bit studio stems exported: ${activePack.stems.size} WAV tracks (dry vocals, drums, bassline, synth) ready in your DAW!",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    // Upload Stems Dialog
    if (showUploadDialog) {
        UploadStemsDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { title, genre, bpm, key, desc, stems ->
                onUploadPack(title, genre, bpm, key, desc, stems)
                showUploadDialog = false
                Toast.makeText(
                    context,
                    "🚀 Stems published! You earned +30 Sparkles!",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}

@Composable
fun StemPackCard(
    pack: StemPack,
    onOpenStudio: () -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenStudio() },
        colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover
                AsyncImage(
                    model = pack.coverUrl,
                    contentDescription = pack.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "By ${pack.artistName} • ${pack.genre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGraySecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${pack.bpm} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• ${pack.musicalKey}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• ${pack.stems.size} Stems",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGraySecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pack.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextGraySecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stem preview badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pack.stems.forEach { stem ->
                    val color = Color(stem.stemType.badgeColorHex)
                    Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stem.stemType.displayName.substringBefore(" /"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row: Test in Studio OR Unlock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = TextGraySecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pack.downloadCount} downloads",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGraySecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenStudio,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mixer Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (pack.isUnlocked) {
                        Button(
                            onClick = onOpenStudio,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = StudioBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlocked", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onUnlock,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldYellow,
                                contentColor = StudioBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock (25 ✨)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveStemStudioDialog(
    pack: StemPack,
    controller: StemMixerController,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = StudioDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stem Mixer Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                        }
                        Text(
                            text = "${pack.title} • ${pack.bpm} BPM • ${pack.musicalKey}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGraySecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transport Bar (Play/Pause, Position, Download Stems)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { controller.togglePlayPause() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (controller.isPlaying) SpotifyGreenBright else SpotifyGreen)
                            ) {
                                Icon(
                                    imageVector = if (controller.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (controller.isPlaying) "Pause" else "Play",
                                    tint = StudioBlack,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                val currentMin = (controller.currentPositionSeconds / 60).toInt()
                                val currentSec = (controller.currentPositionSeconds % 60).toInt()
                                val totalMin = (controller.totalDurationSeconds / 60).toInt()
                                val totalSec = (controller.totalDurationSeconds % 60).toInt()
                                Text(
                                    text = "%02d:%02d / %02d:%02d".format(currentMin, currentSec, totalMin, totalSec),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = if (controller.isPlaying) "Sync Playing • Stems Audible" else "Paused",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (controller.isPlaying) SpotifyGreenBright else TextGraySecondary
                                )
                            }
                        }

                        Button(
                            onClick = onDownload,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pack.isUnlocked) SpotifyGreen else GoldYellow,
                                contentColor = StudioBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (pack.isUnlocked) "Export Stems" else "Unlock (25 ✨)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Studio Channel Strips (M = Mute, S = Solo)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGraySecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable List of stem channel strips
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pack.stems, key = { it.id }) { stem ->
                        val isMuted = controller.muteStates[stem.id] ?: false
                        val isSolo = controller.soloStates[stem.id] ?: false
                        val volume = controller.volumeLevels[stem.id] ?: 0.85f
                        val isAudible = controller.isStemAudible(stem.id)

                        StemMixerStrip(
                            stem = stem,
                            isPlaying = controller.isPlaying,
                            isMuted = isMuted,
                            isSolo = isSolo,
                            isAudible = isAudible,
                            volume = volume,
                            onToggleMute = { controller.toggleMute(stem.id) },
                            onToggleSolo = { controller.toggleSolo(stem.id) },
                            onVolumeChange = { controller.setVolume(stem.id, it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Collab Pitch prompt
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Want to collaborate or remix? Download stems to start building your track, then exchange back with ${pack.artistName}!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGraySecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UploadStemsDialog(
    onDismiss: () -> Unit,
    onUpload: (String, String, Int, String, String, List<AudioStem>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Electronic") }
    var bpmText by remember { mutableStateOf("128") }
    var keyText by remember { mutableStateOf("A Minor") }
    var description by remember { mutableStateOf("") }

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
                        text = "Share Stems With Community",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGraySecondary)
                    }
                }

                Text(
                    text = "Upload your audio stems (acapellas, drum kits, synth loops). You earn +30 Sparkles per pack shared!",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyGreenBright
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stem Pack Name") },
                    placeholder = { Text("e.g. Dreamy Vocal & Synth Stems") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
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
                            focusedBorderColor = NeonCyan,
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
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        )
                    )
                }

                // Quick Genre Presets for Stems
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickStemGenres = listOf(
                        "Dubstep" to "140",
                        "Melodic Dubstep" to "145",
                        "Riddim" to "140",
                        "EDM" to "128",
                        "Drum & Bass" to "174",
                        "Tech House" to "126",
                        "Lo-Fi Hip Hop" to "80"
                    )
                    items(quickStemGenres) { (stemGenre, stemBpm) ->
                        FilterChip(
                            selected = genre == stemGenre,
                            onClick = {
                                genre = stemGenre
                                bpmText = stemBpm
                            },
                            label = { Text(stemGenre, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = StudioBlack,
                                containerColor = StudioCardSurface,
                                labelColor = TextWhitePrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = genre == stemGenre,
                                borderColor = if (genre == stemGenre) NeonCyan else StudioBorder
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        label = { Text("Musical Key") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Collab description & DAW info") },
                    placeholder = { Text("e.g. 24-bit WAV recorded in Ableton Live. Tuned vocals & punchy kicks.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                Button(
                    onClick = {
                        val bpm = bpmText.toIntOrNull() ?: 128
                        val packTitle = title.ifBlank { "Producer Collab Stems" }
                        val dummyWaveform = listOf(0.4f, 0.7f, 0.3f, 0.8f, 0.5f, 0.9f, 0.6f, 0.3f, 0.7f, 0.5f, 0.8f, 0.4f)
                        val packId = "user_pack_${System.currentTimeMillis()}"
                        val generatedStems = listOf(
                            AudioStem("s_u_1", packId, "Lead Vocal Acapella", StemType.VOCALS, keyText, bpm, 180, 10, 0, dummyWaveform),
                            AudioStem("s_u_2", packId, "Punchy 808 & Kick", StemType.DRUMS, keyText, bpm, 180, 10, 0, dummyWaveform),
                            AudioStem("s_u_3", packId, "Sub Bassline", StemType.BASS, keyText, bpm, 180, 10, 0, dummyWaveform),
                            AudioStem("s_u_4", packId, "Melodic Synth Arp", StemType.SYNTH_LEAD, keyText, bpm, 180, 10, 0, dummyWaveform)
                        )
                        onUpload(packTitle, genre, bpm, keyText, description, generatedStems)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = StudioBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Stems (+30 Sparkles)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

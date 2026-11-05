package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import com.example.model.ArtistProfile
import com.example.ui.components.CreditSparklesPill
import com.example.ui.theme.*

@Composable
fun ArtistProfileScreen(
    profile: ArtistProfile,
    onUpdateProfile: (String, String, String, String, String) -> Unit,
    onConnectSpotify: (String, String, String) -> Unit = { _, _, _ -> },
    onDisconnectSpotify: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showSpotifyLoginDialog by remember { mutableStateOf(false) }
    var showSafetyExplainerDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Artist Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = TextWhitePrimary
                )
                CreditSparklesPill(credits = profile.sparklesCredits)
            }
        }

        // Artist Card Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Initial Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen.copy(alpha = 0.2f))
                                .border(2.dp, SpotifyGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = SpotifyGreenBright
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhitePrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Artist",
                                    tint = SpotifyGreenBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${profile.handle} • ${profile.instagramHandle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGraySecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${profile.monthlyListeners} Monthly",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• ${profile.followers} Followers",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGraySecondary
                                )
                            }
                        }

                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = TextGraySecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhitePrimary,
                        lineHeight = 20.sp
                    )

                    // Spotify Connection Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (profile.isSpotifyConnected) StudioCardElevated else StudioCardSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (profile.isSpotifyConnected) SpotifyGreen.copy(alpha = 0.5f) else AmberOrange.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (profile.isSpotifyConnected) SpotifyGreen.copy(alpha = 0.2f) else AmberOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (profile.isSpotifyConnected) Icons.Default.CheckCircle else Icons.Default.LinkOff,
                                    contentDescription = null,
                                    tint = if (profile.isSpotifyConnected) SpotifyGreenBright else AmberOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (profile.isSpotifyConnected) "Spotify Connected (Active)" else "Spotify Not Linked",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhitePrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Safe Organic",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreenBright,
                                        modifier = Modifier
                                            .background(SpotifyGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = if (profile.isSpotifyConnected)
                                        profile.spotifyAccountEmail.ifBlank { "Logged in via Spotify Web & App" }
                                    else
                                        "Link your Spotify account to exchange plays & stems safely",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGraySecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = { showSpotifyLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (profile.isSpotifyConnected) StudioDarkBg else SpotifyGreen,
                                    contentColor = if (profile.isSpotifyConnected) SpotifyGreenBright else StudioBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (profile.isSpotifyConnected) "Switch" else "Log In",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Open on Spotify & Instagram Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.spotifyUri))
                                    spotifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(spotifyIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.spotifyUrl))
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = StudioBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Spotify Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val cleanHandle = profile.instagramHandle.removePrefix("@")
                                val appUri = Uri.parse("http://instagram.com/_u/$cleanHandle")
                                val igIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                                    setPackage("com.instagram.android")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(igIntent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(profile.instagramUrl)))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricPurple.copy(alpha = 0.2f),
                                contentColor = ElectricPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instagram Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Community Exchange Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Organic Exchange Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Sparkles Balance", value = "${profile.sparklesCredits}", color = GoldYellow)
                        StatItem(label = "Plays Given", value = "${profile.playsGiven}", color = SpotifyGreenBright)
                        StatItem(label = "Plays Received", value = "${profile.playsReceived}", color = NeonCyan)
                        StatItem(label = "Stems Shared", value = "${profile.stemsSharedCount}", color = ElectricPurple)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Limits Status Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioCardElevated, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Daily Safety Quotas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                                Text(
                                    text = "Resets 00:00 UTC",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Spotify Plays: ${profile.dailyPlaysUsed}/${profile.dailyPlaysLimit} plays\n• Spotify Followers: ${profile.dailyFollowersUsed}/${profile.dailyFollowersLimit} followers\n• Instagram Followers: ${profile.dailyInstagramFollowersUsed}/${profile.dailyInstagramFollowersLimit} safe follows",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpotifyGreenBright,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Organic caps protect both your Spotify and Instagram accounts against automated rate limit flags and shadowbans.",
                                fontSize = 10.sp,
                                color = TextGraySecondary
                            )
                        }
                    }
                }
            }
        }

        // Spotify & Instagram Organic Growth Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SpotifyGreenBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Safe Organic Growth Principles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RuleItem(
                        number = "1",
                        title = "Zero Bots, Zero Fake Streams",
                        description = "Spotify penalizes streaming farms. StemSpire uses real fellow producers streaming 30s+ on authentic Spotify accounts."
                    )
                    RuleItem(
                        number = "2",
                        title = "Safe Instagram Follower Exchange",
                        description = "StemSpire opens official Instagram profiles directly. Never enter your password or use bot tokens; manual follows keep your account 100% safe."
                    )
                    RuleItem(
                        number = "3",
                        title = "Target a >10% Save-to-Stream Ratio",
                        description = "The Spotify Discover Weekly algorithm triggers when listeners save your track to their Library or user playlists."
                    )
                    RuleItem(
                        number = "4",
                        title = "Trade Stems for Cross-Promotion",
                        description = "Collaborate with artists who have different audiences. Remix releases allow co-artist tagging which doubles Release Radar reach."
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentProfile = profile,
            onDismiss = { showEditDialog = false },
            onSave = { name, handle, bio, spotifyUrl, instagramHandle ->
                onUpdateProfile(name, handle, bio, spotifyUrl, instagramHandle)
                showEditDialog = false
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSpotifyLoginDialog) {
        EasySpotifyLoginDialog(
            currentProfile = profile,
            onDismiss = { showSpotifyLoginDialog = false },
            onConfirmLogin = { artistName, spotifyUrl, email ->
                onConnectSpotify(artistName, spotifyUrl, email)
                showSpotifyLoginDialog = false
                Toast.makeText(context, "Spotify Account Connected Safely!", Toast.LENGTH_SHORT).show()
            },
            onDisconnect = {
                onDisconnectSpotify()
                showSpotifyLoginDialog = false
                Toast.makeText(context, "Spotify unlinked", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextGraySecondary
        )
    }
}

@Composable
private fun RuleItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(SpotifyGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreenBright
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextGraySecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: ArtistProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.name) }
    var handle by remember { mutableStateOf(currentProfile.handle) }
    var instagramHandle by remember { mutableStateOf(currentProfile.instagramHandle) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var spotifyUrl by remember { mutableStateOf(currentProfile.spotifyUrl) }

    Dialog(onDismissRequest = onDismiss) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Edit Artist Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Artist Name") },
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
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Artist Handle") },
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
                    label = { Text("Instagram Handle (e.g. @your_sound)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                OutlinedTextField(
                    value = spotifyUrl,
                    onValueChange = { spotifyUrl = it },
                    label = { Text("Spotify Artist URL") },
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
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Artist Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                Button(
                    onClick = { onSave(name, handle, bio, spotifyUrl, instagramHandle) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = StudioBlack
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EasySpotifyLoginDialog(
    currentProfile: ArtistProfile,
    onDismiss: () -> Unit,
    onConfirmLogin: (String, String, String) -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    var artistName by remember { mutableStateOf(currentProfile.name) }
    var spotifyProfileUrl by remember { mutableStateOf(currentProfile.spotifyUrl) }
    var emailInput by remember { mutableStateOf(currentProfile.spotifyAccountEmail.ifBlank { "dipeshkondalkar@gmail.com" }) }
    var agreedToFairRules by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = StudioDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, SpotifyGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = StudioBlack, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Easy Spotify Login",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "Safe Organic Artist Verification",
                                fontSize = 11.sp,
                                color = SpotifyGreenBright
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGraySecondary)
                    }
                }

                // Safety Assurance Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = SpotifyGreenBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Safe: No passwords or bot scripts stored. Links your authentic artist catalog directly to Spotify apps.",
                            fontSize = 11.sp,
                            color = TextWhitePrimary,
                            lineHeight = 15.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Spotify Account Email") },
                    placeholder = { Text("your.artist.email@domain.com") },
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
                    value = artistName,
                    onValueChange = { artistName = it },
                    label = { Text("Spotify Artist Name") },
                    placeholder = { Text("e.g. Kairos Beatmaker") },
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
                    value = spotifyProfileUrl,
                    onValueChange = { spotifyProfileUrl = it },
                    label = { Text("Spotify Artist Profile URL") },
                    placeholder = { Text("https://open.spotify.com/artist/...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary
                    )
                )

                // Quick Launch to verify or find profile link
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Spotify to Copy Your Artist Link", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { agreedToFairRules = !agreedToFairRules }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToFairRules,
                        onCheckedChange = { agreedToFairRules = it },
                        colors = CheckboxDefaults.colors(checkedColor = SpotifyGreen, checkmarkColor = StudioBlack)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I confirm this is my genuine artist profile. I agree to listen 30s+ per exchange to maintain organic credibility.",
                        fontSize = 11.sp,
                        color = TextGraySecondary,
                        lineHeight = 15.sp
                    )
                }

                // Connect Button
                Button(
                    onClick = {
                        if (emailInput.isBlank()) {
                            Toast.makeText(context, "Please enter your Spotify account email", Toast.LENGTH_SHORT).show()
                        } else {
                            onConfirmLogin(artistName, spotifyProfileUrl, emailInput)
                        }
                    },
                    enabled = agreedToFairRules && emailInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = StudioBlack,
                        disabledContainerColor = StudioCardElevated,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect Spotify Account", fontWeight = FontWeight.Bold)
                }

                if (currentProfile.isSpotifyConnected) {
                    TextButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect / Unlink Spotify", color = CoralRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.StemMixerController
import com.example.data.StemSpireRepository
import com.example.ui.screens.AnalyticsStudioScreen
import com.example.ui.screens.ArtistProfileScreen
import com.example.ui.screens.ExchangeHubScreen
import com.example.ui.screens.StemMarketplaceScreen
import com.example.ui.theme.*

enum class AppNavTab(val title: String, val icon: ImageVector) {
    EXCHANGE("Exchange", Icons.Default.SwapHoriz),
    STEMS("Stems", Icons.Default.Tune),
    ANALYTICS("Analytics", Icons.Default.Insights),
    PROFILE("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: StemSpireRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = StemSpireRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                MainAppScreen(repository = repository)
            }
        }
    }
}

@Composable
fun MainAppScreen(repository: StemSpireRepository) {
    val coroutineScope = rememberCoroutineScope()
    val mixerController = remember { StemMixerController(coroutineScope) }

    var currentTab by remember { mutableStateOf(AppNavTab.EXCHANGE) }

    val artistProfile by repository.artistProfile.collectAsStateWithLifecycle()
    val tracks by repository.exchangeTracks.collectAsStateWithLifecycle()
    val stemPacks by repository.stemPacks.collectAsStateWithLifecycle()

    // BackHandler: returns to Exchange tab if on other sub-screens
    BackHandler(enabled = currentTab != AppNavTab.EXCHANGE) {
        currentTab = AppNavTab.EXCHANGE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StudioDarkBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar"),
                containerColor = StudioBlack,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                AppNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) SpotifyGreenBright else TextGraySecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SpotifyGreenBright else TextGraySecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = SpotifyGreen.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.EXCHANGE -> {
                    ExchangeHubScreen(
                        tracks = tracks,
                        userCredits = artistProfile.sparklesCredits,
                        dailyPlaysUsed = artistProfile.dailyPlaysUsed,
                        dailyPlaysLimit = artistProfile.dailyPlaysLimit,
                        dailyFollowersUsed = artistProfile.dailyFollowersUsed,
                        dailyFollowersLimit = artistProfile.dailyFollowersLimit,
                        dailyInstagramFollowersUsed = artistProfile.dailyInstagramFollowersUsed,
                        dailyInstagramFollowersLimit = artistProfile.dailyInstagramFollowersLimit,
                        onCompleteReview = { trackId, overall, mix, melody, vocal, comment, followedSpotify, followedInstagram ->
                            repository.completeReviewAndExchange(trackId, overall, mix, melody, vocal, comment, followedSpotify, followedInstagram)
                        },
                        onSubmitTrack = { title, spotifyUrl, genre, bpm, key, duration, stake, igHandle ->
                            repository.submitTrackToExchange(title, spotifyUrl, genre, bpm, key, duration, stake, igHandle)
                        },
                        onToggleFavorite = { trackId ->
                            repository.toggleFavoriteTrack(trackId)
                        }
                    )
                }
                AppNavTab.STEMS -> {
                    StemMarketplaceScreen(
                        stemPacks = stemPacks,
                        mixerController = mixerController,
                        userCredits = artistProfile.sparklesCredits,
                        onUnlockPack = { packId ->
                            repository.unlockStemPack(packId)
                        },
                        onUploadPack = { title, genre, bpm, key, desc, stems ->
                            repository.uploadStemPack(title, genre, bpm, key, desc, stems)
                        }
                    )
                }
                AppNavTab.ANALYTICS -> {
                    AnalyticsStudioScreen(
                        repository = repository,
                        tracks = tracks,
                        userCredits = artistProfile.sparklesCredits
                    )
                }
                AppNavTab.PROFILE -> {
                    ArtistProfileScreen(
                        profile = artistProfile,
                        onUpdateProfile = { name, handle, bio, spotifyUrl, instagramHandle ->
                            repository.updateProfile(name, handle, bio, spotifyUrl, instagramHandle)
                        },
                        onConnectSpotify = { artistName, spotifyUrl, email ->
                            repository.connectSpotify(artistName, spotifyUrl, email)
                        },
                        onDisconnectSpotify = {
                            repository.disconnectSpotify()
                        }
                    )
                }
            }
        }
    }
}

package com.example.data

import android.content.Context
import com.example.data.local.StemSpireDatabase
import com.example.data.local.entity.ArtistProfileEntity
import com.example.data.local.entity.ReviewFeedbackEntity
import com.example.data.local.entity.TrackEntity
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ExchangeResult {
    data class Success(
        val sparklesEarned: Int,
        val followedSpotify: Boolean,
        val followedInstagram: Boolean,
        val message: String
    ) : ExchangeResult()
    data class LimitReached(val reason: String) : ExchangeResult()
}

class StemSpireRepository(
    private val context: Context? = null,
    private val database: StemSpireDatabase? = null
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val roomDb: StemSpireDatabase? = database ?: context?.let { StemSpireDatabase.getInstance(it) }

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // In-memory fallback / cache
    private val _artistProfile = MutableStateFlow(createDefaultProfile())
    val artistProfile: StateFlow<ArtistProfile> = _artistProfile.asStateFlow()

    private val _exchangeTracks = MutableStateFlow<List<ExchangeTrack>>(createInitialTracks())
    val exchangeTracks: StateFlow<List<ExchangeTrack>> = _exchangeTracks.asStateFlow()

    private val _stemPacks = MutableStateFlow<List<StemPack>>(createInitialStemPacks())
    val stemPacks: StateFlow<List<StemPack>> = _stemPacks.asStateFlow()

    private val _selectedAnalyticsTrackId = MutableStateFlow("track_1")
    val selectedAnalyticsTrackId: StateFlow<String> = _selectedAnalyticsTrackId.asStateFlow()

    init {
        if (roomDb != null) {
            initRoomPersistence(roomDb)
        }
    }

    private fun initRoomPersistence(db: StemSpireDatabase) {
        repositoryScope.launch {
            // Seed Profile if missing
            val existingProfile = db.artistProfileDao().getProfile()
            if (existingProfile == null) {
                val defaultEntity = ArtistProfileEntity.fromDomain(createDefaultProfile())
                    .copy(quotaDate = todayDateString)
                db.artistProfileDao().insertOrUpdateProfile(defaultEntity)
            } else if (existingProfile.quotaDate != todayDateString) {
                // New day: reset daily limits (500 plays, 100 Spotify followers, 50 Instagram followers)
                db.artistProfileDao().resetDailyLimits(dateStr = todayDateString)
            }

            // Seed Tracks if missing
            val trackCount = db.trackDao().getTrackCount()
            if (trackCount == 0) {
                val initialTracks = createInitialTracks()
                val trackEntities = initialTracks.map { TrackEntity.fromDomain(it) }
                db.trackDao().insertTracks(trackEntities)

                // Seed initial reviews
                val initialReviews = initialTracks.flatMap { track ->
                    track.reviews.map { rev -> ReviewFeedbackEntity.fromDomain(rev, track.id) }
                }
                if (initialReviews.isNotEmpty()) {
                    db.reviewFeedbackDao().insertReviews(initialReviews)
                }
            }

            // Observe Profile flow from Room
            db.artistProfileDao().getProfileFlow().filterNotNull().collect { entity ->
                val domain = entity.toDomain()
                _artistProfile.value = domain
            }
        }

        // Observe Tracks flow combined with reviews from Room
        repositoryScope.launch {
            combine(
                db.trackDao().getAllTracksFlow(),
                db.reviewFeedbackDao().getAllReviewsFlow()
            ) { trackEntities, reviewEntities ->
                val reviewsByTrack = reviewEntities.groupBy { it.trackId }
                trackEntities.map { entity ->
                    val reviews = reviewsByTrack[entity.id]?.map { it.toDomain() } ?: emptyList()
                    entity.toDomain(reviews)
                }
            }.collect { combinedTracks ->
                if (combinedTracks.isNotEmpty()) {
                    _exchangeTracks.value = combinedTracks
                }
            }
        }
    }

    fun selectAnalyticsTrack(trackId: String) {
        _selectedAnalyticsTrackId.value = trackId
    }

    fun updateProfile(name: String, handle: String, bio: String, spotifyUrl: String, instagramHandle: String = "@kairos_producer") {
        val cleanInstagramHandle = if (instagramHandle.startsWith("@")) instagramHandle else "@$instagramHandle"
        val instagramUrl = "https://instagram.com/${cleanInstagramHandle.removePrefix("@")}"

        _artistProfile.update { current ->
            current.copy(
                name = name,
                handle = handle,
                bio = bio,
                spotifyUrl = spotifyUrl,
                instagramHandle = cleanInstagramHandle,
                instagramUrl = instagramUrl
            )
        }
        roomDb?.let { db ->
            repositoryScope.launch {
                db.artistProfileDao().updateProfileFields(
                    name = name,
                    handle = handle,
                    bio = bio,
                    spotifyUrl = spotifyUrl,
                    instagramHandle = cleanInstagramHandle,
                    instagramUrl = instagramUrl
                )
            }
        }
    }

    fun connectSpotify(artistName: String, spotifyUrl: String, email: String) {
        val cleanUri = if (spotifyUrl.contains("artist/")) {
            val id = spotifyUrl.substringAfter("artist/").substringBefore("?").substringBefore("/")
            "spotify:artist:$id"
        } else {
            "spotify:artist:kairos_producer"
        }

        _artistProfile.update { current ->
            current.copy(
                name = if (artistName.isNotBlank()) artistName else current.name,
                spotifyUrl = if (spotifyUrl.isNotBlank()) spotifyUrl else current.spotifyUrl,
                spotifyUri = cleanUri,
                spotifyAccountEmail = email,
                isSpotifyConnected = true,
                isVerified = true,
                organicSafetyScore = 99
            )
        }

        roomDb?.let { db ->
            repositoryScope.launch {
                db.artistProfileDao().updateSpotifyConnection(
                    isConnected = true,
                    email = email,
                    spotifyUrl = if (spotifyUrl.isNotBlank()) spotifyUrl else _artistProfile.value.spotifyUrl,
                    artistName = artistName
                )
            }
        }
    }

    fun disconnectSpotify() {
        _artistProfile.update { current ->
            current.copy(
                isSpotifyConnected = false,
                spotifyAccountEmail = ""
            )
        }
        roomDb?.let { db ->
            repositoryScope.launch {
                db.artistProfileDao().updateSpotifyConnection(
                    isConnected = false,
                    email = "",
                    spotifyUrl = _artistProfile.value.spotifyUrl,
                    artistName = ""
                )
            }
        }
    }

    fun earnCredits(amount: Int) {
        _artistProfile.update { current ->
            current.copy(
                sparklesCredits = current.sparklesCredits + amount,
                playsGiven = current.playsGiven + 1
            )
        }
    }

    fun spendCredits(amount: Int): Boolean {
        if (_artistProfile.value.sparklesCredits < amount) return false
        _artistProfile.update { current ->
            current.copy(sparklesCredits = current.sparklesCredits - amount)
        }
        roomDb?.let { db ->
            repositoryScope.launch {
                db.artistProfileDao().spendCredits(amount = amount)
            }
        }
        return true
    }

    fun submitTrackToExchange(
        title: String,
        spotifyUrlOrId: String,
        genre: String,
        bpm: Int,
        musicalKey: String,
        durationSeconds: Int,
        stakedCredits: Int,
        instagramHandle: String = ""
    ): Boolean {
        if (!spendCredits(stakedCredits)) return false

        val targetPlays = (stakedCredits * 1.5).toInt().coerceAtLeast(5)
        val cleanId = if (spotifyUrlOrId.contains("spotify.com/track/")) {
            spotifyUrlOrId.substringAfter("track/").substringBefore("?")
        } else if (spotifyUrlOrId.startsWith("spotify:track:")) {
            spotifyUrlOrId.removePrefix("spotify:track:")
        } else {
            spotifyUrlOrId.ifBlank { "track_user_${System.currentTimeMillis()}" }
        }

        val cleanIg = if (instagramHandle.isNotBlank()) {
            if (instagramHandle.startsWith("@")) instagramHandle else "@$instagramHandle"
        } else _artistProfile.value.instagramHandle

        val igUrl = "https://instagram.com/${cleanIg.removePrefix("@")}"

        val newTrack = ExchangeTrack(
            id = "user_track_${System.currentTimeMillis()}",
            title = title,
            artistName = _artistProfile.value.name,
            artistHandle = _artistProfile.value.handle,
            spotifyTrackId = cleanId,
            spotifyUri = "spotify:track:$cleanId",
            spotifyWebUrl = "https://open.spotify.com/track/$cleanId",
            instagramHandle = cleanIg,
            instagramUrl = igUrl,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            genre = genre,
            bpm = bpm,
            musicalKey = musicalKey,
            durationSeconds = durationSeconds,
            requiredListenSeconds = 30,
            playsReceived = 0,
            targetPlays = targetPlays,
            stakedCredits = stakedCredits,
            isFavorited = false,
            hasReviewed = false,
            algorithmReadiness = 78,
            releaseDate = "Just now"
        )

        _exchangeTracks.update { listOf(newTrack) + it }

        roomDb?.let { db ->
            repositoryScope.launch {
                db.trackDao().insertTrack(TrackEntity.fromDomain(newTrack, isUserSubmitted = true))
            }
        }
        return true
    }

    fun completeReviewAndExchange(
        trackId: String,
        ratingOverall: Float,
        mixScore: Float,
        melodyScore: Float,
        vocalScore: Float,
        comment: String,
        followedOnSpotify: Boolean,
        followedOnInstagram: Boolean = false
    ): ExchangeResult {
        val currentProfile = _artistProfile.value

        // Enforce daily limits: 500 plays exchange per day
        if (currentProfile.dailyPlaysUsed >= currentProfile.dailyPlaysLimit) {
            return ExchangeResult.LimitReached(
                "Daily limit of ${currentProfile.dailyPlaysLimit} play exchanges reached! Quota resets at midnight to protect your Spotify account health."
            )
        }

        // Enforce daily Spotify followers limit: 100 followers per day
        val canFollowSpotifyToday = !followedOnSpotify || (currentProfile.dailyFollowersUsed < currentProfile.dailyFollowersLimit)
        val finalSpotifyFollowed = followedOnSpotify && canFollowSpotifyToday

        // Enforce daily Instagram safe limit: 50 followers per day to avoid Instagram rate limit / action blocks
        val canFollowInstagramToday = !followedOnInstagram || (currentProfile.dailyInstagramFollowersUsed < currentProfile.dailyInstagramFollowersLimit)
        val finalInstagramFollowed = followedOnInstagram && canFollowInstagramToday

        val newReview = ReviewFeedback(
            id = "rev_${System.currentTimeMillis()}",
            reviewerName = currentProfile.name,
            reviewerHandle = currentProfile.handle,
            ratingOverall = ratingOverall,
            mixProductionRating = mixScore,
            melodyRating = melodyScore,
            vocalRating = vocalScore,
            comment = comment.ifBlank { "Great mix and nice groove! Added to my personal playlist." },
            followedSpotify = finalSpotifyFollowed,
            followedInstagram = finalInstagramFollowed,
            timeAgo = "Just now"
        )

        var bonus = 15 // Base 30s verified stream
        if (finalSpotifyFollowed) bonus += 5
        if (finalInstagramFollowed) bonus += 5

        _artistProfile.update { current ->
            current.copy(
                sparklesCredits = current.sparklesCredits + bonus,
                playsGiven = current.playsGiven + 1,
                dailyPlaysUsed = current.dailyPlaysUsed + 1,
                dailyFollowersUsed = if (finalSpotifyFollowed) current.dailyFollowersUsed + 1 else current.dailyFollowersUsed,
                dailyInstagramFollowersUsed = if (finalInstagramFollowed) current.dailyInstagramFollowersUsed + 1 else current.dailyInstagramFollowersUsed
            )
        }

        _exchangeTracks.update { list ->
            list.map { track ->
                if (track.id == trackId) {
                    track.copy(
                        playsReceived = track.playsReceived + 1,
                        hasReviewed = true,
                        reviews = listOf(newReview) + track.reviews
                    )
                } else track
            }
        }

        // Room database persistence
        roomDb?.let { db ->
            repositoryScope.launch {
                db.artistProfileDao().recordExchangePlay(
                    amount = bonus,
                    followedSpotify = finalSpotifyFollowed,
                    followedInstagram = finalInstagramFollowed
                )
                db.trackDao().markReviewedAndIncrementPlay(trackId)
                db.reviewFeedbackDao().insertReview(ReviewFeedbackEntity.fromDomain(newReview, trackId))
            }
        }

        val bonusDetails = mutableListOf("15 stream")
        if (finalSpotifyFollowed) bonusDetails.add("5 Spotify follow")
        if (finalInstagramFollowed) bonusDetails.add("5 Instagram follow")

        val message = "🎉 +$bonus Sparkles earned! (${bonusDetails.joinToString(" + ")})"

        return ExchangeResult.Success(
            sparklesEarned = bonus,
            followedSpotify = finalSpotifyFollowed,
            followedInstagram = finalInstagramFollowed,
            message = message
        )
    }

    fun toggleFavoriteTrack(trackId: String) {
        _exchangeTracks.update { list ->
            list.map { if (it.id == trackId) it.copy(isFavorited = !it.isFavorited) else it }
        }
        roomDb?.let { db ->
            repositoryScope.launch {
                db.trackDao().toggleFavorite(trackId)
            }
        }
    }

    fun unlockStemPack(packId: String): Boolean {
        val pack = _stemPacks.value.find { it.id == packId } ?: return false
        if (pack.isUnlocked) return true
        val cost = 25
        if (!spendCredits(cost)) return false

        _stemPacks.update { list ->
            list.map { if (it.id == packId) it.copy(isUnlocked = true, downloadCount = it.downloadCount + 1) else it }
        }
        return true
    }

    fun uploadStemPack(
        title: String,
        genre: String,
        bpm: Int,
        musicalKey: String,
        description: String,
        stems: List<AudioStem>
    ) {
        val newPack = StemPack(
            id = "pack_user_${System.currentTimeMillis()}",
            title = title,
            artistName = _artistProfile.value.name,
            genre = genre,
            bpm = bpm,
            musicalKey = musicalKey,
            coverUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=600&auto=format&fit=crop&q=80",
            description = description,
            stems = stems,
            tags = listOf(genre, "${bpm}BPM", musicalKey, "CollabReady"),
            isUnlocked = true,
            downloadCount = 0
        )
        _stemPacks.update { listOf(newPack) + it }
        _artistProfile.update {
            it.copy(
                stemsSharedCount = it.stemsSharedCount + 1,
                sparklesCredits = it.sparklesCredits + 30
            )
        }
        roomDb?.let { db ->
            repositoryScope.launch {
                db.artistProfileDao().recordStemUpload(amount = 30)
            }
        }
    }

    fun getAnalyticsForTrack(trackId: String): TrackAnalytics {
        return when (trackId) {
            "track_1" -> TrackAnalytics(
                trackId = "track_1",
                trackTitle = "Neon Horizon",
                artistName = "Astral Drift",
                totalStreams = 34920,
                monthlyListeners = 12450,
                saveRatePercent = 14.8f,
                completionRatePercent = 78.4f,
                skipRate30sPercent = 12.2f,
                discoverWeeklyScore = 88,
                releaseRadarScore = 92,
                streamHistory = listOf(
                    DailyStreamPoint("Mon", 1420, 110),
                    DailyStreamPoint("Tue", 1680, 145),
                    DailyStreamPoint("Wed", 1950, 180),
                    DailyStreamPoint("Thu", 2310, 240),
                    DailyStreamPoint("Fri", 3450, 420),
                    DailyStreamPoint("Sat", 4120, 510),
                    DailyStreamPoint("Sun", 3890, 460)
                ),
                listenerSources = listOf(
                    TrafficSource("Discover Weekly", 38f, 0xFF1DB954),
                    TrafficSource("Release Radar", 26f, 0xFF00E5FF),
                    TrafficSource("StemSpire Exchange", 18f, 0xFFFFB800),
                    TrafficSource("User Playlists", 12f, 0xFFB388FF),
                    TrafficSource("Direct & Profile", 6f, 0xFFFF5376)
                ),
                retentionCurve = listOf(
                    RetentionPoint(0, 100f),
                    RetentionPoint(15, 94f),
                    RetentionPoint(30, 88f),
                    RetentionPoint(60, 82f),
                    RetentionPoint(120, 78f),
                    RetentionPoint(180, 74f)
                ),
                acousticFingerprint = AcousticFingerprint(
                    bpm = 124,
                    musicalKey = "E Minor",
                    energyScore = 82,
                    danceabilityScore = 76,
                    valenceScore = 64,
                    loudnessLufs = -6.4f,
                    acousticnessScore = 14
                ),
                editorialPitchSummary = "High-energy synthwave anthem blending analog Roland Juno pads with driving modern punchy 808 bass and nostalgic vocal hooks. Tailor-made for 'Night Drive' and 'Retro Gaming' editorial playlists."
            )
            "track_2" -> TrackAnalytics(
                trackId = "track_2",
                trackTitle = "Midnight Mirage",
                artistName = "Luna Rayne",
                totalStreams = 18450,
                monthlyListeners = 7620,
                saveRatePercent = 16.2f,
                completionRatePercent = 81.0f,
                skipRate30sPercent = 9.8f,
                discoverWeeklyScore = 91,
                releaseRadarScore = 85,
                streamHistory = listOf(
                    DailyStreamPoint("Mon", 850, 70),
                    DailyStreamPoint("Tue", 990, 95),
                    DailyStreamPoint("Wed", 1120, 120),
                    DailyStreamPoint("Thu", 1340, 160),
                    DailyStreamPoint("Fri", 1890, 230),
                    DailyStreamPoint("Sat", 2150, 280),
                    DailyStreamPoint("Sun", 2010, 260)
                ),
                listenerSources = listOf(
                    TrafficSource("Discover Weekly", 44f, 0xFF1DB954),
                    TrafficSource("StemSpire Exchange", 24f, 0xFFFFB800),
                    TrafficSource("Release Radar", 18f, 0xFF00E5FF),
                    TrafficSource("User Playlists", 10f, 0xFFB388FF),
                    TrafficSource("Direct", 4f, 0xFFFF5376)
                ),
                retentionCurve = listOf(
                    RetentionPoint(0, 100f),
                    RetentionPoint(15, 96f),
                    RetentionPoint(30, 91f),
                    RetentionPoint(60, 86f),
                    RetentionPoint(120, 82f),
                    RetentionPoint(180, 79f)
                ),
                acousticFingerprint = AcousticFingerprint(
                    bpm = 110,
                    musicalKey = "G Major",
                    energyScore = 68,
                    danceabilityScore = 72,
                    valenceScore = 70,
                    loudnessLufs = -7.2f,
                    acousticnessScore = 32
                ),
                editorialPitchSummary = "Dreamy indie pop groove featuring shimmering guitar textures, intimate vocal harmonies, and irresistible upbeat basslines. Recommended for 'Chill Pop' and 'Indie Arrivals'."
            )
            else -> TrackAnalytics(
                trackId = trackId,
                trackTitle = "Reverie Dreams",
                artistName = "KAIROS",
                totalStreams = 24180,
                monthlyListeners = 9140,
                saveRatePercent = 13.5f,
                completionRatePercent = 75.2f,
                skipRate30sPercent = 14.1f,
                discoverWeeklyScore = 82,
                releaseRadarScore = 87,
                streamHistory = listOf(
                    DailyStreamPoint("Mon", 1100, 80),
                    DailyStreamPoint("Tue", 1250, 110),
                    DailyStreamPoint("Wed", 1400, 130),
                    DailyStreamPoint("Thu", 1750, 190),
                    DailyStreamPoint("Fri", 2500, 310),
                    DailyStreamPoint("Sat", 2980, 380),
                    DailyStreamPoint("Sun", 2700, 340)
                ),
                listenerSources = listOf(
                    TrafficSource("Discover Weekly", 34f, 0xFF1DB954),
                    TrafficSource("StemSpire Exchange", 28f, 0xFFFFB800),
                    TrafficSource("Release Radar", 22f, 0xFF00E5FF),
                    TrafficSource("User Playlists", 11f, 0xFFB388FF),
                    TrafficSource("Direct", 5f, 0xFFFF5376)
                ),
                retentionCurve = listOf(
                    RetentionPoint(0, 100f),
                    RetentionPoint(15, 92f),
                    RetentionPoint(30, 85f),
                    RetentionPoint(60, 79f),
                    RetentionPoint(120, 74f),
                    RetentionPoint(180, 71f)
                ),
                acousticFingerprint = AcousticFingerprint(
                    bpm = 126,
                    musicalKey = "F# Minor",
                    energyScore = 79,
                    danceabilityScore = 74,
                    valenceScore = 58,
                    loudnessLufs = -6.8f,
                    acousticnessScore = 20
                ),
                editorialPitchSummary = "Atmospheric electronic banger that builds into a pulsating drop. Optimized for peak workout and electronic focus playlists."
            )
        }
    }

    private fun createDefaultProfile(): ArtistProfile {
        return ArtistProfile(
            id = "user_me",
            name = "KAIROS",
            handle = "@kairos_producer",
            bio = "Electronic & Indie music producer. Crafting melodic synths, fat 808s, and ambient vocals. Open to stem swaps and remixes!",
            spotifyUri = "spotify:artist:2Ye2Wgw4gimLv2eAKW1RJ7",
            spotifyUrl = "https://open.spotify.com/artist/2Ye2Wgw4gimLv2eAKW1RJ7",
            instagramHandle = "@kairos_producer",
            instagramUrl = "https://instagram.com/kairos_producer",
            monthlyListeners = 8420,
            followers = 1350,
            sparklesCredits = 145,
            isVerified = true,
            genres = listOf("Synthwave", "Indie Pop", "Electronic"),
            playsGiven = 38,
            playsReceived = 52,
            stemsSharedCount = 4,
            dailyPlaysLimit = 500,
            dailyPlaysUsed = 38,
            dailyFollowersLimit = 100,
            dailyFollowersUsed = 14,
            dailyInstagramFollowersLimit = 150,
            dailyInstagramFollowersUsed = 8,
            quotaDate = todayDateString,
            isSpotifyConnected = true,
            spotifyAccountEmail = "dipeshkondalkar@gmail.com",
            organicSafetyScore = 98
        )
    }

    private fun createInitialTracks(): List<ExchangeTrack> {
        return listOf(
            ExchangeTrack(
                id = "track_1",
                title = "Neon Horizon",
                artistName = "Astral Drift",
                artistHandle = "@astral_drift",
                spotifyTrackId = "4cOdK2wGLETKBW3PvgPWqT",
                spotifyUri = "spotify:track:4cOdK2wGLETKBW3PvgPWqT",
                spotifyWebUrl = "https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT",
                instagramHandle = "@astral_drift_sound",
                instagramUrl = "https://instagram.com/astral_drift_sound",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                genre = "Synthwave",
                bpm = 124,
                musicalKey = "E Minor",
                durationSeconds = 194,
                requiredListenSeconds = 30,
                playsReceived = 42,
                targetPlays = 50,
                stakedCredits = 35,
                algorithmReadiness = 88,
                releaseDate = "2 days ago",
                reviews = listOf(
                    ReviewFeedback(
                        id = "rev_1",
                        reviewerName = "EchoPulse",
                        reviewerHandle = "@echopulse",
                        ratingOverall = 4.8f,
                        mixProductionRating = 5.0f,
                        melodyRating = 4.5f,
                        vocalRating = 4.7f,
                        comment = "The snare reverb tail fits the 80s aesthetic perfectly. Followed on Spotify & IG!",
                        followedSpotify = true,
                        followedInstagram = true,
                        timeAgo = "3h ago"
                    ),
                    ReviewFeedback(
                        id = "rev_2",
                        reviewerName = "VocalVibe",
                        reviewerHandle = "@vocalvibe",
                        ratingOverall = 4.6f,
                        mixProductionRating = 4.5f,
                        melodyRating = 4.8f,
                        vocalRating = 4.5f,
                        comment = "Catchy synth arpeggiation. I would love to collab on the stems if you have the vocal acapella available!",
                        followedSpotify = true,
                        followedInstagram = true,
                        timeAgo = "7h ago"
                    )
                )
            ),
            ExchangeTrack(
                id = "track_2",
                title = "Midnight Mirage",
                artistName = "Luna Rayne",
                artistHandle = "@lunar_music",
                spotifyTrackId = "0VjIjW4GlUZAMYd2vXMi3b",
                spotifyUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
                spotifyWebUrl = "https://open.spotify.com/track/0VjIjW4GlUZAMYd2vXMi3b",
                instagramHandle = "@lunar_rayne",
                instagramUrl = "https://instagram.com/lunar_rayne",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                genre = "Indie Pop",
                bpm = 110,
                musicalKey = "G Major",
                durationSeconds = 182,
                requiredListenSeconds = 30,
                playsReceived = 68,
                targetPlays = 80,
                stakedCredits = 50,
                algorithmReadiness = 91,
                releaseDate = "3 days ago",
                reviews = listOf(
                    ReviewFeedback(
                        id = "rev_3",
                        reviewerName = "BeatSmith",
                        reviewerHandle = "@beatsmith",
                        ratingOverall = 4.9f,
                        mixProductionRating = 5.0f,
                        melodyRating = 5.0f,
                        vocalRating = 4.8f,
                        comment = "Incredible vocal presence and silky top-end sheen. Followed both accounts!",
                        followedSpotify = true,
                        followedInstagram = true,
                        timeAgo = "1d ago"
                    )
                )
            ),
            ExchangeTrack(
                id = "track_3",
                title = "Ghost Protocol",
                artistName = "CyberX",
                artistHandle = "@cyberx_sound",
                spotifyTrackId = "7qiZfU4dY1lWllzX7mPBI3",
                spotifyUri = "spotify:track:7qiZfU4dY1lWllzX7mPBI3",
                spotifyWebUrl = "https://open.spotify.com/track/7qiZfU4dY1lWllzX7mPBI3",
                instagramHandle = "@cyberx_producer",
                instagramUrl = "https://instagram.com/cyberx_producer",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                genre = "Melodic Techno",
                bpm = 130,
                musicalKey = "D Minor",
                durationSeconds = 245,
                requiredListenSeconds = 30,
                playsReceived = 31,
                targetPlays = 60,
                stakedCredits = 40,
                algorithmReadiness = 84,
                releaseDate = "5 days ago",
                reviews = emptyList()
            ),
            ExchangeTrack(
                id = "track_4",
                title = "Golden Hour Session",
                artistName = "Marcus Cole",
                artistHandle = "@marcus_chill",
                spotifyTrackId = "3n3Ppam7vgaVa1iaRUc9Lp",
                spotifyUri = "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp",
                spotifyWebUrl = "https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp",
                instagramHandle = "@marcus_chillbeats",
                instagramUrl = "https://instagram.com/marcus_chillbeats",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                genre = "Lo-Fi Hip Hop",
                bpm = 86,
                musicalKey = "C Major",
                durationSeconds = 158,
                requiredListenSeconds = 30,
                playsReceived = 89,
                targetPlays = 100,
                stakedCredits = 60,
                algorithmReadiness = 94,
                releaseDate = "1 week ago",
                reviews = emptyList()
            ),
            ExchangeTrack(
                id = "track_5",
                title = "Reverie Dreams",
                artistName = "KAIROS",
                artistHandle = "@kairos_producer",
                spotifyTrackId = "2TpxZ7JUBn3uw46aR7qd6V",
                spotifyUri = "spotify:track:2TpxZ7JUBn3uw46aR7qd6V",
                spotifyWebUrl = "https://open.spotify.com/track/2TpxZ7JUBn3uw46aR7qd6V",
                instagramHandle = "@kairos_producer",
                instagramUrl = "https://instagram.com/kairos_producer",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                genre = "Electronic",
                bpm = 126,
                musicalKey = "F# Minor",
                durationSeconds = 210,
                requiredListenSeconds = 30,
                playsReceived = 52,
                targetPlays = 75,
                stakedCredits = 45,
                algorithmReadiness = 82,
                releaseDate = "Active Campaign",
                reviews = emptyList()
            )
        )
    }

    private fun createInitialStemPacks(): List<StemPack> {
        val dummyWaveformA = listOf(0.2f, 0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 0.4f, 0.6f, 0.8f, 0.3f, 0.7f, 0.5f, 0.9f, 0.6f, 0.2f)
        val dummyWaveformB = listOf(0.9f, 0.2f, 0.7f, 0.9f, 0.2f, 0.8f, 0.9f, 0.3f, 0.8f, 0.9f, 0.2f, 0.7f, 0.9f, 0.3f, 0.8f, 0.9f)
        val dummyWaveformC = listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.6f, 0.8f, 0.5f, 0.7f, 0.4f, 0.6f, 0.5f, 0.7f, 0.6f, 0.8f, 0.5f, 0.6f)
        val dummyWaveformD = listOf(0.6f, 0.3f, 0.5f, 0.8f, 0.4f, 0.7f, 0.6f, 0.9f, 0.5f, 0.7f, 0.4f, 0.8f, 0.5f, 0.6f, 0.7f, 0.4f)

        return listOf(
            StemPack(
                id = "pack_1",
                title = "Neon Nights Stems & Acapella",
                artistName = "Astral Drift",
                genre = "Synthwave",
                bpm = 124,
                musicalKey = "E Minor",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                description = "Full studio stems from Spotify release 'Neon Horizon'. High quality 24-bit WAV stems including raw & tuned vocal hook, analog Juno chords, analog drum machine, and Moog sub bass.",
                tags = listOf("Synthwave", "124BPM", "Vocals", "Moog Bass", "RoyaltyFree"),
                isUnlocked = true,
                downloadCount = 142,
                stems = listOf(
                    AudioStem("stem_1_1", "pack_1", "Vocal Hook (Dry & Tuned)", StemType.VOCALS, "E Minor", 124, 194, 15, 88, dummyWaveformA),
                    AudioStem("stem_1_2", "pack_1", "80s Retro Drums & Snare", StemType.DRUMS, "E Minor", 124, 194, 10, 102, dummyWaveformB),
                    AudioStem("stem_1_3", "pack_1", "Moog Sub Bass Line", StemType.BASS, "E Minor", 124, 194, 10, 94, dummyWaveformC),
                    AudioStem("stem_1_4", "pack_1", "Juno Synth Chords & Arp", StemType.SYNTH_LEAD, "E Minor", 124, 194, 10, 115, dummyWaveformD)
                )
            ),
            StemPack(
                id = "pack_2",
                title = "Silky R&B & Soul Stems",
                artistName = "Luna Rayne",
                genre = "R&B / Pop",
                bpm = 110,
                musicalKey = "G Major",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                description = "Intimate studio vocal harmonies, Rhodes electric piano chord progressions, and modern crisp 808 percussion.",
                tags = listOf("R&B", "Female Vocals", "Rhodes", "110BPM", "G Major"),
                isUnlocked = false,
                downloadCount = 210,
                stems = listOf(
                    AudioStem("stem_2_1", "pack_2", "Lead Vocal + Harmony Stems", StemType.VOCALS, "G Major", 110, 182, 20, 190, dummyWaveformA),
                    AudioStem("stem_2_2", "pack_2", "Rhodes Piano Chords", StemType.SYNTH_LEAD, "G Major", 110, 182, 10, 140, dummyWaveformD),
                    AudioStem("stem_2_3", "pack_2", "Warm Neo-Soul Bass", StemType.BASS, "G Major", 110, 182, 10, 120, dummyWaveformC),
                    AudioStem("stem_2_4", "pack_2", "Crisp Trap Soul Drums", StemType.DRUMS, "G Major", 110, 182, 10, 160, dummyWaveformB)
                )
            ),
            StemPack(
                id = "pack_3",
                title = "Deep Club Cyber Stems",
                artistName = "CyberX",
                genre = "Melodic Techno",
                bpm = 130,
                musicalKey = "D Minor",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                description = "Dark driving club weapons. Pumping rolling bass, punchy 909 kick & claps, tension build-up risers and vocal chops.",
                tags = listOf("Techno", "Club", "130BPM", "Rolling Bass", "Festival"),
                isUnlocked = false,
                downloadCount = 95,
                stems = listOf(
                    AudioStem("stem_3_1", "pack_3", "Heavy 909 Kick & Hats", StemType.DRUMS, "D Minor", 130, 245, 10, 80, dummyWaveformB),
                    AudioStem("stem_3_2", "pack_3", "Rolling Cyber Acid Bass", StemType.BASS, "D Minor", 130, 245, 10, 92, dummyWaveformC),
                    AudioStem("stem_3_3", "pack_3", "Peak Time Lead Saw Synth", StemType.SYNTH_LEAD, "D Minor", 130, 245, 10, 75, dummyWaveformD),
                    AudioStem("stem_3_4", "pack_3", "Atmospheric Risers & FX", StemType.FX_PERC, "D Minor", 130, 245, 10, 64, dummyWaveformA)
                )
            ),
            StemPack(
                id = "pack_4",
                title = "Lo-Fi Midnight Vinyl Stems",
                artistName = "Marcus Cole",
                genre = "Lo-Fi Hip Hop",
                bpm = 86,
                musicalKey = "C Major",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                description = "Warmed on real reel-to-reel tape. Vinyl crackle, mellow jazz guitar licks, pitched vocal phrase chops, and dusty boom bap drums.",
                tags = listOf("LoFi", "StudyBeats", "Guitar", "Vinyl", "BoomBap"),
                isUnlocked = true,
                downloadCount = 330,
                stems = listOf(
                    AudioStem("stem_4_1", "pack_4", "Mellow Jazz Guitar Licks", StemType.GUITAR, "C Major", 86, 158, 10, 240, dummyWaveformD),
                    AudioStem("stem_4_2", "pack_4", "Dusty Boom Bap Drum Kit", StemType.DRUMS, "C Major", 86, 158, 10, 290, dummyWaveformB),
                    AudioStem("stem_4_3", "pack_4", "Upright Acoustic Bass", StemType.BASS, "C Major", 86, 158, 10, 210, dummyWaveformC),
                    AudioStem("stem_4_4", "pack_4", "Vinyl Crackle & Rain FX", StemType.FX_PERC, "C Major", 86, 158, 5, 305, dummyWaveformA)
                )
            )
        )
    }
}

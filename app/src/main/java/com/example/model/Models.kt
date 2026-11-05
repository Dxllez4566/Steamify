package com.example.model

enum class StemType(val displayName: String, val badgeColorHex: Long) {
    VOCALS("Vocals / Acapella", 0xFFFF5376),
    DRUMS("Drums & 808", 0xFFFFB800),
    BASS("Sub & Bassline", 0xFF00E5FF),
    SYNTH_LEAD("Melody & Synth", 0xFFB388FF),
    GUITAR("Guitar Riffs", 0xFFFF7043),
    FX_PERC("FX & Ambience", 0xFF00E676)
}

data class ArtistProfile(
    val id: String,
    val name: String,
    val handle: String,
    val bio: String,
    val spotifyUri: String,
    val spotifyUrl: String,
    val instagramHandle: String = "@kairos_producer",
    val instagramUrl: String = "https://instagram.com/kairos_producer",
    val monthlyListeners: Int,
    val followers: Int,
    val sparklesCredits: Int,
    val isVerified: Boolean,
    val genres: List<String>,
    val playsGiven: Int,
    val playsReceived: Int,
    val stemsSharedCount: Int,
    val dailyPlaysLimit: Int = 500,
    val dailyPlaysUsed: Int = 38,
    val dailyFollowersLimit: Int = 100,
    val dailyFollowersUsed: Int = 14,
    val dailyInstagramFollowersLimit: Int = 150,
    val dailyInstagramFollowersUsed: Int = 8,
    val quotaDate: String = "",
    val isSpotifyConnected: Boolean = true,
    val spotifyAccountEmail: String = "dipeshkondalkar@gmail.com",
    val organicSafetyScore: Int = 98 // 0-100 algorithm safety compliance score
)

data class ReviewFeedback(
    val id: String,
    val reviewerName: String,
    val reviewerHandle: String,
    val ratingOverall: Float,
    val mixProductionRating: Float,
    val melodyRating: Float,
    val vocalRating: Float,
    val comment: String,
    val followedSpotify: Boolean,
    val followedInstagram: Boolean = false,
    val timeAgo: String
)

data class ExchangeTrack(
    val id: String,
    val title: String,
    val artistName: String,
    val artistHandle: String,
    val spotifyTrackId: String,
    val spotifyUri: String,
    val spotifyWebUrl: String,
    val instagramHandle: String = "",
    val instagramUrl: String = "",
    val coverUrl: String,
    val genre: String,
    val bpm: Int,
    val musicalKey: String,
    val durationSeconds: Int,
    val requiredListenSeconds: Int = 30,
    val playsReceived: Int,
    val targetPlays: Int,
    val stakedCredits: Int,
    val isFavorited: Boolean = false,
    val hasReviewed: Boolean = false,
    val algorithmReadiness: Int, // 0 - 100
    val releaseDate: String,
    val reviews: List<ReviewFeedback> = emptyList()
)

data class AudioStem(
    val id: String,
    val packId: String,
    val name: String,
    val stemType: StemType,
    val musicalKey: String,
    val bpm: Int,
    val durationSeconds: Int,
    val creditCost: Int,
    val downloadCount: Int,
    val waveformPoints: List<Float>
)

data class StemPack(
    val id: String,
    val title: String,
    val artistName: String,
    val genre: String,
    val bpm: Int,
    val musicalKey: String,
    val coverUrl: String,
    val description: String,
    val stems: List<AudioStem>,
    val tags: List<String>,
    val isUnlocked: Boolean = false,
    val downloadCount: Int
)

data class DailyStreamPoint(
    val dayLabel: String,
    val streams: Int,
    val organicAdds: Int
)

data class TrafficSource(
    val name: String,
    val percent: Float,
    val colorHex: Long
)

data class RetentionPoint(
    val timestampSeconds: Int,
    val retentionPercent: Float
)

data class AcousticFingerprint(
    val bpm: Int,
    val musicalKey: String,
    val energyScore: Int, // 0-100
    val danceabilityScore: Int, // 0-100
    val valenceScore: Int, // 0-100 (mood positivity)
    val loudnessLufs: Float, // e.g. -6.8 LUFS
    val acousticnessScore: Int // 0-100
)

data class TrackAnalytics(
    val trackId: String,
    val trackTitle: String,
    val artistName: String,
    val totalStreams: Int,
    val monthlyListeners: Int,
    val saveRatePercent: Float,
    val completionRatePercent: Float,
    val skipRate30sPercent: Float,
    val discoverWeeklyScore: Int,
    val releaseRadarScore: Int,
    val streamHistory: List<DailyStreamPoint>,
    val listenerSources: List<TrafficSource>,
    val retentionCurve: List<RetentionPoint>,
    val acousticFingerprint: AcousticFingerprint,
    val editorialPitchSummary: String
)

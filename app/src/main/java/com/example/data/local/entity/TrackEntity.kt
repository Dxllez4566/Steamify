package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ExchangeTrack
import com.example.model.ReviewFeedback

@Entity(tableName = "spotify_tracks")
data class TrackEntity(
    @PrimaryKey
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
    val algorithmReadiness: Int,
    val releaseDate: String,
    val isUserSubmitted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(reviews: List<ReviewFeedback> = emptyList()): ExchangeTrack {
        return ExchangeTrack(
            id = id,
            title = title,
            artistName = artistName,
            artistHandle = artistHandle,
            spotifyTrackId = spotifyTrackId,
            spotifyUri = spotifyUri,
            spotifyWebUrl = spotifyWebUrl,
            instagramHandle = instagramHandle,
            instagramUrl = instagramUrl,
            coverUrl = coverUrl,
            genre = genre,
            bpm = bpm,
            musicalKey = musicalKey,
            durationSeconds = durationSeconds,
            requiredListenSeconds = requiredListenSeconds,
            playsReceived = playsReceived,
            targetPlays = targetPlays,
            stakedCredits = stakedCredits,
            isFavorited = isFavorited,
            hasReviewed = hasReviewed,
            algorithmReadiness = algorithmReadiness,
            releaseDate = releaseDate,
            reviews = reviews
        )
    }

    companion object {
        fun fromDomain(domain: ExchangeTrack, isUserSubmitted: Boolean = false): TrackEntity {
            return TrackEntity(
                id = domain.id,
                title = domain.title,
                artistName = domain.artistName,
                artistHandle = domain.artistHandle,
                spotifyTrackId = domain.spotifyTrackId,
                spotifyUri = domain.spotifyUri,
                spotifyWebUrl = domain.spotifyWebUrl,
                instagramHandle = domain.instagramHandle,
                instagramUrl = domain.instagramUrl,
                coverUrl = domain.coverUrl,
                genre = domain.genre,
                bpm = domain.bpm,
                musicalKey = domain.musicalKey,
                durationSeconds = domain.durationSeconds,
                requiredListenSeconds = domain.requiredListenSeconds,
                playsReceived = domain.playsReceived,
                targetPlays = domain.targetPlays,
                stakedCredits = domain.stakedCredits,
                isFavorited = domain.isFavorited,
                hasReviewed = domain.hasReviewed,
                algorithmReadiness = domain.algorithmReadiness,
                releaseDate = domain.releaseDate,
                isUserSubmitted = isUserSubmitted
            )
        }
    }
}

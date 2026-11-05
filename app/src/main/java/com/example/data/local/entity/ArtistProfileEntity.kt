package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ArtistProfile

@Entity(tableName = "artist_profile")
data class ArtistProfileEntity(
    @PrimaryKey
    val id: String = "user_me",
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
    val genresDelimited: String,
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
    val organicSafetyScore: Int = 98,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ArtistProfile {
        return ArtistProfile(
            id = id,
            name = name,
            handle = handle,
            bio = bio,
            spotifyUri = spotifyUri,
            spotifyUrl = spotifyUrl,
            instagramHandle = instagramHandle,
            instagramUrl = instagramUrl,
            monthlyListeners = monthlyListeners,
            followers = followers,
            sparklesCredits = sparklesCredits,
            isVerified = isVerified,
            genres = genresDelimited.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            playsGiven = playsGiven,
            playsReceived = playsReceived,
            stemsSharedCount = stemsSharedCount,
            dailyPlaysLimit = dailyPlaysLimit,
            dailyPlaysUsed = dailyPlaysUsed,
            dailyFollowersLimit = dailyFollowersLimit,
            dailyFollowersUsed = dailyFollowersUsed,
            dailyInstagramFollowersLimit = dailyInstagramFollowersLimit,
            dailyInstagramFollowersUsed = dailyInstagramFollowersUsed,
            quotaDate = quotaDate,
            isSpotifyConnected = isSpotifyConnected,
            spotifyAccountEmail = spotifyAccountEmail,
            organicSafetyScore = organicSafetyScore
        )
    }

    companion object {
        fun fromDomain(domain: ArtistProfile): ArtistProfileEntity {
            return ArtistProfileEntity(
                id = domain.id,
                name = domain.name,
                handle = domain.handle,
                bio = domain.bio,
                spotifyUri = domain.spotifyUri,
                spotifyUrl = domain.spotifyUrl,
                instagramHandle = domain.instagramHandle,
                instagramUrl = domain.instagramUrl,
                monthlyListeners = domain.monthlyListeners,
                followers = domain.followers,
                sparklesCredits = domain.sparklesCredits,
                isVerified = domain.isVerified,
                genresDelimited = domain.genres.joinToString(","),
                playsGiven = domain.playsGiven,
                playsReceived = domain.playsReceived,
                stemsSharedCount = domain.stemsSharedCount,
                dailyPlaysLimit = domain.dailyPlaysLimit,
                dailyPlaysUsed = domain.dailyPlaysUsed,
                dailyFollowersLimit = domain.dailyFollowersLimit,
                dailyFollowersUsed = domain.dailyFollowersUsed,
                dailyInstagramFollowersLimit = domain.dailyInstagramFollowersLimit,
                dailyInstagramFollowersUsed = domain.dailyInstagramFollowersUsed,
                quotaDate = domain.quotaDate,
                isSpotifyConnected = domain.isSpotifyConnected,
                spotifyAccountEmail = domain.spotifyAccountEmail,
                organicSafetyScore = domain.organicSafetyScore
            )
        }
    }
}

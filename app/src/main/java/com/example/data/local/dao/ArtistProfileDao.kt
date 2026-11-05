package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ArtistProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistProfileDao {

    @Query("SELECT * FROM artist_profile WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: String = "user_me"): Flow<ArtistProfileEntity?>

    @Query("SELECT * FROM artist_profile WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String = "user_me"): ArtistProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ArtistProfileEntity)

    @Query("UPDATE artist_profile SET name = :name, handle = :handle, bio = :bio, spotifyUrl = :spotifyUrl, instagramHandle = :instagramHandle, instagramUrl = :instagramUrl, updatedAt = :now WHERE id = :id")
    suspend fun updateProfileFields(
        id: String = "user_me",
        name: String,
        handle: String,
        bio: String,
        spotifyUrl: String,
        instagramHandle: String,
        instagramUrl: String,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE artist_profile SET isSpotifyConnected = :isConnected, spotifyAccountEmail = :email, spotifyUrl = :spotifyUrl, name = CASE WHEN :artistName != '' THEN :artistName ELSE name END, updatedAt = :now WHERE id = :id")
    suspend fun updateSpotifyConnection(
        id: String = "user_me",
        isConnected: Boolean,
        email: String,
        spotifyUrl: String,
        artistName: String,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE artist_profile SET sparklesCredits = sparklesCredits + :amount, playsGiven = playsGiven + 1, dailyPlaysUsed = dailyPlaysUsed + 1, dailyFollowersUsed = CASE WHEN :followedSpotify THEN dailyFollowersUsed + 1 ELSE dailyFollowersUsed END, dailyInstagramFollowersUsed = CASE WHEN :followedInstagram THEN dailyInstagramFollowersUsed + 1 ELSE dailyInstagramFollowersUsed END, updatedAt = :now WHERE id = :id")
    suspend fun recordExchangePlay(
        id: String = "user_me",
        amount: Int,
        followedSpotify: Boolean,
        followedInstagram: Boolean,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE artist_profile SET sparklesCredits = sparklesCredits - :amount, updatedAt = :now WHERE id = :id")
    suspend fun spendCredits(
        id: String = "user_me",
        amount: Int,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE artist_profile SET sparklesCredits = sparklesCredits + :amount, stemsSharedCount = stemsSharedCount + 1, updatedAt = :now WHERE id = :id")
    suspend fun recordStemUpload(
        id: String = "user_me",
        amount: Int = 30,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE artist_profile SET dailyPlaysUsed = 0, dailyFollowersUsed = 0, dailyInstagramFollowersUsed = 0, quotaDate = :dateStr, updatedAt = :now WHERE id = :id")
    suspend fun resetDailyLimits(
        id: String = "user_me",
        dateStr: String,
        now: Long = System.currentTimeMillis()
    )
}

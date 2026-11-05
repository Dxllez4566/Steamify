package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM spotify_tracks ORDER BY createdAt DESC")
    fun getAllTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM spotify_tracks WHERE id = :id LIMIT 1")
    fun getTrackByIdFlow(id: String): Flow<TrackEntity?>

    @Query("SELECT * FROM spotify_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("UPDATE spotify_tracks SET isFavorited = NOT isFavorited WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("UPDATE spotify_tracks SET playsReceived = playsReceived + 1, hasReviewed = 1 WHERE id = :id")
    suspend fun markReviewedAndIncrementPlay(id: String)

    @Query("SELECT COUNT(*) FROM spotify_tracks")
    suspend fun getTrackCount(): Int
}

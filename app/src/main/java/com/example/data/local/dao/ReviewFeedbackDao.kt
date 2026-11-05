package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReviewFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewFeedbackDao {

    @Query("SELECT * FROM track_reviews WHERE trackId = :trackId ORDER BY timestamp DESC")
    fun getReviewsForTrackFlow(trackId: String): Flow<List<ReviewFeedbackEntity>>

    @Query("SELECT * FROM track_reviews ORDER BY timestamp DESC")
    fun getAllReviewsFlow(): Flow<List<ReviewFeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewFeedbackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewFeedbackEntity>)
}

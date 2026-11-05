package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ReviewFeedback

@Entity(tableName = "track_reviews")
data class ReviewFeedbackEntity(
    @PrimaryKey
    val id: String,
    val trackId: String,
    val reviewerName: String,
    val reviewerHandle: String,
    val ratingOverall: Float,
    val mixProductionRating: Float,
    val melodyRating: Float,
    val vocalRating: Float,
    val comment: String,
    val followedSpotify: Boolean,
    val followedInstagram: Boolean = false,
    val timeAgo: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): ReviewFeedback {
        return ReviewFeedback(
            id = id,
            reviewerName = reviewerName,
            reviewerHandle = reviewerHandle,
            ratingOverall = ratingOverall,
            mixProductionRating = mixProductionRating,
            melodyRating = melodyRating,
            vocalRating = vocalRating,
            comment = comment,
            followedSpotify = followedSpotify,
            followedInstagram = followedInstagram,
            timeAgo = timeAgo
        )
    }

    companion object {
        fun fromDomain(domain: ReviewFeedback, trackId: String): ReviewFeedbackEntity {
            return ReviewFeedbackEntity(
                id = domain.id,
                trackId = trackId,
                reviewerName = domain.reviewerName,
                reviewerHandle = domain.reviewerHandle,
                ratingOverall = domain.ratingOverall,
                mixProductionRating = domain.mixProductionRating,
                melodyRating = domain.melodyRating,
                vocalRating = domain.vocalRating,
                comment = domain.comment,
                followedSpotify = domain.followedSpotify,
                followedInstagram = domain.followedInstagram,
                timeAgo = domain.timeAgo
            )
        }
    }
}

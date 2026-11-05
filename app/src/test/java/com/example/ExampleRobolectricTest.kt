package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ExchangeResult
import com.example.data.StemSpireRepository
import com.example.data.local.StemSpireDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StemSpire", appName)
    }

    @Test
    fun `repository initial state and credit exchange works`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = StemSpireRepository(context)
        val profile = repository.artistProfile.value
        assertEquals("KAIROS", profile.name)
        assertTrue(profile.sparklesCredits > 0)
        assertEquals(500, profile.dailyPlaysLimit)
        assertEquals(100, profile.dailyFollowersLimit)
        assertEquals(150, profile.dailyInstagramFollowersLimit)
        assertTrue(profile.isSpotifyConnected)

        val initialCredits = profile.sparklesCredits
        repository.earnCredits(20)
        assertEquals(initialCredits + 20, repository.artistProfile.value.sparklesCredits)

        val tracks = repository.exchangeTracks.value
        assertTrue(tracks.isNotEmpty())

        val stemPacks = repository.stemPacks.value
        assertTrue(stemPacks.isNotEmpty())
    }

    @Test
    fun `easy spotify login updates credentials`() {
        val repository = StemSpireRepository()
        repository.connectSpotify("Seven Bassist", "https://open.spotify.com/artist/4cOdK2wGLETKBW3PvgPWqT", "dipeshkondalkar@gmail.com")
        val updated = repository.artistProfile.value
        assertEquals("Seven Bassist", updated.name)
        assertEquals("dipeshkondalkar@gmail.com", updated.spotifyAccountEmail)
        assertTrue(updated.isSpotifyConnected)
    }

    @Test
    fun `daily limit allows exchange within quota`() {
        val repository = StemSpireRepository()
        val result = repository.completeReviewAndExchange(
            trackId = "track_1",
            ratingOverall = 4.8f,
            mixScore = 4.5f,
            melodyScore = 5.0f,
            vocalScore = 4.5f,
            comment = "Great mix!",
            followedOnSpotify = true
        )
        assertTrue(result is ExchangeResult.Success)
    }

    @Test
    fun `room database initializes cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StemSpireDatabase.getInstance(context)
        assertTrue(db != null)
    }
}

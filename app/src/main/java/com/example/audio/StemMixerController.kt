package com.example.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.StemPack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StemMixerController(private val scope: CoroutineScope) {

    var currentPack by mutableStateOf<StemPack?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentPositionSeconds by mutableFloatStateOf(0f)
        private set

    val totalDurationSeconds: Float
        get() = currentPack?.stems?.firstOrNull()?.durationSeconds?.toFloat() ?: 180f

    // Channel state maps: stemId -> state
    val muteStates = mutableStateMapOf<String, Boolean>()
    val soloStates = mutableStateMapOf<String, Boolean>()
    val volumeLevels = mutableStateMapOf<String, Float>()

    private var playbackJob: Job? = null

    fun loadPack(pack: StemPack) {
        pause()
        currentPack = pack
        currentPositionSeconds = 0f
        muteStates.clear()
        soloStates.clear()
        volumeLevels.clear()

        pack.stems.forEach { stem ->
            muteStates[stem.id] = false
            soloStates[stem.id] = false
            volumeLevels[stem.id] = 0.85f
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (currentPack == null) return
        isPlaying = true
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive && isPlaying) {
                delay(100)
                currentPositionSeconds += 0.1f
                if (currentPositionSeconds >= totalDurationSeconds) {
                    currentPositionSeconds = 0f
                }
            }
        }
    }

    fun pause() {
        isPlaying = false
        playbackJob?.cancel()
    }

    fun seekTo(seconds: Float) {
        currentPositionSeconds = seconds.coerceIn(0f, totalDurationSeconds)
    }

    fun toggleMute(stemId: String) {
        muteStates[stemId] = !(muteStates[stemId] ?: false)
    }

    fun toggleSolo(stemId: String) {
        val current = soloStates[stemId] ?: false
        soloStates[stemId] = !current
    }

    fun setVolume(stemId: String, volume: Float) {
        volumeLevels[stemId] = volume.coerceIn(0f, 1f)
    }

    fun isStemAudible(stemId: String): Boolean {
        val hasAnySolo = soloStates.values.any { it }
        if (hasAnySolo) {
            return soloStates[stemId] == true
        }
        return !(muteStates[stemId] ?: false)
    }
}

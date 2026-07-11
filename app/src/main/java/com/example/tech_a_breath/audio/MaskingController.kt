package com.example.tech_a_breath.audio

import androidx.media3.common.Player

class MaskingController {
    private var currentLevel: Int = 100

    fun setMaskingLevel(percent: Int) {
        currentLevel = percent.coerceIn(0, 100)
    }

    fun applyMasking(player: Player) {
        player.volume = currentLevel / 100f
    }

    fun getLevel(): Int = currentLevel
}

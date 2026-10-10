package com.jcbernal.serpienteneon.game

import android.content.Context
import androidx.core.content.edit

/** Guarda el récord en SharedPreferences para que sobreviva al cerrar la app. */
class HighScoreStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): Int = prefs.getInt(KEY_HIGH_SCORE, 0)

    fun save(score: Int) {
        prefs.edit { putInt(KEY_HIGH_SCORE, score) }
    }

    private companion object {
        const val PREFS_NAME = "serpiente_neon"
        const val KEY_HIGH_SCORE = "high_score"
    }
}

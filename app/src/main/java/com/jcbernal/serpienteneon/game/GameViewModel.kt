package com.jcbernal.serpienteneon.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Conecta la lógica pura ([GameEngine]) con la UI: mantiene el estado,
 * corre el bucle del juego en una corrutina y guarda el récord.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val random = Random.Default
    private val store = HighScoreStore(application)

    private val _state = MutableStateFlow(GameEngine.newGame(random))
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _highScore = MutableStateFlow(store.load())
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    /** true cuando la partida que acaba de terminar superó el récord anterior. */
    private val _newRecord = MutableStateFlow(false)
    val newRecord: StateFlow<Boolean> = _newRecord.asStateFlow()

    private var loopJob: Job? = null

    /** Gesto o botón de dirección. También arranca o reanuda la partida. */
    fun onDirection(direction: Direction) {
        val current = _state.value
        if (current.isOver) return
        val turned = GameEngine.changeDirection(current, direction)
        _state.value = when (current.status) {
            Status.READY, Status.PAUSED -> turned.copy(status = Status.RUNNING)
            else -> turned
        }
        ensureLoop()
    }

    /** Toque sobre el tablero: inicia, pausa, reanuda o reinicia según el estado. */
    fun onTap() {
        val current = _state.value
        when (current.status) {
            Status.READY, Status.PAUSED -> {
                _state.value = current.copy(status = Status.RUNNING)
                ensureLoop()
            }
            Status.RUNNING -> pause()
            Status.GAME_OVER, Status.WON -> restart()
        }
    }

    fun pause() {
        val current = _state.value
        if (current.status == Status.RUNNING) {
            _state.value = current.copy(status = Status.PAUSED)
        }
    }

    fun restart() {
        loopJob?.cancel()
        loopJob = null
        _newRecord.value = false
        _state.value = GameEngine.newGame(random)
    }

    private fun ensureLoop() {
        if (loopJob?.isActive == true) return
        if (_state.value.status != Status.RUNNING) return
        loopJob = viewModelScope.launch {
            while (isActive && _state.value.status == Status.RUNNING) {
                delay(GameEngine.tickMillis(_state.value.score))
                val next = GameEngine.tick(_state.value, random)
                _state.value = next
                if (next.isOver) onGameFinished(next.score)
            }
        }
    }

    private fun onGameFinished(score: Int) {
        if (score > _highScore.value) {
            _highScore.value = score
            _newRecord.value = true
            store.save(score)
        }
    }
}

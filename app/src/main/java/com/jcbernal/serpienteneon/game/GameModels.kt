package com.jcbernal.serpienteneon.game

/** Dirección de movimiento de la serpiente, expresada como desplazamiento en la cuadrícula. */
enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    /** true si [other] apunta exactamente al lado contrario (no se permite dar media vuelta). */
    fun isOppositeOf(other: Direction): Boolean = dx + other.dx == 0 && dy + other.dy == 0
}

/** Una celda de la cuadrícula. (0,0) es la esquina superior izquierda. */
data class Cell(val x: Int, val y: Int) {
    operator fun plus(d: Direction): Cell = Cell(x + d.dx, y + d.dy)
}

enum class Status {
    /** Partida lista; espera el primer deslizamiento o toque. */
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER,

    /** La serpiente llenó todo el tablero. */
    WON,
}

/**
 * Estado completo e inmutable de una partida.
 *
 * @property snake celdas de la serpiente; la primera es la cabeza.
 * @property direction dirección con la que se movió en el último tick.
 * @property pendingDirection dirección que se aplicará en el siguiente tick.
 */
data class GameState(
    val cols: Int,
    val rows: Int,
    val snake: List<Cell>,
    val direction: Direction,
    val pendingDirection: Direction,
    val food: Cell,
    val score: Int = 0,
    val status: Status = Status.READY,
) {
    val head: Cell get() = snake.first()
    val isOver: Boolean get() = status == Status.GAME_OVER || status == Status.WON
}

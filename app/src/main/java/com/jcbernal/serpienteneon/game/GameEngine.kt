package com.jcbernal.serpienteneon.game

import kotlin.random.Random

/**
 * Lógica pura del juego: sin Android, sin corrutinas. Cada función recibe un estado
 * y devuelve uno nuevo, así se puede probar con pruebas unitarias normales.
 */
object GameEngine {

    const val GRID_SIZE = 18
    private const val INITIAL_LENGTH = 3
    private const val BASE_TICK_MS = 180L
    private const val MIN_TICK_MS = 70L
    private const val SPEEDUP_PER_POINT_MS = 4L

    /** Crea una partida nueva con la serpiente al centro mirando a la derecha. */
    fun newGame(
        random: Random,
        cols: Int = GRID_SIZE,
        rows: Int = GRID_SIZE,
    ): GameState {
        require(cols >= INITIAL_LENGTH + 2 && rows >= 3) { "Tablero demasiado pequeño" }
        val cx = cols / 2
        val cy = rows / 2
        val snake = List(INITIAL_LENGTH) { i -> Cell(cx - i, cy) }
        val food = randomFreeCell(cols, rows, snake, random)
            ?: error("No hay espacio para la comida")
        return GameState(
            cols = cols,
            rows = rows,
            snake = snake,
            direction = Direction.RIGHT,
            pendingDirection = Direction.RIGHT,
            food = food,
        )
    }

    /**
     * Registra un cambio de dirección para el siguiente tick.
     * Se compara contra la dirección del último movimiento real para que dos gestos
     * rápidos no permitan a la serpiente girar 180° sobre sí misma.
     */
    fun changeDirection(state: GameState, newDirection: Direction): GameState {
        if (state.isOver) return state
        if (newDirection.isOppositeOf(state.direction)) return state
        return state.copy(pendingDirection = newDirection)
    }

    /** Avanza la partida un paso. Si no está en curso, devuelve el mismo estado. */
    fun tick(state: GameState, random: Random): GameState {
        if (state.status != Status.RUNNING) return state

        val dir = state.pendingDirection
        val newHead = state.head + dir

        val outOfBounds = newHead.x !in 0 until state.cols || newHead.y !in 0 until state.rows
        if (outOfBounds) return state.copy(direction = dir, status = Status.GAME_OVER)

        val eats = newHead == state.food
        // Si no come, la cola se mueve en este mismo tick, así que esa celda queda libre.
        val body = if (eats) state.snake else state.snake.dropLast(1)
        if (newHead in body) return state.copy(direction = dir, status = Status.GAME_OVER)

        val newSnake = listOf(newHead) + body
        if (!eats) {
            return state.copy(snake = newSnake, direction = dir)
        }

        val newFood = randomFreeCell(state.cols, state.rows, newSnake, random)
            ?: return state.copy(
                snake = newSnake,
                direction = dir,
                score = state.score + 1,
                status = Status.WON,
            )

        return state.copy(
            snake = newSnake,
            direction = dir,
            food = newFood,
            score = state.score + 1,
        )
    }

    /** Milisegundos entre ticks: el juego se acelera conforme sube la puntuación. */
    fun tickMillis(score: Int): Long =
        (BASE_TICK_MS - score * SPEEDUP_PER_POINT_MS).coerceAtLeast(MIN_TICK_MS)

    /** Celda aleatoria que no ocupa la serpiente, o null si el tablero está lleno. */
    fun randomFreeCell(cols: Int, rows: Int, snake: List<Cell>, random: Random): Cell? {
        val occupied = snake.toHashSet()
        val free = ArrayList<Cell>(cols * rows - occupied.size)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val c = Cell(x, y)
                if (c !in occupied) free.add(c)
            }
        }
        return if (free.isEmpty()) null else free[random.nextInt(free.size)]
    }
}

package com.jcbernal.serpienteneon.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private val random = Random(42)

    private fun running(
        snake: List<Cell>,
        direction: Direction = Direction.RIGHT,
        food: Cell = Cell(0, 0),
        cols: Int = 10,
        rows: Int = 10,
    ) = GameState(
        cols = cols,
        rows = rows,
        snake = snake,
        direction = direction,
        pendingDirection = direction,
        food = food,
        status = Status.RUNNING,
    )

    @Test
    fun newGame_startsCenteredAndFoodIsFree() {
        val s = GameEngine.newGame(random)
        assertEquals(3, s.snake.size)
        assertEquals(Status.READY, s.status)
        assertFalse(s.food in s.snake)
    }

    @Test
    fun tick_movesForwardWithoutGrowing() {
        val s = running(listOf(Cell(5, 5), Cell(4, 5), Cell(3, 5)))
        val next = GameEngine.tick(s, random)
        assertEquals(listOf(Cell(6, 5), Cell(5, 5), Cell(4, 5)), next.snake)
        assertEquals(Status.RUNNING, next.status)
    }

    @Test
    fun tick_eatingGrowsAndScores() {
        val s = running(listOf(Cell(5, 5), Cell(4, 5)), food = Cell(6, 5))
        val next = GameEngine.tick(s, random)
        assertEquals(3, next.snake.size)
        assertEquals(1, next.score)
        assertFalse(next.food in next.snake)
    }

    @Test
    fun tick_hittingWallEndsGame() {
        val s = running(listOf(Cell(9, 5), Cell(8, 5)))
        assertEquals(Status.GAME_OVER, GameEngine.tick(s, random).status)
    }

    @Test
    fun tick_hittingOwnBodyEndsGame() {
        // Cabeza en (5,5) yendo hacia abajo, con el cuerpo justo debajo dando la vuelta.
        val s = running(
            listOf(Cell(5, 5), Cell(6, 5), Cell(6, 6), Cell(5, 6), Cell(4, 6)),
            direction = Direction.DOWN,
        )
        assertEquals(Status.GAME_OVER, GameEngine.tick(s, random).status)
    }

    @Test
    fun tick_movingIntoTailCellIsAllowed() {
        // Un cuadrado de 4: la cabeza entra donde estaba la cola, que se mueve en el mismo tick.
        val s = running(
            listOf(Cell(5, 5), Cell(6, 5), Cell(6, 6), Cell(5, 6)),
            direction = Direction.DOWN,
        )
        val next = GameEngine.tick(s, random)
        assertEquals(Status.RUNNING, next.status)
        assertEquals(Cell(5, 6), next.head)
    }

    @Test
    fun changeDirection_ignoresReversal() {
        val s = running(listOf(Cell(5, 5), Cell(4, 5)))
        assertEquals(Direction.RIGHT, GameEngine.changeDirection(s, Direction.LEFT).pendingDirection)
        assertEquals(Direction.UP, GameEngine.changeDirection(s, Direction.UP).pendingDirection)
    }

    @Test
    fun changeDirection_twoQuickTurnsCannotReverse() {
        // Va a la derecha; el jugador desliza ARRIBA y luego IZQUIERDA antes del siguiente tick.
        val s = running(listOf(Cell(5, 5), Cell(4, 5)))
        val up = GameEngine.changeDirection(s, Direction.UP)
        val left = GameEngine.changeDirection(up, Direction.LEFT)
        assertEquals(Direction.UP, left.pendingDirection)
    }

    @Test
    fun tick_doesNothingWhenPaused() {
        val s = running(listOf(Cell(5, 5))).copy(status = Status.PAUSED)
        assertEquals(s, GameEngine.tick(s, random))
    }

    @Test
    fun tick_fillingBoardWins() {
        // Tablero de 5x3 casi lleno: solo queda libre la celda de la comida.
        val snake = listOf(
            Cell(3, 0), Cell(2, 0), Cell(1, 0), Cell(0, 0),
            Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(3, 1), Cell(4, 1),
            Cell(4, 2), Cell(3, 2), Cell(2, 2), Cell(1, 2), Cell(0, 2),
        )
        val s = running(snake, food = Cell(4, 0), cols = 5, rows = 3)
        val next = GameEngine.tick(s, random)
        assertEquals(Status.WON, next.status)
        assertEquals(15, next.snake.size)
    }

    @Test
    fun randomFreeCell_nullWhenFull() {
        val all = (0 until 2).flatMap { y -> (0 until 2).map { x -> Cell(x, y) } }
        assertNull(GameEngine.randomFreeCell(2, 2, all, random))
    }

    @Test
    fun tickMillis_speedsUpButHasFloor() {
        assertTrue(GameEngine.tickMillis(10) < GameEngine.tickMillis(0))
        assertEquals(GameEngine.tickMillis(1_000), GameEngine.tickMillis(10_000))
    }
}

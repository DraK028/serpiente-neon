package com.jcbernal.serpienteneon.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jcbernal.serpienteneon.game.Cell
import com.jcbernal.serpienteneon.game.Direction
import com.jcbernal.serpienteneon.game.GameEngine
import com.jcbernal.serpienteneon.game.GameState
import com.jcbernal.serpienteneon.game.GameViewModel
import com.jcbernal.serpienteneon.game.Status
import com.jcbernal.serpienteneon.ui.theme.Neon
import com.jcbernal.serpienteneon.ui.theme.SerpienteNeonTheme
import kotlin.math.abs
import kotlin.random.Random

/** Punto de entrada de la UI: lee el estado del ViewModel y lo pasa a [GameContent]. */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val highScore by viewModel.highScore.collectAsStateWithLifecycle()
    val newRecord by viewModel.newRecord.collectAsStateWithLifecycle()

    // Vibración ligera al comer y una más fuerte al perder.
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(state.score) {
        if (state.score > 0) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    LaunchedEffect(state.status) {
        if (state.status == Status.GAME_OVER) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    GameContent(
        state = state,
        highScore = highScore,
        newRecord = newRecord,
        onDirection = viewModel::onDirection,
        onTap = viewModel::onTap,
    )
}

/** Pantalla completa sin dependencias del ViewModel (así funciona el @Preview). */
@Composable
fun GameContent(
    state: GameState,
    highScore: Int,
    newRecord: Boolean,
    onDirection: (Direction) -> Unit,
    onTap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Neon.BackgroundTop, Neon.Background))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Title()
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ScoreCard("PUNTOS", state.score, Neon.Cyan, Modifier.weight(1f))
                ScoreCard("RÉCORD", highScore, Neon.Magenta, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Board(
                    state = state,
                    newRecord = newRecord,
                    onDirection = onDirection,
                    onTap = onTap,
                    modifier = Modifier.aspectRatio(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            DPad(status = state.status, onDirection = onDirection, onTap = onTap)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "DESLIZA PARA MOVER · TOCA PARA PAUSAR",
                style = MaterialTheme.typography.labelSmall,
                color = Neon.TextDim,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Title() {
    val base = MaterialTheme.typography.displaySmall
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "SERPIENTE ",
            style = base.copy(color = Neon.Cyan, shadow = Shadow(Neon.Cyan, Offset.Zero, 28f)),
        )
        Text(
            text = "NEÓN",
            style = base.copy(color = Neon.Magenta, shadow = Shadow(Neon.Magenta, Offset.Zero, 28f)),
        )
    }
}

@Composable
private fun ScoreCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.5f), shape)
            .padding(vertical = 10.dp, horizontal = 14.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Neon.TextDim)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = Shadow(color, Offset.Zero, 16f),
            ),
            color = color,
        )
    }
}

@Composable
private fun Board(
    state: GameState,
    newRecord: Boolean,
    onDirection: (Direction) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val foodScale by pulse.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "foodScale",
    )

    // pointerInput(Unit) no se reinicia en cada recomposición, así que leemos
    // siempre la versión más reciente de los callbacks.
    val currentOnDirection by rememberUpdatedState(onDirection)
    val currentOnTap by rememberUpdatedState(onTap)
    val swipeThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }

    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Neon.Board)
            .border(2.dp, Brush.linearGradient(listOf(Neon.Cyan, Neon.Magenta)), shape)
            .pointerInput(Unit) {
                var accumulated = Offset.Zero
                detectDragGestures(
                    onDragStart = { accumulated = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulated += dragAmount
                        if (accumulated.getDistance() > swipeThresholdPx) {
                            currentOnDirection(accumulated.toDirection())
                            accumulated = Offset.Zero
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { currentOnTap() })
            }
            .padding(8.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBoard(state, foodScale)
        }
        BoardOverlay(state = state, newRecord = newRecord)
    }
}

private fun Offset.toDirection(): Direction =
    if (abs(x) > abs(y)) {
        if (x > 0) Direction.RIGHT else Direction.LEFT
    } else {
        if (y > 0) Direction.DOWN else Direction.UP
    }

private fun DrawScope.drawBoard(state: GameState, foodScale: Float) {
    val cellW = size.width / state.cols
    val cellH = size.height / state.rows
    fun centerOf(c: Cell) = Offset((c.x + 0.5f) * cellW, (c.y + 0.5f) * cellH)

    // Cuadrícula de puntos tenues
    for (y in 0 until state.rows) {
        for (x in 0 until state.cols) {
            drawCircle(Neon.GridDot, radius = cellW * 0.06f, center = centerOf(Cell(x, y)))
        }
    }

    // Comida con halo que late
    val foodCenter = centerOf(state.food)
    val haloRadius = cellW * 1.3f * foodScale
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Neon.Yellow.copy(alpha = 0.45f), Color.Transparent),
            center = foodCenter,
            radius = haloRadius,
        ),
        radius = haloRadius,
        center = foodCenter,
    )
    drawCircle(Neon.Yellow, radius = cellW * 0.32f * foodScale, center = foodCenter)

    // Serpiente: se dibuja de la cola a la cabeza para que la cabeza quede encima.
    val n = state.snake.size
    val dead = state.status == Status.GAME_OVER
    val inset = cellW * 0.06f
    for (i in n - 1 downTo 0) {
        val c = state.snake[i]
        val t = if (n <= 1) 0f else i / (n - 1f)
        val color = if (dead && i == 0) Neon.Danger else lerp(Neon.Cyan, Neon.Magenta, t)
        val left = c.x * cellW
        val top = c.y * cellH
        // Halo
        drawRoundRect(
            color = color.copy(alpha = 0.22f),
            topLeft = Offset(left - inset * 2, top - inset * 2),
            size = Size(cellW + inset * 4, cellH + inset * 4),
            cornerRadius = CornerRadius(cellW * 0.45f),
        )
        // Segmento
        drawRoundRect(
            color = color,
            topLeft = Offset(left + inset, top + inset),
            size = Size(cellW - inset * 2, cellH - inset * 2),
            cornerRadius = CornerRadius(cellW * 0.3f),
        )
    }

    // Ojos en la cabeza, orientados hacia donde avanza
    val dir = state.direction
    val headCenter = centerOf(state.head)
    val forward = Offset(dir.dx * cellW * 0.14f, dir.dy * cellH * 0.14f)
    val side = Offset(-dir.dy * cellW * 0.2f, dir.dx * cellH * 0.2f)
    val eyeRadius = cellW * 0.09f
    drawCircle(Neon.Background, eyeRadius, headCenter + forward + side)
    drawCircle(Neon.Background, eyeRadius, headCenter + forward - side)
}

@Composable
private fun BoardOverlay(state: GameState, newRecord: Boolean) {
    // Guardamos el último estado no‑RUNNING para que el texto siga visible
    // mientras el overlay se desvanece al reanudar.
    val lastShown = remember { arrayOf(state) }
    if (state.status != Status.RUNNING) lastShown[0] = state
    val shown = lastShown[0]

    AnimatedVisibility(
        visible = state.status != Status.RUNNING,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Neon.Background.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (shown.status) {
                    Status.READY -> OverlayText(
                        title = "¿LISTO?",
                        color = Neon.Cyan,
                        lines = listOf("Desliza o toca para empezar"),
                    )
                    Status.PAUSED -> OverlayText(
                        title = "PAUSA",
                        color = Neon.Yellow,
                        lines = listOf("Toca para continuar"),
                    )
                    Status.GAME_OVER -> OverlayText(
                        title = "FIN DEL JUEGO",
                        color = Neon.Danger,
                        lines = listOfNotNull(
                            "Puntos: ${shown.score}",
                            if (newRecord) "★ ¡NUEVO RÉCORD! ★" else null,
                            "Toca para reintentar",
                        ),
                    )
                    Status.WON -> OverlayText(
                        title = "¡GANASTE!",
                        color = Neon.Yellow,
                        lines = listOf("Llenaste todo el tablero", "Toca para jugar otra vez"),
                    )
                    Status.RUNNING -> Unit
                }
            }
        }
    }
}

@Composable
private fun OverlayText(title: String, color: Color, lines: List<String>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            shadow = Shadow(color, Offset.Zero, 24f),
        ),
        color = color,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(10.dp))
    lines.forEach { line ->
        Text(
            text = line,
            style = MaterialTheme.typography.bodyMedium,
            color = Neon.Text,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp),
        )
    }
}

@Composable
private fun DPad(status: Status, onDirection: (Direction) -> Unit, onTap: () -> Unit) {
    val centerLabel = when (status) {
        Status.RUNNING -> "❚❚"
        Status.GAME_OVER, Status.WON -> "↻"
        else -> "▶"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PadButton("▲") { onDirection(Direction.UP) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PadButton("◀") { onDirection(Direction.LEFT) }
            PadButton(centerLabel, color = Neon.Magenta, onClick = onTap)
            PadButton("▶") { onDirection(Direction.RIGHT) }
        }
        PadButton("▼") { onDirection(Direction.DOWN) }
    }
}

@Composable
private fun PadButton(label: String, color: Color = Neon.Cyan, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.5.dp, color.copy(alpha = 0.7f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = color, fontSize = 18.sp)
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 820)
@Composable
private fun GameContentPreview() {
    val base = GameEngine.newGame(Random(7))
    SerpienteNeonTheme {
        GameContent(
            state = base.copy(
                snake = listOf(Cell(9, 9), Cell(8, 9), Cell(7, 9), Cell(7, 10), Cell(6, 10)),
                score = 12,
                status = Status.PAUSED,
            ),
            highScore = 31,
            newRecord = false,
            onDirection = {},
            onTap = {},
        )
    }
}

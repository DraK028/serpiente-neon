# Serpiente Neón 🐍

Juego de Snake para Android con estética neón, hecho en **Kotlin + Jetpack Compose**.

## Cómo abrirlo
1. Clona el repo: `git clone https://github.com/DraK028/serpiente-neon.git`
2. En Android Studio: **File → Open…** y elige la carpeta del proyecto.
3. Espera a que termine el *Gradle Sync* (la primera vez descarga dependencias).
4. Elige un emulador o tu teléfono y presiona ▶ **Run**.

Requisitos: Android Studio reciente (Ladybug o posterior), dispositivo con Android 8.0 (API 26) o superior.

## Cómo se juega
- **Desliza** sobre el tablero (o usa el D‑pad) para cambiar de dirección.
- **Toca** el tablero o el botón central para pausar / reanudar / reiniciar.
- Cada comida suma 1 punto y la serpiente se acelera un poco.
- Pierdes si chocas con la pared o contigo mismo. Si llenas el tablero, ganas.
- El récord se guarda en el teléfono. Si sales de la app, la partida se pausa sola.

## Estructura
```
app/src/main/java/com/jcbernal/serpienteneon/
├── MainActivity.kt          Activity; pausa la partida en onPause()
├── game/
│   ├── GameModels.kt        Direction, Cell, Status, GameState (inmutable)
│   ├── GameEngine.kt        Lógica pura: newGame, changeDirection, tick, velocidad
│   ├── GameViewModel.kt     Bucle del juego con corrutinas + StateFlow
│   └── HighScoreStore.kt    Récord en SharedPreferences
└── ui/
    ├── GameScreen.kt        Pantalla, tablero en Canvas, gestos, D‑pad, overlays
    └── theme/Theme.kt       Paleta neón y tipografía
app/src/test/.../GameEngineTest.kt   12 pruebas unitarias del motor
```

**Arquitectura:** el motor (`GameEngine`) es Kotlin puro sin Android, así que se prueba con JUnit
normal (`./gradlew test`). El `ViewModel` corre el bucle y expone el estado con `StateFlow`;
la UI solo dibuja ese estado y manda eventos (dirección, toque).

## Ideas para extenderlo
- Paredes que te teletransportan al otro lado (modo "sin bordes").
- Obstáculos por nivel o comida especial que da más puntos.
- Animación suave entre celdas interpolando la posición con el tiempo del tick.
- Guardar historial de partidas con Room o DataStore.

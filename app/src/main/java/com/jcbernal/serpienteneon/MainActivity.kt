package com.jcbernal.serpienteneon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.jcbernal.serpienteneon.game.GameViewModel
import com.jcbernal.serpienteneon.ui.GameScreen
import com.jcbernal.serpienteneon.ui.theme.SerpienteNeonTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SerpienteNeonTheme {
                GameScreen(viewModel)
            }
        }
    }

    /** Si sales de la app (llamada, notificación, etc.) la partida se pausa sola. */
    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }
}

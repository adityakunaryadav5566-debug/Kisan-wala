package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.example.ui.KisanApp
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.KisanPlantAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(EmeraldDeep.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(EmeraldDeep.toArgb())
        )
        setContent {
            KisanPlantAiTheme {
                KisanApp()
            }
        }
    }
}

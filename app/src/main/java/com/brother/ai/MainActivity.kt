package com.brother.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.brother.ai.ui.BrotherDashboard
import com.brother.ai.ui.theme.BrotherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrotherTheme {
                Surface(color = Color.Transparent) {
                    BrotherDashboard()
                }
            }
        }
    }
}
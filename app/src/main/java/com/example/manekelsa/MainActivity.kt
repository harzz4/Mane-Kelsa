package com.example.manekelsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.manekelsa.presentation.ManeKelsaApp
import com.example.manekelsa.presentation.theme.ManeKelsaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ManeKelsaTheme {
                ManeKelsaApp()
            }
        }
    }
}

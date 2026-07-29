package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.MainAppStructure
import com.example.ui.theme.SilverErpTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SilverViewModel

class MainActivity : ComponentActivity() {

    private val silverViewModel: SilverViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilverErpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppStructure(
                        silverViewModel = silverViewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

package com.example.bacalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bacalog.ui.navigation.AppNavGraph
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.ui.theme.BacaLogTheme
import com.example.bacalog.viewmodel.AuthViewModel
import com.example.bacalog.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BacaLogTheme {
                val factory = ViewModelFactory(applicationContext)
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

                when (isUserLoggedIn) {
                    null -> {
                        // Layar memuat awal saat membaca sesi dari DataStore
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    true -> {
                        AppNavGraph(
                            authViewModel = authViewModel,
                            startDestination = Screen.Home.route
                        )
                    }
                    false -> {
                        AppNavGraph(
                            authViewModel = authViewModel,
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}

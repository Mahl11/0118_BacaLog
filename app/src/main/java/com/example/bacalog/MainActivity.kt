package com.example.bacalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bacalog.ui.navigation.AppNavGraph
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.ui.theme.BacaLogTheme
import com.example.bacalog.viewmodel.AuthViewModel
import com.example.bacalog.viewmodel.HomeViewModel
import com.example.bacalog.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BacaLogTheme {
                val factory = ViewModelFactory(applicationContext)
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

                when (isUserLoggedIn) {
                    null -> SplashScreen()
                    true -> {
                        AppNavGraph(
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            factory = factory,
                            startDestination = Screen.Home.route
                        )
                    }
                    false -> {
                        AppNavGraph(
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            factory = factory,
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_bacalog),
                contentDescription = stringResource(R.string.desc_logo_app),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

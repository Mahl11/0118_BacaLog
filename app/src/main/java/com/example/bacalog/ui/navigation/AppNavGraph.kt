package com.example.bacalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bacalog.ui.screens.auth.LoginScreen
import com.example.bacalog.ui.screens.auth.RegisterScreen
import com.example.bacalog.ui.screens.bacaan.BacaanListScreen
import com.example.bacalog.ui.screens.home.HomeScreen
import com.example.bacalog.ui.screens.kategori.KategoriListScreen
import com.example.bacalog.ui.screens.laporan.LaporanLokalScreen
import com.example.bacalog.ui.screens.profil.ProfilScreen
import com.example.bacalog.ui.screens.progres.ProgresListScreen
import com.example.bacalog.ui.screens.target.TargetListScreen
import com.example.bacalog.viewmodel.AuthViewModel
import com.example.bacalog.viewmodel.BacaanViewModel
import com.example.bacalog.viewmodel.HomeViewModel
import com.example.bacalog.viewmodel.KategoriViewModel

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    factory: ViewModelProvider.Factory,
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    homeViewModel.loadStatistik()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    homeViewModel.loadStatistik()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Kategori.route) {
            val kategoriViewModel: KategoriViewModel = viewModel(factory = factory)
            KategoriListScreen(
                viewModel = kategoriViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Bacaan.route) {
            val bacaanViewModel: BacaanViewModel = viewModel(factory = factory)
            BacaanListScreen(
                viewModel = bacaanViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Progres.route) {
            ProgresListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Target.route) {
            TargetListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profil.route) {
            ProfilScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Laporan.route) {
            LaporanLokalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

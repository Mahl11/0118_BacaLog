package com.example.bacalog.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Kategori : Screen("kategori")
    object Bacaan : Screen("bacaan")
    object Progres : Screen("progres")
    object Target : Screen("target")
    object Profil : Screen("profil")
    object Laporan : Screen("laporan")
}

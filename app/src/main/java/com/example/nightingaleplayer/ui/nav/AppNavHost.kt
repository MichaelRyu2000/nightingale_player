package com.example.nightingaleplayer.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = NavigationItem.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier,
    ) {
        composable(route = NavigationItem.Home.route) {
            
        }
        composable(route = NavigationItem.SelectedSong.route) {

        }
    }
}
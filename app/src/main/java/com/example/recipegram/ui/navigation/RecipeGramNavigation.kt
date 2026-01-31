package com.example.recipegram.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipegram.ui.screens.HomeScreen
import com.example.recipegram.ui.screens.LoginScreen
import com.example.recipegram.ui.screens.NewRecipeScreen
import com.example.recipegram.ui.screens.ProfileScreen
import com.example.recipegram.ui.screens.RecipeDetailsScreen
import com.example.recipegram.ui.screens.RegisterScreen
import com.example.recipegram.ui.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val NEW_RECIPE = "new"
    const val PROFILE = "profile"
    const val DETAILS = "details/{id}"

    fun getDetailsRoute(id: Long) = "details/$id"
}

@Composable
fun RecipeGramApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val tabRoutes = listOf(Routes.HOME, Routes.NEW_RECIPE, Routes.PROFILE)
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RecipeBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onRecipeClick = { id -> navController.navigate(Routes.getDetailsRoute(id)) }
                )
            }

            composable(Routes.NEW_RECIPE) {
                NewRecipeScreen(
                    authViewModel = authViewModel,
                    onPublishSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        }
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onRecipeClick = { id -> navController.navigate(Routes.getDetailsRoute(id)) },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Routes.DETAILS,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getLong("id") ?: 0L
                RecipeDetailsScreen(
                    recipeId = recipeId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun RecipeBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                navController.navigate(Routes.HOME) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.NEW_RECIPE,
            onClick = {
                navController.navigate(Routes.NEW_RECIPE) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "New") },
            label = { Text("New") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = {
                navController.navigate(Routes.PROFILE) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
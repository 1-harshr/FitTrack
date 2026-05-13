package com.harsh.fittrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harsh.fittrack.ui.feature.auth.LoginScreen
import com.harsh.fittrack.ui.feature.auth.SplashScreen

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    showAppleSignIn: Boolean = false,
) {
    NavHost(navController = navController, startDestination = Route.Splash) {

        composable<Route.Splash> {
            SplashScreen(
                onSignedIn = {
                    navController.navigate(Route.MainTabs) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
                onSignedOut = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Login> {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(Route.MainTabs) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                },
                showAppleSignIn = showAppleSignIn,
            )
        }

        composable<Route.MainTabs> {
            MainTabsScaffold(
                onSignedOut = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.MainTabs) { inclusive = true }
                    }
                },
            )
        }
    }
}

package com.legalmetrology.inspector.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.legalmetrology.inspector.ui.screens.chat.ChatScreen
import com.legalmetrology.inspector.ui.screens.dashboard.DashboardScreen
import com.legalmetrology.inspector.ui.screens.ecommerce.ECommerceScreen
import com.legalmetrology.inspector.ui.screens.history.HistoryScreen
import com.legalmetrology.inspector.ui.screens.login.LoginScreen
import com.legalmetrology.inspector.ui.screens.onboarding.OnboardingScreen
import com.legalmetrology.inspector.ui.screens.report.PackagingReportScreen
import com.legalmetrology.inspector.ui.screens.report.ReportScreen
import com.legalmetrology.inspector.ui.screens.review.ReviewScreen
import com.legalmetrology.inspector.ui.screens.scan.ScanScreen
import com.legalmetrology.inspector.ui.screens.splash.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartNewInspection = {
                    navController.navigate(Screen.Onboarding.route)
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                },
                onECommerceInspection = {
                    navController.navigate(Screen.ECommerce.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onProceedToScan = { packageType, category ->
                    navController.navigate(
                        Screen.Scan.createRoute(packageType.name, category.name)
                    )
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Scan.route) { backStackEntry ->
            val packageType = backStackEntry.arguments?.getString("packageType") ?: "RETAIL"
            val category = backStackEntry.arguments?.getString("category") ?: "GENERAL"

            ScanScreen(
                packageType = packageType,
                category = category,
                onProceedToReview = { inspectionId ->
                    navController.navigate(Screen.Review.createRoute(inspectionId))
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Review.route) { backStackEntry ->
            val inspectionId = backStackEntry.arguments?.getString("inspectionId") ?: ""
            ReviewScreen(
                inspectionId = inspectionId,
                onSubmitForAnalysis = { id ->
                    navController.navigate(Screen.Report.createRoute(id)) {
                        popUpTo(Screen.Review.route) { inclusive = true }
                    }
                },
                onRetakePhoto = { navController.navigateUp() },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Report.route) { backStackEntry ->
            val inspectionId = backStackEntry.arguments?.getString("inspectionId") ?: ""
            ReportScreen(
                inspectionId = inspectionId,
                onViewHistory = {
                    navController.navigate(Screen.History.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onNewInspection = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onViewReport = { inspectionId ->
                    navController.navigate(Screen.Report.createRoute(inspectionId))
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onOpenReport = { requestId ->
                    navController.navigate(Screen.PackagingReport.createRoute(requestId))
                },
                onOpenLabelValidator = {
                    navController.navigate(Screen.Onboarding.route)
                },
                onOpenHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.PackagingReport.route) {
            // requestId reaches PackagingReportViewModel through the
            // destination's SavedStateHandle, so it is not read here.
            PackagingReportScreen(
                onBack = { navController.navigateUp() },
                onValidateLabel = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }

        composable(Screen.ECommerce.route) {
            ECommerceScreen(
                onProceedToReport = { inspectionId ->
                    navController.navigate(Screen.Report.createRoute(inspectionId)) {
                        popUpTo(Screen.ECommerce.route) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }
    }
}

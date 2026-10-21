package com.example.campusconnect.ui.navigation

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.campusconnect.ui.screens.splash.SplashScreen
import com.example.campusconnect.ui.screens.auth.LoginScreen
import com.example.campusconnect.ui.screens.auth.RegisterScreen
import com.example.campusconnect.ui.screens.home.HomeScreen
import com.example.campusconnect.ui.screens.studymatch.StudyMatchScreen
import com.example.campusconnect.ui.screens.studymatch.StudyGroupScreen
import com.example.campusconnect.ui.screens.assignments.AssignmentScreen
import com.example.campusconnect.ui.screens.assignments.AcademicCompanionScreen
import com.example.campusconnect.ui.screens.teambuilder.TeamBuilderScreen
import com.example.campusconnect.ui.screens.campusintel.CampusIntelScreen
import com.example.campusconnect.ui.screens.marketplace.MarketplaceScreen
import com.example.campusconnect.ui.screens.marketplace.CreateListingScreen
import com.example.campusconnect.ui.screens.lostandfound.LostAndFoundScreen
import com.example.campusconnect.ui.screens.lostandfound.ReportItemScreen
import com.example.campusconnect.ui.screens.social.SocialFeedScreen
import com.example.campusconnect.ui.screens.social.CreatePostScreen
import com.example.campusconnect.ui.screens.search.StudentSearchScreen
import com.example.campusconnect.ui.screens.profile.ProfileScreen
import com.example.campusconnect.ui.screens.opportunities.OpportunitiesScreen
import com.example.campusconnect.ui.screens.webview.WebViewScreen
import com.example.campusconnect.viewmodel.AuthViewModel
import com.example.campusconnect.viewmodel.HomeViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Shared Auth ViewModel
    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(application) as T
    })

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── Splash ──
        composable(Screen.Splash.route) {
            SplashScreen(
                isLoggedIn = currentUser != null,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ──
        composable(Screen.Login.route) {
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                onLogin = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = {
                    authViewModel.clearError()
                    navController.navigate(Screen.Register.route)
                },
                onForgotPassword = { email, newPassword, onResult ->
                    authViewModel.resetPassword(email, newPassword, onResult)
                },
                errorMessage = errorMessage,
                isLoading = isLoading
            )
        }

        // ── Register ──
        composable(Screen.Register.route) {
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            RegisterScreen(
                onRegister = { user -> authViewModel.register(user) },
                onNavigateToLogin = {
                    authViewModel.clearError()
                    navController.popBackStack()
                },
                errorMessage = errorMessage,
                isLoading = isLoading
            )
        }

        // ── Home ──
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(application) as T
            })
            val upcoming by homeViewModel.upcomingAssignments.collectAsStateWithLifecycle()
            val posts by homeViewModel.recentPosts.collectAsStateWithLifecycle()

            HomeScreen(
                userName = currentUser?.name ?: "Student",
                onNavigate = { route -> navController.navigate(route) },
                upcomingAssignments = upcoming,
                recentPosts = posts
            )
        }

        // ── Study Match ──
        composable(Screen.StudyMatch.route) {
            StudyMatchScreen(
                onNavigateToGroup = { groupId ->
                    navController.navigate(Screen.StudyGroup.createRoute(groupId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Study Group ──
        composable(
            route = Screen.StudyGroup.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
            StudyGroupScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Assignments ──
        composable(Screen.Assignments.route) {
            AssignmentScreen(
                onNavigateToAcademic = { navController.navigate(Screen.AcademicCompanion.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Academic Companion ──
        composable(Screen.AcademicCompanion.route) {
            AcademicCompanionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Team Builder ──
        composable(Screen.TeamBuilder.route) {
            TeamBuilderScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Campus Intel ──
        composable(Screen.CampusIntel.route) {
            CampusIntelScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Marketplace ──
        composable(Screen.Marketplace.route) {
            MarketplaceScreen(
                onNavigateToCreateListing = { navController.navigate(Screen.CreateListing.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Create Listing ──
        composable(Screen.CreateListing.route) {
            CreateListingScreen(
                currentUser = currentUser,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Lost & Found ──
        composable(Screen.LostAndFound.route) {
            LostAndFoundScreen(
                onNavigateToReport = { type ->
                    navController.navigate(Screen.ReportItem.createRoute(type))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Report Item ──
        composable(
            route = Screen.ReportItem.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "Lost"
            ReportItemScreen(
                type = type,
                currentUser = currentUser,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Social Feed ──
        composable(Screen.SocialFeed.route) {
            SocialFeedScreen(
                onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Create Post ──
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                currentUser = currentUser,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Student Search / Find Peers ──
        composable(Screen.StudentSearch.route) {
            StudentSearchScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Profile ──
        composable(Screen.Profile.route) {
            ProfileScreen(
                currentUser = currentUser,
                onNavigateToWebView = { url ->
                    navController.navigate(Screen.WebViewScreen.createRoute(url))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Opportunities ──
        composable(Screen.Opportunities.route) {
            OpportunitiesScreen(
                onNavigateToWebView = { url ->
                    navController.navigate(Screen.WebViewScreen.createRoute(url))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── WebView ──
        composable(
            route = Screen.WebViewScreen.route,
            arguments = listOf(navArgument("url") {
                type = NavType.StringType
                defaultValue = "https://www.google.com"
            })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: "https://www.google.com"
            WebViewScreen(
                url = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example.campusconnect.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object StudyMatch : Screen("study_match")
    object StudyGroup : Screen("study_group/{groupId}") {
        fun createRoute(groupId: Long) = "study_group/$groupId"
    }
    object Assignments : Screen("assignments")
    object AcademicCompanion : Screen("academic_companion")
    object TeamBuilder : Screen("team_builder")
    object CampusIntel : Screen("campus_intel")
    object Marketplace : Screen("marketplace")
    object CreateListing : Screen("create_listing")
    object LostAndFound : Screen("lost_and_found")
    object ReportItem : Screen("report_item/{type}") {
        fun createRoute(type: String) = "report_item/$type"
    }
    object SocialFeed : Screen("social_feed")
    object CreatePost : Screen("create_post")
    object StudentSearch : Screen("student_search")
    object Profile : Screen("profile")
    object Opportunities : Screen("opportunities")
    object WebViewScreen : Screen("webview?url={url}") {
        fun createRoute(url: String) = "webview?url=${Uri.encode(url)}"
    }
}

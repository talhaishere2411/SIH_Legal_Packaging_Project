package com.legalmetrology.inspector.ui.navigation

/**
 * All navigation routes in the app.
 * Using sealed class for type safety and compile-time route verification.
 */
sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Login        : Screen("login")
    object Onboarding   : Screen("onboarding")
    object Scan         : Screen("scan/{packageType}/{category}") {
        fun createRoute(packageType: String, category: String) =
            "scan/$packageType/$category"
    }
    object Review       : Screen("review/{inspectionId}") {
        fun createRoute(inspectionId: String) = "review/$inspectionId"
    }
    object Report       : Screen("report/{inspectionId}") {
        fun createRoute(inspectionId: String) = "report/$inspectionId"
    }
    object History      : Screen("history")
    object Dashboard    : Screen("dashboard")
    object ECommerce    : Screen("ecommerce")

    // --- Packaging Advisor (primary flow) ---
    /** Conversational recommendation surface — the app's home. */
    object Chat            : Screen("chat")
    /** Full recommendation report produced by a chat turn. */
    object PackagingReport : Screen("packaging_report/{requestId}") {
        fun createRoute(requestId: String) = "packaging_report/$requestId"
    }
}

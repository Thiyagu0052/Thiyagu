package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Login")
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Shops : Screen("shops", "Shops")
    data object ShopDetail : Screen("shop_detail/{shopId}", "Shop Detail") {
        fun createRoute(shopId: Long) = "shop_detail/$shopId"
    }
    data object Products : Screen("products", "Products")
    data object Transactions : Screen("transactions", "Transactions")
    data object Reports : Screen("reports", "Reports")
    data object Settings : Screen("settings", "Settings")
}

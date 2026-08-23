package com.example.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.login.LoginScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.shops.ShopDetailScreen
import com.example.ui.screens.shops.ShopsScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SilverViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "முகப்பு", Icons.Default.Dashboard),
    BottomNavItem(Screen.Shops.route, "கடைகள்", Icons.Default.Store),
    BottomNavItem(Screen.Products.route, "பொருட்கள்", Icons.Default.Category),
    BottomNavItem(Screen.Transactions.route, "லெட்ஜர்", Icons.AutoMirrored.Filled.ReceiptLong),
    BottomNavItem(Screen.Reports.route, "அறிக்கை", Icons.Default.Assessment),
    BottomNavItem(Screen.Settings.route, "அமைப்பு", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(
    silverViewModel: SilverViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val shops by silverViewModel.shops.collectAsState()
    val products by silverViewModel.products.collectAsState()
    val transactions by silverViewModel.transactions.collectAsState()
    val holdSummary by silverViewModel.holdSummary.collectAsState()
    val syncStatus by silverViewModel.syncStatus.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                silverViewModel.exportData(outputStream) { success ->
                    val message = if (success) "ஏற்றுமதி வெற்றிகரமாக முடிந்தது!" else "ஏற்றுமதி தோல்வியடைந்தது."
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { inputStream ->
                silverViewModel.importData(inputStream) { success ->
                    val message = if (success) "மீட்டமைப்பு வெற்றிகரமாக முடிந்தது!" else "மீட்டமைப்பு தோல்வியடைந்தது."
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val showBottomBar = isLoggedIn && currentRoute != Screen.Login.route

    Scaffold(
        topBar = {
            if (showBottomBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "KKY SILVERS",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier.height(56.dp),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.height(80.dp),
                    windowInsets = WindowInsets.navigationBars
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            alwaysShowLabel = true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title, modifier = Modifier.size(22.dp)) },
                            label = { 
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { email, pass ->
                        if (authViewModel.login(email, pass)) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    summary = holdSummary,
                    shops = shops,
                    products = products,
                    transactions = transactions,
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToShops = { navController.navigate(Screen.Shops.route) },
                    onNavigateToShopDetail = { shopId ->
                        navController.navigate(Screen.ShopDetail.createRoute(shopId))
                    },
                    onAddTransaction = { date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                        silverViewModel.addTransaction(date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
                    },
                    onUpdateTransaction = { id, date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                        silverViewModel.updateTransaction(id, date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
                    },
                    onDeleteTransaction = { silverViewModel.deleteTransaction(it) }
                )
            }

            composable(Screen.Shops.route) {
                ShopsScreen(
                    shops = shops,
                    transactions = transactions,
                    onAddShop = { name, owner, phone, address, gst, notes ->
                        silverViewModel.addShop(name, owner, phone, address, gst, notes)
                    },
                    onUpdateShop = { silverViewModel.updateShop(it) },
                    onDeleteShop = { silverViewModel.deleteShop(it) },
                    onShopClick = { shopId ->
                        navController.navigate(Screen.ShopDetail.createRoute(shopId))
                    }
                )
            }

            composable(
                route = Screen.ShopDetail.route,
                arguments = listOf(navArgument("shopId") { type = NavType.LongType })
            ) { backStackEntry ->
                val shopId = backStackEntry.arguments?.getLong("shopId") ?: 0L
                ShopDetailScreen(
                    shopId = shopId,
                    shops = shops,
                    products = products,
                    transactions = transactions,
                    onUpdateShop = { silverViewModel.updateShop(it) },
                    onAddTransaction = { date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri ->
                        silverViewModel.addTransaction(date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri)
                    },
                    onUpdateTransaction = { id, date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri ->
                        silverViewModel.updateTransaction(id, date, time, sId, sName, type, weight, touch, touchAdj, remarks, imageUri)
                    },
                    onDeleteTransaction = { silverViewModel.deleteTransaction(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Products.route) {
                ProductsScreen(
                    products = products,
                    onAddProduct = { name, cat, defaultW, imageUri ->
                        silverViewModel.addProduct(name, cat, defaultW, imageUri)
                    },
                    onUpdateProduct = { silverViewModel.updateProduct(it) },
                    onDeleteProduct = { silverViewModel.deleteProduct(it) }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    shops = shops,
                    products = products,
                    transactions = transactions,
                    onAddTransaction = { date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                        silverViewModel.addTransaction(date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
                    },
                    onUpdateTransaction = { id, date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri ->
                        silverViewModel.updateTransaction(id, date, time, shopId, shopName, type, weight, touch, touchAdj, remarks, imageUri)
                    },
                    onDeleteTransaction = { silverViewModel.deleteTransaction(it) }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    shops = shops,
                    transactions = transactions
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentUser = currentUser,
                    syncStatus = syncStatus,
                    onSyncNow = { silverViewModel.syncToFirebase() },
                    onExportData = { exportLauncher.launch("silver_erp_backup.json") },
                    onImportData = { importLauncher.launch(arrayOf("application/json")) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

package com.example.easycooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.easycooking.navigation.BottomTab
import com.example.easycooking.navigation.Screen
import com.example.easycooking.ui.components.AppBottomNavigationBar
import com.example.easycooking.ui.screens.AccountScreen
import com.example.easycooking.ui.screens.CookingModeScreen
import com.example.easycooking.ui.screens.DietaryAllergiesScreen
import com.example.easycooking.ui.screens.DiscoverScreen
import com.example.easycooking.ui.screens.FilterModalScreen
import com.example.easycooking.ui.screens.FinishedCookingScreen
import com.example.easycooking.ui.screens.ForgotPasswordScreen
import com.example.easycooking.ui.screens.GroceryDetailScreen
import com.example.easycooking.ui.screens.GroceryOverviewScreen
import com.example.easycooking.ui.screens.LoginScreen
import com.example.easycooking.ui.screens.RecipeDetailScreen
import com.example.easycooking.ui.screens.RegisterScreen
import com.example.easycooking.ui.screens.SavedScreen
import com.example.easycooking.ui.screens.SearchResultScreen
import com.example.easycooking.ui.screens.UploadStep1Screen
import com.example.easycooking.ui.screens.UploadStep2Screen
import com.example.easycooking.ui.screens.WelcomeScreen
import com.example.easycooking.ui.screens.WriteReviewScreen
import com.example.easycooking.ui.theme.EasyCookingTheme
import com.example.easycooking.viewmodel.CookingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemNavigationBar()
        setContent {
            EasyCookingTheme {
                EasyCookingApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigationBar()
        }
    }

    private fun hideSystemNavigationBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

@Composable
fun EasyCookingApp(
    viewModel: CookingViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTab by viewModel.currentTab.collectAsState()

    val shouldShowBottomBar = currentRoute == Screen.MainContainer.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                AppBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        viewModel.setBottomTab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate(Screen.Login.route) },
                    onContinueGuestClick = {
                        navController.navigate(Screen.MainContainer.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onSignInSuccess = {
                        navController.navigate(Screen.MainContainer.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                    onCreateAccountClick = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.MainContainer.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onSignInClick = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() },
                    onSendCodeClick = { navController.navigate(Screen.Login.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.MainContainer.route) {
                when (currentTab) {
                    BottomTab.DISCOVER -> DiscoverScreen(
                        viewModel = viewModel,
                        onRecipeClick = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                        onFilterClick = { navController.navigate(Screen.FilterModal.route) },
                        onSearchClick = { navController.navigate(Screen.SearchResults.route) },
                        onProfileClick = { viewModel.setBottomTab(BottomTab.ACCOUNT) },
                        onSignInClick = { navController.navigate(Screen.Login.route) }
                    )
                    BottomTab.SAVED -> SavedScreen(
                        viewModel = viewModel,
                        onRecipeClick = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                        onAddRecipeClick = {
                            if (viewModel.isLoggedIn.value) {
                                navController.navigate(Screen.UploadStep1.route)
                            } else {
                                navController.navigate(Screen.Login.route)
                            }
                        },
                        onSearchClick = { navController.navigate(Screen.SearchResults.route) },
                        onOpenPlanClick = {
                            val first = viewModel.groceryLists.value.firstOrNull()
                            if (first != null) {
                                navController.navigate(Screen.GroceryDetail.createRoute(first.id))
                            } else {
                                viewModel.setBottomTab(BottomTab.GROCERY)
                            }
                        },
                        onBrowseClick = { viewModel.setBottomTab(BottomTab.DISCOVER) },
                        onSignInClick = { navController.navigate(Screen.Login.route) }
                    )
                    BottomTab.GROCERY -> GroceryOverviewScreen(
                        viewModel = viewModel,
                        onListClick = { listId -> navController.navigate(Screen.GroceryDetail.createRoute(listId)) },
                        onDiscoverClick = { viewModel.setBottomTab(BottomTab.DISCOVER) },
                        onSignInClick = { navController.navigate(Screen.Login.route) }
                    )
                    BottomTab.ACCOUNT -> AccountScreen(
                        viewModel = viewModel,
                        onSignInClick = { navController.navigate(Screen.Login.route) },
                        onDietaryPreferencesClick = { navController.navigate(Screen.DietaryAllergies.route) },
                        onNewRecipeClick = { navController.navigate(Screen.UploadStep1.route) },
                        onRecipeClick = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) }
                    )
                }
            }

            composable(Screen.SearchResults.route) {
                SearchResultScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onFilterClick = { navController.navigate(Screen.FilterModal.route) },
                    onRecipeClick = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                    onSignInClick = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.FilterModal.route) {
                FilterModalScreen(
                    viewModel = viewModel,
                    onCloseClick = { navController.popBackStack() },
                    onApplyClick = {
                        navController.popBackStack()
                        navController.navigate(Screen.SearchResults.route)
                    }
                )
            }

            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "1"
                RecipeDetailScreen(
                    recipeId = recipeId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onStartCookingClick = { navController.navigate(Screen.CookingMode.createRoute(recipeId)) },
                    onWriteReviewClick = { navController.navigate(Screen.WriteReview.createRoute(recipeId)) },
                    onProfileClick = {
                        viewModel.setBottomTab(BottomTab.ACCOUNT)
                        navController.popBackStack(Screen.MainContainer.route, false)
                    },
                    onSignInClick = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(
                route = Screen.CookingMode.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "1"
                CookingModeScreen(
                    recipeId = recipeId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onFinishCookingClick = { navController.navigate(Screen.FinishedCooking.createRoute(recipeId)) }
                )
            }

            composable(
                route = Screen.FinishedCooking.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "1"
                FinishedCookingScreen(
                    recipeId = recipeId,
                    viewModel = viewModel,
                    onRateRecipeClick = { navController.navigate(Screen.WriteReview.createRoute(recipeId)) },
                    onDoneClick = {
                        navController.popBackStack(Screen.MainContainer.route, false)
                    }
                )
            }

            composable(
                route = Screen.WriteReview.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "1"
                if (!viewModel.isLoggedIn.value) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.WriteReview.route) { inclusive = true }
                    }
                } else {
                    WriteReviewScreen(
                        recipeId = recipeId,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onSubmitSuccess = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.GroceryDetail.route,
                arguments = listOf(navArgument("listId") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId") ?: "g1"
                GroceryDetailScreen(
                    listId = listId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onProfileClick = {
                        viewModel.setBottomTab(BottomTab.ACCOUNT)
                        navController.popBackStack(Screen.MainContainer.route, false)
                    }
                )
            }

            composable(Screen.UploadStep1.route) {
                if (!viewModel.isLoggedIn.value) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.UploadStep1.route) { inclusive = true }
                    }
                } else {
                    UploadStep1Screen(
                        viewModel = viewModel,
                        onCancelClick = { navController.popBackStack() },
                        onNextClick = { navController.navigate(Screen.UploadStep2.route) }
                    )
                }
            }

            composable(Screen.UploadStep2.route) {
                UploadStep2Screen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onPublishSuccess = {
                        viewModel.setBottomTab(BottomTab.SAVED)
                        navController.popBackStack(Screen.MainContainer.route, false)
                    }
                )
            }

            composable(Screen.DietaryAllergies.route) {
                DietaryAllergiesScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}

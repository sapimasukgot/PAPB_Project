package com.example.easycooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
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

private const val NAV_TRANSITION_DURATION_MS = 260
private const val TAB_TRANSITION_DURATION_MS = 220

@Composable
fun EasyCookingApp(
    viewModel: CookingViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTab by viewModel.currentTab.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()
    val startupAuthChecked by viewModel.startupAuthChecked.collectAsState()
    var didAutoRoute by remember { mutableStateOf(false) }

    LaunchedEffect(startupAuthChecked, isLoggedIn, rememberMe) {
        if (startupAuthChecked && !didAutoRoute) {
            didAutoRoute = true
            if (isLoggedIn && rememberMe &&
                navController.currentDestination?.route == Screen.Welcome.route
            ) {
                navController.navigate(Screen.MainContainer.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
        }
    }

    val slideSpec = tween<IntOffset>(
        durationMillis = NAV_TRANSITION_DURATION_MS,
        easing = FastOutSlowInEasing
    )

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .imePadding(),
            enterTransition = {
                slideInHorizontally(animationSpec = slideSpec) { fullWidth -> fullWidth }
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = slideSpec) { fullWidth -> -(fullWidth * 0.25f).toInt() }
            },
            popEnterTransition = {
                slideInHorizontally(animationSpec = slideSpec) { fullWidth -> -(fullWidth * 0.25f).toInt() }
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = slideSpec) { fullWidth -> fullWidth }
            }
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate(Screen.Login.route) },
                    onContinueGuestClick = {
                        coroutineScope.launch {
                            viewModel.continueAsGuest()
                            navController.navigate(Screen.MainContainer.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
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
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.MainContainer.route) {
                BackHandler(enabled = currentTab != BottomTab.DISCOVER) {
                    viewModel.setBottomTab(BottomTab.DISCOVER)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                val isForward = targetState.ordinal >= initialState.ordinal
                                val tabSlideSpec = tween<IntOffset>(
                                    durationMillis = TAB_TRANSITION_DURATION_MS,
                                    easing = FastOutSlowInEasing
                                )
                                if (isForward) {
                                    slideInHorizontally(animationSpec = tabSlideSpec) { it } togetherWith
                                        slideOutHorizontally(animationSpec = tabSlideSpec) { -(it * 0.25f).toInt() }
                                } else {
                                    slideInHorizontally(animationSpec = tabSlideSpec) { -(it * 0.25f).toInt() } togetherWith
                                        slideOutHorizontally(animationSpec = tabSlideSpec) { it }
                                }
                            },
                            label = "main_tab_transition"
                        ) { targetTab ->
                            when (targetTab) {
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
                                        if (isLoggedIn) {
                                            navController.navigate(Screen.UploadStep1.route)
                                        } else {
                                            navController.navigate(Screen.Login.route)
                                        }
                                    },
                                    onSearchClick = { navController.navigate(Screen.SearchResults.route) },
                                    onOpenPlanClick = {
                                        viewModel.setBottomTab(BottomTab.GROCERY)
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
                    }

                    AppBottomNavigationBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            viewModel.setBottomTab(tab)
                        }
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
                        val cameFromSearch = navController.previousBackStackEntry?.destination?.route == Screen.SearchResults.route
                        navController.popBackStack()
                        if (!cameFromSearch) {
                            navController.navigate(Screen.SearchResults.route)
                        }
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
                    onStartCookingClick = { firstStepDuration ->
                        viewModel.initCookingMode(recipeId, firstStepDuration, forceReset = true)
                        navController.navigate(Screen.CookingMode.createRoute(recipeId))
                    },
                    onWriteReviewClick = { navController.navigate(Screen.WriteReview.createRoute(recipeId)) },
                    onProfileClick = {
                        viewModel.setBottomTab(BottomTab.ACCOUNT)
                        navController.popBackStack(Screen.MainContainer.route, false)
                    },
                    onSignInClick = { navController.navigate(Screen.Login.route) },
                    onOpenGroceryList = { listId ->
                        navController.navigate(Screen.GroceryDetail.createRoute(listId))
                    }
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
                    onFinishCookingClick = {
                        navController.navigate(Screen.FinishedCooking.createRoute(recipeId)) {
                            popUpTo(Screen.CookingMode.route) { inclusive = true }
                        }
                    }
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
                    onRateRecipeClick = { selectedRating ->
                        navController.navigate(Screen.WriteReview.createRoute(recipeId, selectedRating))
                    },
                    onDoneClick = {
                        navController.popBackStack(Screen.MainContainer.route, false)
                    }
                )
            }

            composable(
                route = Screen.WriteReview.route,
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.StringType },
                    navArgument("initialRating") {
                        type = NavType.IntType
                        defaultValue = 5
                    }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "1"
                val initialRating = backStackEntry.arguments?.getInt("initialRating") ?: 5
                LaunchedEffect(isLoggedIn) {
                    if (!isLoggedIn) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.WriteReview.route) { inclusive = true }
                        }
                    }
                }
                if (isLoggedIn) {
                    WriteReviewScreen(
                        recipeId = recipeId,
                        initialRating = initialRating,
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
                LaunchedEffect(isLoggedIn) {
                    if (!isLoggedIn) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.UploadStep1.route) { inclusive = true }
                        }
                    }
                }
                if (isLoggedIn) {
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

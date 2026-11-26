package com.example.easycooking.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    object MainContainer : Screen("main_container")

    object SearchResults : Screen("search_results")
    object FilterModal : Screen("filter_modal")

    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }

    object CookingMode : Screen("cooking_mode/{recipeId}") {
        fun createRoute(recipeId: String) = "cooking_mode/$recipeId"
    }

    object FinishedCooking : Screen("finished_cooking/{recipeId}") {
        fun createRoute(recipeId: String) = "finished_cooking/$recipeId"
    }

    object WriteReview : Screen("write_review/{recipeId}?initialRating={initialRating}") {
        fun createRoute(recipeId: String, initialRating: Int = 5) =
            "write_review/$recipeId?initialRating=$initialRating"
    }

    object GroceryDetail : Screen("grocery_detail/{listId}") {
        fun createRoute(listId: String) = "grocery_detail/$listId"
    }

    object UploadStep1 : Screen("upload_step1")
    object UploadStep2 : Screen("upload_step2")

    object DietaryAllergies : Screen("dietary_allergies")
}

enum class BottomTab(val title: String) {
    DISCOVER("Discover"),
    SAVED("Saved"),
    GROCERY("Grocery"),
    ACCOUNT("Account")
}

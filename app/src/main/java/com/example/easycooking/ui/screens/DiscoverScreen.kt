package com.example.easycooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.components.RecipeCardGrid
import com.example.easycooking.ui.components.RecipeImageBox
import com.example.easycooking.ui.theme.AmberBadgeBg
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.BorderGray
import com.example.easycooking.ui.theme.DarkAvatarBg
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PlaceholderImageBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow
import com.example.easycooking.viewmodel.CookingViewModel

@Composable
fun DiscoverScreen(
    viewModel: CookingViewModel,
    onRecipeClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSignInClick: () -> Unit = {}
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val allRecipes by viewModel.recipes.collectAsState()
    val recipes by viewModel.curatedRecipes.collectAsState()
    val selectedDietary by viewModel.selectedDietaryLifestyles.collectAsState()
    val selectedAllergens by viewModel.selectedAllergens.collectAsState()
    val strictAllergenFilter by viewModel.strictAllergenFilter.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All Recipes") }
    val categories = listOf("All Recipes", "< 15 Mins", "Gluten-Free", "High Protein")

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Member Feature", fontWeight = FontWeight.Bold, color = DarkText)
            },
            text = {
                Text(
                    "Please sign in or register to bookmark recipes.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthDialog = false
                        onSignInClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText)
                ) {
                    Text("Sign In / Register", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Later", color = DarkText)
                }
            }
        )
    }

    val displayedRecipes = when (selectedCategory) {
        "< 15 Mins" -> recipes.filter { it.durationMinutes <= 15 }
        "Gluten-Free" -> recipes.filter {
            it.dietaryBadges.any { b -> b.equals("Gluten-Free", true) } ||
                it.allergens.none { a -> a.equals("Gluten", true) }
        }
        "High Protein" -> recipes.filter { it.dietaryBadges.contains("High Protein") || it.calories >= 350 }
        else -> recipes
    }

    val quickDinners = if (selectedCategory == "All Recipes") {
        displayedRecipes.filter { it.category.equals("Dinner", true) && it.durationMinutes <= 30 }
            .ifEmpty { displayedRecipes.drop(3) }
    } else {
        displayedRecipes.drop(3)
    }

    val shownIds = remember(displayedRecipes, quickDinners) {
        (displayedRecipes.take(3).map { it.id } + quickDinners.map { it.id }).toSet()
    }
    val remainingRecipes = if (selectedCategory == "All Recipes") {
        displayedRecipes.filterNot { it.id in shownIds }
    } else {
        emptyList()
    }

    val totalPublishedCount = allRecipes.count { !it.isDraft }
    val hasActivePersonalization = isLoggedIn &&
        (selectedDietary.isNotEmpty() || (strictAllergenFilter && selectedAllergens.isNotEmpty())) &&
        recipes.size < totalPublishedCount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discover",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LightGrayBg)
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search ${recipes.size} recipes...",
                    color = DarkTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = DarkText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it }) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryYellow else LightGrayBg)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = DarkText
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (hasActivePersonalization) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberBadgeBg)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dietary/allergy preferences active (${recipes.size} of $totalPublishedCount shown)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberBadgeText,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Show All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier
                            .clickable { viewModel.clearDietaryAndAllergenPreferences() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Trending Recipes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LightGrayBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${minOf(displayedRecipes.size, 3)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
                }
                Text(
                    text = "See all >",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onSearchClick)
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayedRecipes.take(3), key = { it.id }) { recipe ->
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGrayBg)
                            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                            .clickable { onRecipeClick(recipe.id) }
                    ) {
                        Column {
                            RecipeImageBox(
                                imageUri = recipe.imageUri,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = StarYellow,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = recipe.rating.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${recipe.durationMinutes}m",
                                            fontSize = 10.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                        .clickable {
                                            if (isLoggedIn) viewModel.toggleBookmark(recipe.id) else showAuthDialog = true
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (recipe.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (recipe.isBookmarked) PrimaryYellow else DarkTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = recipe.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${recipe.difficulty} | ${recipe.calories} kcal",
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "All Recipes") "Quick Dinners" else "More Recipes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "See all >",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onSearchClick)
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(quickDinners, key = { it.id }) { recipe ->
            DiscoverRecipeRow(
                recipe = recipe,
                badgeLabel = if (recipe.durationMinutes <= 20) "FAST" else recipe.category.uppercase(),
                onClick = { onRecipeClick(recipe.id) },
                onBookmarkClick = { if (isLoggedIn) viewModel.toggleBookmark(recipe.id) else showAuthDialog = true }
            )
        }

        if (remainingRecipes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore More Curated (${remainingRecipes.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "See all >",
                        fontSize = 13.sp,
                        color = DarkTextSecondary,
                        modifier = Modifier
                            .clickable(onClick = onSearchClick)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(remainingRecipes, key = { "more_${it.id}" }) { recipe ->
                DiscoverRecipeRow(
                    recipe = recipe,
                    badgeLabel = recipe.category.uppercase(),
                    onClick = { onRecipeClick(recipe.id) },
                    onBookmarkClick = { if (isLoggedIn) viewModel.toggleBookmark(recipe.id) else showAuthDialog = true }
                )
            }
        }

        if (displayedRecipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching recipes found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your dietary or allergen preferences in Account.",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DiscoverRecipeRow(
    recipe: com.example.easycooking.model.Recipe,
    badgeLabel: String,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LightGrayBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecipeImageBox(
            imageUri = recipe.imageUri,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberBadgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberBadgeText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${recipe.durationMinutes}m",
                    fontSize = 11.sp,
                    color = DarkTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recipe.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = recipe.rating.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberBadgeText
                )
            }
        }

        IconButton(onClick = onBookmarkClick) {
            Icon(
                imageVector = if (recipe.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = if (recipe.isBookmarked) PrimaryYellow else DarkTextSecondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterModalScreen(
    viewModel: CookingViewModel,
    onCloseClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    val filterState by viewModel.filterState.collectAsState()

    val durations = listOf("< 15m", "15-30m", "30-60m", "1h+")
    val dietaries = listOf("Gluten-Free", "Dairy-Free", "Nut-Free", "Shellfish-Free", "Vegetarian")
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Dessert")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = DarkText)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Filter Recipes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }

                Text(
                    text = "Reset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary,
                    modifier = Modifier
                        .clickable { viewModel.resetFilters() }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGrayBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Real-time matching dietary profile & allergens",
                    fontSize = 12.sp,
                    color = DarkTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cook Duration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                Text(if (filterState.selectedDuration.isNotEmpty()) filterState.selectedDuration else "Any duration", fontSize = 12.sp, color = DarkTextSecondary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.forEach { duration ->
                    val isSelected = filterState.selectedDuration == duration
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .sizeIn(minHeight = 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryYellow else LightGrayBg)
                            .clickable { viewModel.updateFilterDuration(duration) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = duration,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = DarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dietary & Allergies", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                Text("${filterState.selectedDietary.size} Selected", fontSize = 12.sp, color = DarkTextSecondary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dietaries.forEach { diet ->
                    val isChecked = filterState.selectedDietary.contains(diet)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isChecked) DarkText else LightGrayBg)
                            .clickable { viewModel.toggleFilterDietary(diet) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isChecked) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isChecked) diet else "+ $diet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isChecked) Color.White else DarkText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Meal Type", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                Text(if (filterState.selectedMealType.isNotEmpty()) filterState.selectedMealType else "All", fontSize = 12.sp, color = DarkTextSecondary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealTypes.forEach { meal ->
                    val isSelected = filterState.selectedMealType == meal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DarkText else LightGrayBg)
                            .clickable { viewModel.updateFilterMealType(meal) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = meal,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else DarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        val filteredRecipes by viewModel.filteredRecipes.collectAsState()
        PrimaryYellowButton(
            text = "Apply Filters (Show ${filteredRecipes.size} Recipes)",
            onClick = onApplyClick
        )
    }
}

@Composable
fun SearchResultScreen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onSignInClick: () -> Unit = {}
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    val filteredRecipes by viewModel.filteredRecipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.selectedSortOption.collectAsState()
    var searchInput by remember(searchQuery) { mutableStateOf(searchQuery) }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Member Feature", fontWeight = FontWeight.Bold, color = DarkText)
            },
            text = {
                Text(
                    "Please sign in or register to bookmark recipes.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthDialog = false
                        onSignInClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText)
                ) {
                    Text("Sign In / Register", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Later", color = DarkText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Search Results",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        OutlinedTextField(
            value = searchInput,
            onValueChange = {
                searchInput = it
                viewModel.setSearchQuery(it)
            },
            singleLine = true,
            placeholder = { Text("Search by name, chef, or ingredient...", color = DarkTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = DarkText)
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchInput.isNotEmpty()) {
                        IconButton(onClick = {
                            searchInput = ""
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = DarkTextSecondary)
                        }
                    }
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filter", tint = PrimaryYellow)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                cursorColor = DarkText,
                focusedContainerColor = LightGrayBg,
                unfocusedContainerColor = LightGrayBg,
                focusedBorderColor = PrimaryYellow,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredRecipes.size} recipes found",
                fontSize = 13.sp,
                color = DarkTextSecondary
            )
            Text(
                text = "Sort: $sortOption",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AmberBadgeText,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberBadgeBg)
                    .clickable { viewModel.cycleSortOption() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matching recipes found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting your multi-criteria filters or search terms.",
                        fontSize = 13.sp,
                        color = DarkTextSecondary
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    RecipeCardGrid(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onBookmarkToggle = { if (isLoggedIn) viewModel.toggleBookmark(recipe.id) else showAuthDialog = true }
                    )
                }
            }
        }
    }
}

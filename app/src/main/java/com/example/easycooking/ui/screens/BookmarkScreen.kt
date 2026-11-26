package com.example.easycooking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.ui.components.RecipeCardGrid
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.viewmodel.CookingViewModel

@Composable
fun SavedScreen(
    viewModel: CookingViewModel,
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onOpenPlanClick: () -> Unit = {},
    onBrowseClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val curatedRecipes by viewModel.curatedRecipes.collectAsState()
    val groceryLists by viewModel.groceryLists.collectAsState()
    val savedRecipes = recipes.filter { !it.isDraft && it.isBookmarked }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Breakfast", "Quick Dinners", "Desserts")

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
                    text = "Saved",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = DarkText)
                    }
                    Button(
                        onClick = {
                            if (isLoggedIn) onAddRecipeClick() else onSignInClick()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryYellow,
                            contentColor = DarkText
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recipe", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        if (!isLoggedIn) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGrayBg)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Save Your Favorite Recipes",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sign in or create an account to bookmark recipes and view your saved collection.",
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSignInClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryYellow,
                                contentColor = DarkText
                            )
                        ) {
                            Text("Sign In / Register", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { category ->
                        val isSelected = category == selectedCategory
                        val label = if (category == "All") "All (${savedRecipes.size})" else category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PrimaryYellow else LightGrayBg)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = DarkText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Handpicked favourites",
                        fontSize = 13.sp,
                        color = DarkTextSecondary
                    )
                    Text(
                        text = "${savedRecipes.size} saved",
                        fontSize = 12.sp,
                        color = DarkTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                val filteredRecipes = when (selectedCategory) {
                    "All" -> savedRecipes
                    "Quick Dinners" -> savedRecipes.filter {
                        it.category.equals("Dinner", ignoreCase = true) && it.durationMinutes <= 30
                    }
                    "Desserts" -> savedRecipes.filter { it.category.equals("Dessert", ignoreCase = true) }
                    else -> savedRecipes.filter { it.category.equals(selectedCategory, ignoreCase = true) }
                }

                if (filteredRecipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGrayBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkBorder,
                                contentDescription = null,
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No saved recipes in $selectedCategory",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bookmark recipes from Discover to find them quickly here.",
                                fontSize = 12.sp,
                                color = DarkTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onBrowseClick,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText)
                            ) {
                                Text("Browse Recipes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    val chunkedList = filteredRecipes.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        chunkedList.forEach { rowRecipes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowRecipes.forEach { recipe ->
                                    RecipeCardGrid(
                                        recipe = recipe,
                                        onClick = { onRecipeClick(recipe.id) },
                                        onBookmarkToggle = { viewModel.toggleBookmark(recipe.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowRecipes.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                val activeListsCount = groceryLists.count { !it.isCompleted }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGrayBg)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = DarkText, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ready for dinner?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Text(
                                text = "$activeListsCount active grocery list(s)",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenPlanClick,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Open List", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            val publishedCount = curatedRecipes.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightGrayBg)
                    .clickable(onClick = onBrowseClick)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Explore, contentDescription = null, tint = DarkText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Looking for inspiration?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Explore $publishedCount curated recipes",
                            fontSize = 11.sp,
                            color = DarkTextSecondary
                        )
                    }
                }

                Text(
                    text = "Browse >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

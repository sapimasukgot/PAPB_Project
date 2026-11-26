package com.example.easycooking.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.easycooking.model.AlarmSoundPreset
import com.example.easycooking.model.Recipe
import com.example.easycooking.ui.components.AlarmSoundPickerDialog
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.components.RecipeImageBox
import com.example.easycooking.ui.theme.AmberBadgeBg
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow
import com.example.easycooking.viewmodel.CookingViewModel
import kotlin.math.roundToInt

@Composable
fun AccountScreen(
    viewModel: CookingViewModel,
    onSignInClick: () -> Unit,
    onDietaryPreferencesClick: () -> Unit,
    onNewRecipeClick: () -> Unit,
    onRecipeClick: (String) -> Unit = {}
) {
    var isCreatorMode by remember { mutableStateOf(false) }
    var offlineBannerVisible by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val isMetricUnit by viewModel.isMetricUnit.collectAsState()
    val alarmSoundId by viewModel.alarmSoundId.collectAsState()
    val customAlarmName by viewModel.customAlarmName.collectAsState()
    val isPreviewingAlarm by viewModel.isPreviewingAlarm.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loggedInName by viewModel.loggedInName.collectAsState()
    val loggedInEmail by viewModel.loggedInEmail.collectAsState()
    val allRecipes by viewModel.recipes.collectAsState()
    val groceryLists by viewModel.groceryLists.collectAsState()

    val savedRecipesCount = allRecipes.count { !it.isDraft && it.isBookmarked }
    val groceryItemsRemainingCount = groceryLists.filter { !it.isCompleted }.sumOf { it.remainingItems }

    val myRecipes = allRecipes.filter { recipe ->
        isLoggedIn && (
            recipe.ownerEmail.equals(loggedInEmail, ignoreCase = true) ||
                (recipe.ownerEmail.isBlank() && recipe.chefName.equals(loggedInName, ignoreCase = true))
            )
    }
    val myPublishedRecipes = myRecipes.filter { !it.isDraft }
    val myDraftRecipes = myRecipes.filter { it.isDraft }

    var showAuthDialog by remember { mutableStateOf(false) }
    var authDialogReason by remember { mutableStateOf("") }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAlarmDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    val activeAlarmPreset = AlarmSoundPreset.fromId(alarmSoundId)
    val activeAlarmLabel = if (activeAlarmPreset == AlarmSoundPreset.CUSTOM && customAlarmName.isNotBlank()) {
        customAlarmName
    } else {
        activeAlarmPreset.label
    }

    if (showAlarmDialog) {
        AlarmSoundPickerDialog(
            selectedSoundId = alarmSoundId,
            customAudioName = customAlarmName,
            isPreviewing = isPreviewingAlarm,
            onSelectPreset = { viewModel.selectAlarmPreset(it) },
            onPickCustomAudio = { uri, cb -> viewModel.saveCustomAlarmFromUri(uri, cb) },
            onTogglePreview = { viewModel.previewAlarmSound() },
            onStopPreview = { viewModel.stopAlarm() },
            onDismiss = { showAlarmDialog = false }
        )
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Member Feature", fontWeight = FontWeight.Bold, color = DarkText)
            },
            text = {
                Text(authDialogReason, fontSize = 13.sp, color = DarkTextSecondary)
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
                    Text("Cancel", color = DarkText)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text("Delete Account", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
            },
            text = {
                Text(
                    "Are you sure you want to delete your account? All personal recipes, drafts, and grocery lists will be permanently removed.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = DarkText)
                }
            }
        )
    }

    if (recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text("Delete Recipe", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
            text = {
                Text("Delete \"${recipeToDelete?.title}\" permanently?", fontSize = 13.sp, color = DarkTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        recipeToDelete?.let { viewModel.deleteRecipe(it.id) }
                        recipeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text("Cancel", color = DarkText)
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("EasyCooking Help & FAQ", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                Text(
                    "1. Dietary & Allergies: Toggle Strict Allergen Filter to automatically hide unsafe recipes.\n" +
                        "2. Cooking Mode: Keeps the screen awake and includes a +1 min timer grace window before advancing steps.\n" +
                        "3. Grocery Lists: Export scaled ingredients from any recipe and track items by aisle.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

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
                    text = "Account",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCreatorMode) {
                        Button(
                            onClick = onNewRecipeClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryYellow,
                                contentColor = DarkText
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("+ New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help", tint = DarkText)
                        }
                    }
                }
            }
        }

        if (!isCreatorMode) {
            item {
                AnimatedVisibility(visible = offlineBannerVisible) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGrayBg)
                            .padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = AmberBadgeText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Local Storage Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Text("Your recipes, bookmarks, and lists stay saved on this device.", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                        }
                        IconButton(onClick = { offlineBannerVisible = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGrayBg)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = DarkText, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isLoggedIn && loggedInName.isNotBlank()) loggedInName else "Guest User",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isLoggedIn) "- Member" else "- Guest",
                                        fontSize = 11.sp,
                                        color = DarkTextSecondary
                                    )
                                }
                                Text(
                                    text = if (isLoggedIn && loggedInEmail.isNotBlank()) loggedInEmail else "Sign in to access saved recipes, creator mode & grocery lists.",
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("$savedRecipesCount Saved", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    Text("Bookmarked Recipes", fontSize = 11.sp, color = DarkTextSecondary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("$groceryItemsRemainingCount Items", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    Text("To Buy", fontSize = 11.sp, color = DarkTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isLoggedIn) {
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Sign Out", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            }
                        } else {
                            PrimaryYellowButton(
                                text = "Sign In / Register",
                                onClick = onSignInClick
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountMenuItem(
                        title = "App Preferences & Allergies",
                        subtitle = "Manage dietary filters and strict allergen protection",
                        onClick = onDietaryPreferencesClick
                    )
                    AccountMenuItem(
                        title = "Measurement Units",
                        subtitle = if (isMetricUnit) "Grams & milliliters (Metric)" else "Ounces & fluid ounces (Imperial)",
                        trailingBadge = if (isMetricUnit) "Metric" else "Imperial",
                        onClick = {
                            if (isLoggedIn) {
                                viewModel.toggleMeasurementUnit()
                            } else {
                                authDialogReason = "Please sign in or register to customize measurement units."
                                showAuthDialog = true
                            }
                        }
                    )
                    AccountMenuItem(
                        title = "Timer Alarm Sound",
                        subtitle = "Choose a built-in chime or custom audio file",
                        trailingBadge = activeAlarmLabel,
                        onClick = { showAlarmDialog = true }
                    )
                    AccountMenuItem(
                        title = "Switch to Creator View",
                        subtitle = "Manage your published recipes & drafts",
                        trailingBadge = "Creator",
                        onClick = {
                            if (isLoggedIn) {
                                isCreatorMode = true
                            } else {
                                authDialogReason = "Please sign in or register to access Creator Mode."
                                showAuthDialog = true
                            }
                        }
                    )
                    if (isLoggedIn) {
                        AccountMenuItem(
                            title = "Delete Account",
                            subtitle = "Permanently remove your account and local data",
                            onClick = { showDeleteAccountDialog = true }
                        )
                    }
                    AccountMenuItem(
                        title = "Help & Support",
                        subtitle = "FAQ & kitchen tips",
                        onClick = { showHelpDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = "EasyCooking | v1.0 (${if (isLoggedIn) "Member" else "Guest"})",
                    fontSize = 11.sp,
                    color = DarkTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGrayBg)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AmberBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = AmberBadgeText, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(loggedInName.ifBlank { "Creator" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryYellow)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Creator", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    }
                                }
                                Text("${myPublishedRecipes.size} Published | ${myDraftRecipes.size} Drafts", fontSize = 12.sp, color = DarkTextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val ratedRecipes = myPublishedRecipes.filter { it.reviewCount > 0 }
                                val avgRating = if (ratedRecipes.isNotEmpty()) {
                                    val avg = ratedRecipes.map { it.rating }.average()
                                    (avg * 10.0).roundToInt() / 10.0
                                } else 0.0
                                Text(avgRating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Exit Creator Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberBadgeText,
                            modifier = Modifier
                                .clickable { isCreatorMode = false }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = DarkText,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = true),
                            color = PrimaryYellow
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Published (${myPublishedRecipes.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Drafts (${myDraftRecipes.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            val displayedCreatorRecipes = if (selectedTab == 0) myPublishedRecipes else myDraftRecipes

            if (displayedCreatorRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedTab == 0) "No published recipes yet" else "No saved drafts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedTab == 0) "Create and share your first homemade recipe." else "Save a recipe as draft during upload to resume later.",
                                fontSize = 12.sp,
                                color = DarkTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNewRecipeClick,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("+ Create New Recipe", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(displayedCreatorRecipes, key = { it.id }) { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightGrayBg)
                            .clickable { onRecipeClick(recipe.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RecipeImageBox(
                            imageUri = recipe.imageUri,
                            fallbackColor = Color(0xFFCBD5E1),
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            if (recipe.imageUri.isBlank()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Restaurant, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            val statusText = when {
                                recipe.isDraft && recipe.ingredients.isEmpty() -> "Draft (no ingredients)"
                                recipe.isDraft -> "Draft"
                                else -> "Published"
                            }
                            Text("${recipe.category} | ${recipe.durationMinutes}m | $statusText", fontSize = 11.sp, color = DarkTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (recipe.isDraft) {
                                val canPublishDraft = recipe.ingredients.isNotEmpty()
                                IconButton(
                                    onClick = { viewModel.publishExistingDraft(recipe) },
                                    enabled = canPublishDraft
                                ) {
                                    Icon(
                                        Icons.Filled.Publish,
                                        contentDescription = "Publish Draft",
                                        tint = if (canPublishDraft) DarkText else DarkTextSecondary.copy(alpha = 0.4f)
                                    )
                                }
                            } else {
                                Text(recipe.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { recipeToDelete = recipe }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFB91C1C))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun AccountMenuItem(
    title: String,
    subtitle: String,
    trailingBadge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LightGrayBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(subtitle, fontSize = 11.sp, color = DarkTextSecondary)
        }
        if (trailingBadge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AmberBadgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(trailingBadge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberBadgeText)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = DarkTextSecondary)
    }
}

@Composable
fun DietaryAllergiesScreen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val selectedLifestyles by viewModel.selectedDietaryLifestyles.collectAsState()
    val selectedAllergens by viewModel.selectedAllergens.collectAsState()
    val strictFilter by viewModel.strictAllergenFilter.collectAsState()

    val lifestyles = listOf("Vegetarian", "Keto", "Vegan", "Gluten-Free", "Pescatarian", "Dairy-Free")
    val allergens = listOf("Dairy", "Gluten", "Fish", "Shellfish", "Eggs", "Tree Nuts", "Soy", "Peanuts")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
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
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                }
                Text("Dietary & Allergies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text(
                    "Skip",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onBackClick)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Personalize Your Plate", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select dietary habits and allergies so we can curate safe, delicious recommendations.",
                fontSize = 13.sp,
                color = DarkTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dietary Lifestyle", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text("${selectedLifestyles.size} active", fontSize = 12.sp, color = DarkTextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lifestyles.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { lifestyle ->
                            val isSelected = selectedLifestyles.contains(lifestyle)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryYellow else LightGrayBg)
                                    .clickable { viewModel.toggleDietaryLifestyle(lifestyle) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lifestyle,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = DarkText
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Allergies & Intolerances", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text("${selectedAllergens.size} chosen", fontSize = 12.sp, color = DarkTextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allergens.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { allergen ->
                            val isSelected = selectedAllergens.contains(allergen)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .sizeIn(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryYellow else LightGrayBg)
                                    .clickable { viewModel.toggleAllergen(allergen) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = allergen,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = DarkText
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = AmberBadgeText, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Strict Allergen Filter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        Text("Hide recipes with selected allergens", fontSize = 11.sp, color = DarkTextSecondary)
                    }
                }
                Switch(
                    checked = strictFilter,
                    onCheckedChange = { viewModel.setStrictAllergenFilter(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = DarkText, checkedTrackColor = PrimaryYellow)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            val totalCount = selectedLifestyles.size + selectedAllergens.size
            PrimaryYellowButton(
                text = "Save Preferences ($totalCount Selected)",
                onClick = onSaveSuccess
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now", color = DarkTextSecondary, fontSize = 13.sp)
            }
        }
    }
}

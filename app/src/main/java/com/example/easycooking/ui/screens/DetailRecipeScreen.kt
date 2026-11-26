package com.example.easycooking.ui.screens

import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.model.AlarmSoundPreset
import com.example.easycooking.model.ExportGroceryResult
import com.example.easycooking.model.ScalingMode
import com.example.easycooking.ui.components.AlarmSoundPickerDialog
import com.example.easycooking.ui.components.ExportGroceryResultDialog
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.components.RecipeImageBox
import com.example.easycooking.ui.theme.AmberBadgeBg
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.BorderGray
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.GreenSuccessBg
import com.example.easycooking.ui.theme.GreenSuccessText
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow
import com.example.easycooking.ui.theme.TipBannerBg
import com.example.easycooking.ui.theme.TipBannerText
import com.example.easycooking.viewmodel.CookingViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onStartCookingClick: (Int) -> Unit,
    onWriteReviewClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSignInClick: () -> Unit = {},
    onOpenGroceryList: ((String) -> Unit)? = null,
    openReviewsTab: Boolean = false,
    onReviewsTabOpened: () -> Unit = {}
) {
    val context = LocalContext.current
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId }
    if (recipe == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recipe not found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryYellowButton(text = "Go Back", onClick = onBackClick, showArrow = false)
        }
        return
    }

    LaunchedEffect(recipe.id) {
        viewModel.initRecipeDetail(recipe.id, recipe.defaultServings)
    }

    val servings by viewModel.servingsCount.collectAsState()
    val checkedIngredientsMap by viewModel.detailCheckedIngredients.collectAsState()
    val checkedIds = checkedIngredientsMap[recipe.id].orEmpty()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(openReviewsTab) {
        if (openReviewsTab) {
            selectedTabIndex = 2
            onReviewsTabOpened()
        }
    }
    val tabTitles = listOf("Ingredients", "Steps", "Reviews")
    var exportResult by remember { mutableStateOf<ExportGroceryResult?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authDialogReason by remember { mutableStateOf("") }
    val isMetricUnit by viewModel.isMetricUnit.collectAsState()

    exportResult?.let { result ->
        ExportGroceryResultDialog(
            result = result,
            onDismiss = { exportResult = null },
            onOpenList = onOpenGroceryList
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
                    Text("Later", color = DarkText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Text(
                text = "Recipe Hub",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val shareBody = buildString {
                        appendLine("${recipe.title} (${recipe.durationMinutes} min | ${recipe.difficulty})")
                        appendLine("By ${recipe.chefName}")
                        appendLine()
                        appendLine("Ingredients ($servings servings):")
                        recipe.ingredients.forEach { ing ->
                            val qty = ing.formattedAmount(
                                servings.toDouble() / recipe.defaultServings.coerceAtLeast(1).toDouble(),
                                isMetric = isMetricUnit
                            )
                            appendLine("- $qty ${ing.name}")
                        }
                    }
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, recipe.title)
                        putExtra(Intent.EXTRA_TEXT, shareBody)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = DarkText)
                }
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7D5800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item {
                val totalCookingMins = recipe.steps.sumOf { it.durationMinutes }.coerceAtLeast(recipe.durationMinutes)
                RecipeImageBox(
                    imageUri = recipe.imageUri,
                    fallbackColor = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            onStartCookingClick(recipe.steps.firstOrNull()?.durationMinutes ?: 5)
                        }
                ) {
                    if (recipe.imageUri.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                    }
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PrimaryYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Start Interactive Cooking",
                                tint = DarkText,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start Guided Cooking",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$totalCookingMins min guide",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = recipe.chefName,
                                fontSize = 13.sp,
                                color = DarkTextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${recipe.difficulty} | ${recipe.durationMinutes}m",
                                fontSize = 13.sp,
                                color = AmberBadgeText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (isLoggedIn) {
                            viewModel.toggleBookmark(recipe.id)
                        } else {
                            authDialogReason = "Please sign in or register to bookmark recipes."
                            showAuthDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = if (recipe.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (recipe.isBookmarked) PrimaryYellow else DarkTextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = DarkText,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                            color = PrimaryYellow
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = if (index == 2) "$title (${recipe.reviews.size})" else title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) DarkText else DarkTextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedTabIndex == 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGrayBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Servings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Text(
                                text = "Auto-recalculates quantities",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .clickable { viewModel.decrementServings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease servings", tint = DarkText, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = servings.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .clickable { viewModel.incrementServings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase servings", tint = DarkText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (recipe.ingredients.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightGrayBg)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No ingredients listed for this recipe.",
                                fontSize = 13.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (checkedIds.isEmpty()) {
                                    "Tap ingredients to select items for grocery"
                                } else {
                                    "${checkedIds.size} of ${recipe.ingredients.size} selected"
                                },
                                fontSize = 12.sp,
                                color = DarkTextSecondary
                            )
                            val allSelected = checkedIds.size == recipe.ingredients.size
                            Text(
                                text = if (allSelected) "Clear" else "Select All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberBadgeText,
                                modifier = Modifier
                                    .clickable {
                                        if (allSelected) {
                                            viewModel.setDetailIngredientsChecked(recipe.id, emptySet())
                                        } else {
                                            viewModel.setDetailIngredientsChecked(
                                                recipe.id,
                                                recipe.ingredients.map { it.id }.toSet()
                                            )
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    items(recipe.ingredients, key = { it.id }) { ingredient ->
                        val isChecked = checkedIds.contains(ingredient.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightGrayBg)
                                .clickable { viewModel.toggleDetailIngredientChecked(recipe.id, ingredient.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleDetailIngredientChecked(recipe.id, ingredient.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryYellow,
                                        checkmarkColor = DarkText
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = ingredient.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkText
                                    )
                                    if (ingredient.subtitle.isNotBlank() || ingredient.scalingMode != ScalingMode.PROPORTIONAL) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (ingredient.subtitle.isNotBlank()) {
                                                Text(
                                                    text = ingredient.subtitle,
                                                    fontSize = 11.sp,
                                                    color = DarkTextSecondary
                                                )
                                            }
                                            if (ingredient.scalingMode != ScalingMode.PROPORTIONAL) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(AmberBadgeBg)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = ingredient.scalingMode.shortLabel,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = AmberBadgeText
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = ingredient.formattedAmount(
                                        servings.toDouble() / recipe.defaultServings.coerceAtLeast(1).toDouble(),
                                        isMetric = isMetricUnit
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (selectedTabIndex == 1) {
                if (recipe.steps.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightGrayBg)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No preparation steps listed for this recipe.",
                                fontSize = 13.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }
                } else {
                    items(recipe.steps, key = { it.stepNumber }) { step ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LightGrayBg)
                                .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryYellow),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = step.stepNumber.toString(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkText
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = step.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkText
                                        )
                                    }

                                    Text(
                                        text = "${step.durationMinutes} min",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = step.description,
                                    fontSize = 13.sp,
                                    color = DarkTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightGrayBg)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${recipe.rating} / 5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                            }
                            Text("${recipe.reviewCount} foodies cooked this", fontSize = 11.sp, color = DarkTextSecondary)
                        }

                        OutlinedButton(
                            onClick = {
                                if (isLoggedIn) {
                                    onWriteReviewClick()
                                } else {
                                    authDialogReason = "Please sign in or register to rate and review recipes."
                                    showAuthDialog = true
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("+ Review", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                item {
                    val totalReviews = recipe.reviews.size.coerceAtLeast(1).toFloat()
                    val starDistribution = listOf(5, 4, 3, 2, 1).map { star ->
                        val count = recipe.reviews.count { it.rating == star }
                        Pair(star, count.toFloat() / totalReviews)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGrayBg)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                text = "${recipe.rating}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Row {
                                repeat(5) { starIndex ->
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (starIndex < recipe.rating.toInt()) StarYellow else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${recipe.reviews.size} reviews",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            starDistribution.forEach { (star, pct) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Text("$star", fontSize = 10.sp, color = DarkTextSecondary, modifier = Modifier.width(10.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = PrimaryYellow,
                                        trackColor = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (recipe.reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightGrayBg)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No reviews yet. Be the first to review this recipe!",
                                fontSize = 13.sp,
                                color = DarkTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(recipe.reviews, key = { it.id }) { review ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LightGrayBg)
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = review.userName.take(2).uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkText
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = review.userName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkText
                                        )
                                    }
                                    Text(
                                        text = review.timeAgo,
                                        fontSize = 11.sp,
                                        color = DarkTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    repeat(review.rating) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = review.comment,
                                    fontSize = 13.sp,
                                    color = DarkText,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(thickness = 1.dp, color = BorderGray)
                Box(modifier = Modifier.padding(16.dp)) {
                    if (selectedTabIndex == 0) {
                        val exportButtonText = if (checkedIds.isNotEmpty()) {
                            "Export Selected (${checkedIds.size}) to Grocery"
                        } else {
                            "Export All to Grocery Checklist"
                        }
                        PrimaryYellowButton(
                            text = exportButtonText,
                            onClick = {
                                if (isLoggedIn) {
                                    viewModel.exportRecipeToGrocery(recipe, servings, checkedIds) { result ->
                                        exportResult = result
                                    }
                                } else {
                                    authDialogReason = "Please sign in or register to export ingredients to a grocery checklist."
                                    showAuthDialog = true
                                }
                            }
                        )
                    } else if (selectedTabIndex == 1) {
                        PrimaryYellowButton(
                            text = "Start Cooking Mode",
                            onClick = {
                                onStartCookingClick(recipe.steps.firstOrNull()?.durationMinutes ?: 5)
                            }
                        )
                    } else {
                        PrimaryYellowButton(
                            text = "Write a Review",
                            onClick = {
                                if (isLoggedIn) {
                                    onWriteReviewClick()
                                } else {
                                    authDialogReason = "Please sign in or register to write a review."
                                    showAuthDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CookingModeScreen(
    recipeId: String,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onFinishCookingClick: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.pauseTimer()
        }
    }

    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull()
    val steps = recipe?.steps.orEmpty()

    if (recipe == null || steps.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No cooking steps available for this recipe.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryYellowButton(text = "Back to Recipe", onClick = onBackClick, showArrow = false)
        }
        return
    }

    LaunchedEffect(recipe.id) {
        viewModel.initCookingMode(recipe.id, steps.first().durationMinutes)
    }

    val currentStepIndex by viewModel.currentCookingStep.collectAsState()
    val safeStepIndex = currentStepIndex.coerceIn(1, steps.size)
    val currentStep = steps[safeStepIndex - 1]

    val timerSecondsLeft by viewModel.timerSecondsLeft.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isTimerDone by viewModel.isTimerDone.collectAsState()
    val autoAdvanceSecondsLeft by viewModel.autoAdvanceSecondsLeft.collectAsState()
    val alarmSoundId by viewModel.alarmSoundId.collectAsState()
    val customAlarmName by viewModel.customAlarmName.collectAsState()
    val isPreviewingAlarm by viewModel.isPreviewingAlarm.collectAsState()
    var showAlarmPicker by remember { mutableStateOf(false) }

    val activeAlarmPreset = AlarmSoundPreset.fromId(alarmSoundId)
    val activeAlarmLabel = if (activeAlarmPreset == AlarmSoundPreset.CUSTOM && customAlarmName.isNotBlank()) {
        customAlarmName
    } else {
        activeAlarmPreset.label
    }

    if (showAlarmPicker) {
        AlarmSoundPickerDialog(
            selectedSoundId = alarmSoundId,
            customAudioName = customAlarmName,
            isPreviewing = isPreviewingAlarm,
            onSelectPreset = { viewModel.selectAlarmPreset(it) },
            onPickCustomAudio = { uri, cb -> viewModel.saveCustomAlarmFromUri(uri, cb) },
            onTogglePreview = { viewModel.previewAlarmSound() },
            onStopPreview = { viewModel.stopAlarm() },
            onDismiss = { showAlarmPicker = false }
        )
    }

    val latestStepIndex by rememberUpdatedState(safeStepIndex)
    val latestSteps by rememberUpdatedState(steps)
    val latestOnFinish by rememberUpdatedState(onFinishCookingClick)

    LaunchedEffect(Unit) {
        viewModel.autoAdvanceEvent.collect {
            val cur = latestStepIndex
            val list = latestSteps
            if (cur < list.size) {
                val next = cur + 1
                val nextDur = list.getOrNull(next - 1)?.durationMinutes ?: 5
                viewModel.setCookingStep(next, nextDur)
            } else {
                latestOnFinish()
            }
        }
    }

    val progress = viewModel.calculateProgress(safeStepIndex, steps.size)
    val timerMinutes = timerSecondsLeft / 60
    val timerSeconds = timerSecondsLeft % 60
    val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", timerMinutes, timerSeconds)

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
                Text(
                    text = "Cooking Mode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                IconButton(onClick = {
                    val shareStep = "${recipe.title} - Step $safeStepIndex: ${currentStep.title}\n${currentStep.description}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareStep)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Cooking Step"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share Step", tint = DarkText)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryYellow,
                trackColor = LightGrayBg
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(
                targetState = safeStepIndex,
                transitionSpec = {
                    val isForward = targetState >= initialState
                    val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(
                        durationMillis = 220,
                        easing = FastOutSlowInEasing
                    )
                    if (isForward) {
                        slideInHorizontally(animationSpec = slideSpec) { it } togetherWith
                            slideOutHorizontally(animationSpec = slideSpec) { -(it * 0.25f).toInt() }
                    } else {
                        slideInHorizontally(animationSpec = slideSpec) { -(it * 0.25f).toInt() } togetherWith
                            slideOutHorizontally(animationSpec = slideSpec) { it }
                    }
                },
                label = "cooking_step_transition"
            ) { animatedStepIndex ->
                val animatedStep = steps[(animatedStepIndex - 1).coerceIn(0, steps.lastIndex)]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = animatedStep.stepNumber.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recipe.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        }

                        if (animatedStep.isFinalStep || animatedStepIndex == steps.size) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberBadgeBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Final Step",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberBadgeText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val statusLabel = when {
                                autoAdvanceSecondsLeft != null -> "Time's up! Moving on in ${autoAdvanceSecondsLeft}s"
                                isTimerDone -> "Step Complete!"
                                else -> "Keep Screen Awake Active"
                            }
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "METHOD",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = animatedStep.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightGrayBg)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = animatedStep.description,
                            fontSize = 14.sp,
                            color = DarkText,
                            lineHeight = 22.sp
                        )
                    }

                    if (animatedStep.tip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TipBannerBg)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Chef's Pro Tip",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberBadgeText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = animatedStep.tip,
                                    fontSize = 12.sp,
                                    color = TipBannerText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isTimerDone) GreenSuccessBg else LightGrayBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.startStepTimer(currentStep.durationMinutes) }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isTimerDone -> Icons.Filled.Check
                            isTimerRunning -> Icons.Filled.Pause
                            else -> Icons.Filled.PlayArrow
                        },
                        contentDescription = "Timer Toggle",
                        tint = if (isTimerDone) GreenSuccessText else PrimaryYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTimerDone) "Done!" else formattedTime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTimerDone) GreenSuccessText else DarkText
                        )
                        val timerSub = when {
                            autoAdvanceSecondsLeft != null -> {
                                val nextLabel = if (safeStepIndex < steps.size) "next step" else "summary"
                                "Next: $nextLabel in ${autoAdvanceSecondsLeft}s (tap +1 min to add time)"
                            }
                            isTimerDone -> "Cooking stage finished"
                            isTimerRunning -> "Timer running (tap to pause)"
                            else -> "Tap to start countdown"
                        }
                        Text(
                            text = timerSub,
                            fontSize = 11.sp,
                            color = if (autoAdvanceSecondsLeft != null) GreenSuccessText else DarkTextSecondary,
                            fontWeight = if (autoAdvanceSecondsLeft != null) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (autoAdvanceSecondsLeft != null) PrimaryYellow else Color.White)
                            .clickable { viewModel.addExtraMinute() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "+1 min",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
                    if (autoAdvanceSecondsLeft != null) {
                        Text(
                            text = "Stay here",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary,
                            modifier = Modifier
                                .clickable { viewModel.cancelAutoAdvanceGrace() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGrayBg)
                    .clickable { showAlarmPicker = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = DarkText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alarm: $activeAlarmLabel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                }
                Text(
                    text = "Change",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberBadgeText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Column {
            PrimaryYellowButton(
                text = "I Have Finished Cooking",
                onClick = onFinishCookingClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< Previous Step",
                    fontSize = 13.sp,
                    color = if (safeStepIndex > 1) DarkText else DarkTextSecondary.copy(alpha = 0.45f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(enabled = safeStepIndex > 1) {
                            val prev = safeStepIndex - 1
                            val prevDuration = steps.getOrNull(prev - 1)?.durationMinutes ?: 5
                            viewModel.setCookingStep(prev, prevDuration)
                        }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )

                Text(
                    text = "Step $safeStepIndex of ${steps.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextSecondary
                )

                Text(
                    text = "Next >",
                    fontSize = 13.sp,
                    color = if (safeStepIndex < steps.size) DarkText else DarkTextSecondary.copy(alpha = 0.45f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(enabled = safeStepIndex < steps.size) {
                            val next = safeStepIndex + 1
                            val nextDuration = steps.getOrNull(next - 1)?.durationMinutes ?: 5
                            viewModel.setCookingStep(next, nextDuration)
                        }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun FinishedCookingScreen(
    recipeId: String,
    viewModel: CookingViewModel,
    onRateRecipeClick: (Int) -> Unit,
    onDoneClick: () -> Unit,
    onSignInClick: () -> Unit = {}
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull() ?: return
    var userRating by remember { mutableIntStateOf(5) }
    var showAuthDialog by remember { mutableStateOf(false) }
    val stepsCount = recipe.steps.size.coerceAtLeast(1)

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Member Feature", fontWeight = FontWeight.Bold, color = DarkText)
            },
            text = {
                Text("Please sign in or register to rate and review recipes.", fontSize = 13.sp, color = DarkTextSecondary)
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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDoneClick) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = DarkText)
                }
                Text(
                    text = "Finished Cooking",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryYellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = DarkText,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Bon Appetit!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You've completed ${recipe.title} in ${recipe.durationMinutes} mins!",
                fontSize = 14.sp,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightGrayBg)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RecipeImageBox(
                        imageUri = recipe.imageUri,
                        fallbackColor = Color(0xFFCBD5E1),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "COMPLETED DISH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextSecondary
                        )
                        Text(
                            text = recipe.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Crafted with ${recipe.chefName}",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${recipe.durationMinutes}m", fontSize = 11.sp, color = DarkText)
                            Text("${recipe.calories} kcal", fontSize = 11.sp, color = DarkText)
                            Text("$stepsCount/$stepsCount steps", fontSize = 11.sp, color = DarkText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightGrayBg)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How did it turn out?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "Share feedback to inspire fellow home chefs!",
                        fontSize = 12.sp,
                        color = DarkTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            Box(
                                modifier = Modifier
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .clickable { userRating = star },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= userRating) StarYellow else Color.LightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rating ($userRating / 5)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            PrimaryYellowButton(
                text = "Rate & Review Recipe",
                onClick = {
                    if (isLoggedIn) {
                        onRateRecipeClick(userRating)
                    } else {
                        showAuthDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDoneClick)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done for Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary
                )
            }
        }
    }
}

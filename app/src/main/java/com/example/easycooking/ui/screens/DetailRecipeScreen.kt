package com.example.easycooking.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.easycooking.model.Recipe
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.theme.BorderGray
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow
import com.example.easycooking.viewmodel.CookingViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onStartCookingClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSignInClick: () -> Unit = {}
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull() ?: return
    val servings by viewModel.servingsCount.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Ingredients", "Steps", "Reviews")
    var exportSuccessMessage by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authDialogReason by remember { mutableStateOf("") }
    val isMetricUnit by viewModel.isMetricUnit.collectAsState()

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Fitur Khusus Pengguna Terdaftar", fontWeight = FontWeight.Bold, color = DarkText)
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
                    Text("Masuk / Daftar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Nanti Saja", color = DarkText)
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
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = DarkText)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7D5800))
                        .clickable(onClick = onProfileClick),
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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
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
                            contentDescription = "Play Video",
                            tint = DarkText,
                            modifier = Modifier.size(32.dp)
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
                            text = "04:15",
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
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (isLoggedIn) {
                            viewModel.toggleBookmark(recipe.id)
                        } else {
                            authDialogReason = "Menyimpan (bookmark) resep hanya dapat dilakukan oleh pengguna terdaftar."
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
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = DarkText,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
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
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { viewModel.decrementServings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = DarkText, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = servings.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryYellow)
                                    .clickable { viewModel.incrementServings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase", tint = DarkText, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                items(recipe.ingredients) { ingredient ->
                    var isChecked by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGrayBg)
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
                                onCheckedChange = { isChecked = it },
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
                                if (ingredient.subtitle.isNotBlank()) {
                                    Text(
                                        text = ingredient.subtitle,
                                        fontSize = 11.sp,
                                        color = DarkTextSecondary
                                    )
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
                                text = ingredient.formattedAmount(servings.toDouble() / recipe.defaultServings.toDouble(), isMetric = isMetricUnit),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = exportSuccessMessage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Exported to Grocery Checklist successfully!",
                                color = Color(0xFF15803D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else if (selectedTabIndex == 1) {
                items(recipe.steps) { step ->
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
                                Text("4.8 / 5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                            }
                            Text("420 foodies cooked this", fontSize = 11.sp, color = DarkTextSecondary)
                        }

                        OutlinedButton(
                            onClick = {
                                if (isLoggedIn) {
                                    onWriteReviewClick()
                                } else {
                                    authDialogReason = "Memberi rating dan ulasan resep hanya dapat dilakukan oleh pengguna terdaftar."
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
                                text = "4.8",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Row {
                                repeat(5) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(
                                text = "42 reviews",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            listOf(Pair(5, 0.85f), Pair(4, 0.12f), Pair(3, 0.03f), Pair(2, 0.0f), Pair(1, 0.0f)).forEach { (star, pct) ->
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

                items(recipe.reviews) { review ->
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (selectedTabIndex == 0) {
                    PrimaryYellowButton(
                        text = "Export to Grocery Checklist",
                        onClick = {
                            if (isLoggedIn) {
                                viewModel.exportRecipeToGrocery(recipe)
                                exportSuccessMessage = true
                            } else {
                                authDialogReason = "Mengekspor bahan resep ke daftar belanja hanya dapat dilakukan oleh pengguna terdaftar."
                                showAuthDialog = true
                            }
                        }
                    )
                } else if (selectedTabIndex == 1) {
                    PrimaryYellowButton(
                        text = "Start Cooking Mode",
                        onClick = onStartCookingClick
                    )
                } else {
                    PrimaryYellowButton(
                        text = "Write a Review",
                        onClick = {
                            if (isLoggedIn) {
                                onWriteReviewClick()
                            } else {
                                authDialogReason = "Memberi rating dan ulasan resep hanya dapat dilakukan oleh pengguna terdaftar."
                                showAuthDialog = true
                            }
                        }
                    )
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
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull() ?: return
    val currentStepIndex by viewModel.currentCookingStep.collectAsState()
    val steps = recipe.steps
    val currentStep = steps.getOrNull(currentStepIndex - 1) ?: steps.lastOrNull() ?: return

    val timerSecondsLeft by viewModel.timerSecondsLeft.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isTimerDone by viewModel.isTimerDone.collectAsState()

    LaunchedEffect(currentStep.stepNumber) {
        viewModel.resetTimer(currentStep.durationMinutes)
    }

    val progress = viewModel.calculateProgress(currentStepIndex, steps.size)

    val timerMinutes = timerSecondsLeft / 60
    val timerSeconds = timerSecondsLeft % 60
    val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", timerMinutes, timerSeconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
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
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = DarkText)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimaryYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentStep.stepNumber.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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

                if (currentStep.isFinalStep) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFE0B2))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Final Step",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF334155))
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isTimerDone) "Step Complete!" else "Keep Screen Awake Active",
                        fontSize = 11.sp,
                        color = PrimaryYellow,
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
                        text = currentStep.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LightGrayBg)
                    .padding(16.dp)
            ) {
                Text(
                    text = currentStep.description,
                    fontSize = 14.sp,
                    color = DarkText,
                    lineHeight = 22.sp
                )
            }

            if (currentStep.tip.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Chef's Pro Tip",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStep.tip,
                            fontSize = 12.sp,
                            color = Color(0xFFBF360C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isTimerDone) Color(0xFFE8F5E9) else LightGrayBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.startStepTimer(currentStep.durationMinutes) }
                ) {
                    Icon(
                        imageVector = when {
                            isTimerDone -> Icons.Filled.Check
                            isTimerRunning -> Icons.Filled.Pause
                            else -> Icons.Filled.PlayArrow
                        },
                        contentDescription = "Timer Toggle",
                        tint = if (isTimerDone) Color(0xFF2E7D32) else PrimaryYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTimerDone) "Done!" else formattedTime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTimerDone) Color(0xFF2E7D32) else DarkText
                        )
                        Text(
                            text = when {
                                isTimerDone -> "Cooking stage finished"
                                isTimerRunning -> "Timer running (tap to pause)"
                                else -> "Tap to start countdown"
                            },
                            fontSize = 11.sp,
                            color = DarkTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { viewModel.addExtraMinute() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("+1 min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                }
            }
        }

        Column {
            PrimaryYellowButton(
                text = "I Have Finished Cooking",
                onClick = onFinishCookingClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< Previous Step",
                    fontSize = 13.sp,
                    color = if (currentStepIndex > 1) DarkText else Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = currentStepIndex > 1) {
                        val prev = currentStepIndex - 1
                        val prevDuration = steps.getOrNull(prev - 1)?.durationMinutes ?: 5
                        viewModel.setCookingStep(prev, prevDuration)
                    }
                )

                Text(
                    text = "Step $currentStepIndex of ${steps.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextSecondary
                )

                Text(
                    text = "Next >",
                    fontSize = 13.sp,
                    color = if (currentStepIndex < steps.size) DarkText else Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = currentStepIndex < steps.size) {
                        val next = currentStepIndex + 1
                        val nextDuration = steps.getOrNull(next - 1)?.durationMinutes ?: 5
                        viewModel.setCookingStep(next, nextDuration)
                    }
                )
            }
        }
    }
}

@Composable
fun FinishedCookingScreen(
    recipeId: String,
    viewModel: CookingViewModel,
    onRateRecipeClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull() ?: return
    var userRating by remember { mutableIntStateOf(4) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFCBD5E1))
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
                            Text("6/6 steps", fontSize = 11.sp, color = DarkText)
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

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= userRating) StarYellow else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { userRating = star }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Great! ($userRating / 5)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextSecondary
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            PrimaryYellowButton(
                text = "Rate & Review Recipe",
                onClick = onRateRecipeClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDoneClick),
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

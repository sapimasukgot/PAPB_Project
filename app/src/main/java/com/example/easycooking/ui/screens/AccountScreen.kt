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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow
import com.example.easycooking.viewmodel.CookingViewModel

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
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loggedInName by viewModel.loggedInName.collectAsState()
    val loggedInEmail by viewModel.loggedInEmail.collectAsState()
    val allRecipes by viewModel.recipes.collectAsState()
    val myPublishedRecipes = allRecipes.filter {
        it.chefName.equals(loggedInName, ignoreCase = true) || it.id.startsWith("user_rec_")
    }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authDialogReason by remember { mutableStateOf("") }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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
                    Text("Batal", color = DarkText)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text("Hapus Akun", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus akun Anda? Seluruh data akun akan dihapus secara permanen.", fontSize = 13.sp, color = DarkTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Batal", color = DarkText)
                }
            }
        )
    }

    val creatorRecipes = listOf(
        Triple("Creamy Truffle Risotto", "Dinner | 25m | Published", "4.9"),
        Triple("Pan-Seared Salmon Salad", "Lunch | 15m | Published", "4.8"),
        Triple("Garlic Butter Steak Bites", "Dinner | 20m | Published", "5.0"),
        Triple("Honey Glazed Salmon", "Dinner | 18m | Published", "4.9"),
        Triple("Classic Margherita Pizza", "Dinner | 30m | Published", "4.7")
    )

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
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+ New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.HelpOutline, contentDescription = "Help", tint = DarkText)
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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Offline Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Text("Your culinary bookmarks are saved locally.", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                        }
                        IconButton(onClick = { offlineBannerVisible = false }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
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
                                        text = if (isLoggedIn) "- Member" else "- Local Storage",
                                        fontSize = 11.sp,
                                        color = DarkTextSecondary
                                    )
                                }
                                Text(
                                    text = if (isLoggedIn && loggedInEmail.isNotBlank()) loggedInEmail else "Sign in to sync recipes & grocery items across devices.",
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
                                    Text("4 Recipes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    Text("Unsynced", fontSize = 11.sp, color = DarkTextSecondary)
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
                                    Text("12 Items", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                    Text("In Pantry", fontSize = 11.sp, color = DarkTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isLoggedIn) {
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Sign Out", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
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
                        subtitle = "Manage filters & notifications",
                        onClick = onDietaryPreferencesClick
                    )
                    AccountMenuItem(
                        title = "Measurement Units",
                        subtitle = if (isMetricUnit) "Grams, milliliters, and Celsius | Metric (g, ml)" else "Ounces, cups, and Fahrenheit | Imperial (oz, cups)",
                        trailingBadge = if (isMetricUnit) "Metric" else "Imperial",
                        onClick = {
                            if (isLoggedIn) {
                                viewModel.toggleMeasurementUnit()
                            } else {
                                authDialogReason = "Mengonversi satuan takaran hanya dapat dilakukan oleh pengguna terdaftar."
                                showAuthDialog = true
                            }
                        }
                    )
                    AccountMenuItem(
                        title = "Switch to Creator View",
                        subtitle = "View published recipes & analytics",
                        trailingBadge = "Creator",
                        onClick = {
                            if (isLoggedIn) {
                                isCreatorMode = true
                            } else {
                                authDialogReason = "Fitur resep pribadi dan mode kreator hanya dapat diakses oleh pengguna terdaftar."
                                showAuthDialog = true
                            }
                        }
                    )
                    if (isLoggedIn) {
                        AccountMenuItem(
                            title = "Delete Account",
                            subtitle = "Permanently remove your account and data",
                            onClick = { showDeleteAccountDialog = true }
                        )
                    }
                    AccountMenuItem(
                        title = "Help & Support",
                        subtitle = "FAQ, kitchen tips & contact us",
                        onClick = { }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGrayBg)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFCBD5E1))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("RECIPE OF THE DAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary)
                            Text("Golden Artisan Sourdough", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Savor Kitchen Companion | v2.4.0 (${if (isLoggedIn) "Registered User" else "Guest"})",
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
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(32.dp))
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
                                Text("${myPublishedRecipes.size} Published", fontSize = 12.sp, color = DarkTextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val avgRating = if (myPublishedRecipes.isNotEmpty()) {
                                    val avg = myPublishedRecipes.map { it.rating }.average()
                                    Math.round(avg * 10.0) / 10.0
                                } else 5.0
                                Text(avgRating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Exit Creator Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309),
                            modifier = Modifier
                                .clickable { isCreatorMode = false }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = DarkText,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
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
                        text = { Text("Drafts (0)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (selectedTab == 0) {
                if (myPublishedRecipes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Belum ada resep yang dipublikasikan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Mulai unggah resep masakan Anda sekarang.", fontSize = 12.sp, color = DarkTextSecondary)
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onNewRecipeClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("+ Buat Resep Baru", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(myPublishedRecipes) { recipe ->
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
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recipe.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Text("${recipe.category} | ${recipe.durationMinutes}m | Published", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(recipe.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.deleteRecipe(recipe.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
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
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(trailingBadge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
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
    val allergens = listOf("Peanuts", "Shellfish", "Eggs", "Tree Nuts", "Wheat", "Soy")

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
                Text("Dietary & Allergies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text("Skip", fontSize = 14.sp, color = DarkTextSecondary, modifier = Modifier.clickable(onClick = onBackClick))
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryYellow else LightGrayBg)
                                    .clickable { viewModel.toggleDietaryLifestyle(lifestyle) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryYellow else LightGrayBg)
                                    .clickable { viewModel.toggleAllergen(allergen) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
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
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
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
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            val totalCount = selectedLifestyles.size + selectedAllergens.size
            PrimaryYellowButton(
                text = "Save Preferences ($totalCount Selected)",
                onClick = onSaveSuccess
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now", color = DarkTextSecondary, fontSize = 13.sp)
            }
        }
    }
}

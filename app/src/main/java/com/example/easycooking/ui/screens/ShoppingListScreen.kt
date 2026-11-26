package com.example.easycooking.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.model.ExportGroceryResult
import com.example.easycooking.model.GroceryCategories
import com.example.easycooking.model.GroceryList
import com.example.easycooking.ui.components.ExportGroceryResultDialog
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.components.RecipeImageBox
import com.example.easycooking.ui.theme.AmberBadgeBg
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.DarkAvatarBg
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.GreenSuccessBg
import com.example.easycooking.ui.theme.GreenSuccessText
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PlaceholderImageBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.TipBannerBg
import com.example.easycooking.ui.theme.TipBannerText
import com.example.easycooking.viewmodel.CookingViewModel

@Composable
fun GroceryOverviewScreen(
    viewModel: CookingViewModel,
    onListClick: (String) -> Unit,
    onDiscoverClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val groceryLists by viewModel.groceryLists.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val activeLists = groceryLists.filter { !it.isCompleted }
    val completedLists = groceryLists.filter { it.isCompleted }

    var showNewListDialog by remember { mutableStateOf(false) }
    var newListTitle by remember { mutableStateOf("") }
    var listToDelete by remember { mutableStateOf<GroceryList?>(null) }
    var exportResult by remember { mutableStateOf<ExportGroceryResult?>(null) }

    val totalRemainingItems = activeLists.sumOf { it.remainingItems }
    val suggestedRecipes = recipes.filter { !it.isDraft }.take(2)

    exportResult?.let { result ->
        ExportGroceryResultDialog(
            result = result,
            onDismiss = { exportResult = null },
            onOpenList = onListClick
        )
    }

    if (showNewListDialog) {
        AlertDialog(
            onDismissRequest = { showNewListDialog = false },
            title = { Text("Create Grocery List", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                OutlinedTextField(
                    value = newListTitle,
                    onValueChange = { newListTitle = it },
                    singleLine = true,
                    placeholder = { Text("e.g., Weekend Meal Prep", color = DarkTextSecondary) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = newListTitle.trim().ifBlank { "Custom Shopping List" }
                        showNewListDialog = false
                        newListTitle = ""
                        viewModel.createNewGroceryList(title) { createdId ->
                            onListClick(createdId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewListDialog = false }) {
                    Text("Cancel", color = DarkText)
                }
            }
        )
    }

    if (listToDelete != null) {
        AlertDialog(
            onDismissRequest = { listToDelete = null },
            title = { Text("Delete Grocery List", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
            text = {
                Text("Remove \"${listToDelete?.title}\" and all its items?", fontSize = 13.sp, color = DarkTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        listToDelete?.let { viewModel.deleteGroceryList(it.id) }
                        listToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { listToDelete = null }) {
                    Text("Cancel", color = DarkText)
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
                    text = "Grocery",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Button(
                    onClick = {
                        if (isLoggedIn) {
                            showNewListDialog = true
                        } else {
                            onSignInClick()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightGrayBg,
                        contentColor = DarkText
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New List", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            text = "Smart Grocery Checklist",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sign in or create an account to manage and sync your ingredient shopping lists.",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGrayBg)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = AmberBadgeText, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$totalRemainingItems items to buy",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Text(
                                text = "Across ${activeLists.size} active list(s)",
                                fontSize = 12.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val firstList = activeLists.firstOrNull()
                            if (firstList != null) {
                                onListClick(firstList.id)
                            } else {
                                showNewListDialog = true
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryYellow,
                            contentColor = DarkText
                        )
                    ) {
                        Text(if (activeLists.isNotEmpty()) "Start Shopping" else "Create List", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (activeLists.isEmpty() && completedLists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGrayBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No grocery lists yet", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Export ingredients from any recipe or tap + New List to start.",
                                fontSize = 12.sp,
                                color = DarkTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Lists (${activeLists.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Tap to view",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(activeLists, key = { it.id }) { list ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightGrayBg)
                            .clickable { onListClick(list.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val matchingImageUri = recipes.find { it.title.equals(list.title, ignoreCase = true) }?.imageUri.orEmpty()
                        RecipeImageBox(
                            imageUri = matchingImageUri,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = list.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${list.remainingItems}/${list.totalItems} items left | ${list.updatedAt}",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val progress = if (list.totalItems > 0) (list.totalItems - list.remainingItems).toFloat() / list.totalItems else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryYellow,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                        IconButton(onClick = { listToDelete = list }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete",
                                tint = DarkTextSecondary
                            )
                        }
                    }
                }

                if (completedLists.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Completed (${completedLists.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(completedLists, key = { it.id }) { list ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LightGrayBg)
                                .clickable { onListClick(list.id) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GreenSuccessBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = GreenSuccessText)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = list.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Text(
                                    text = "${list.totalItems}/${list.totalItems} completed | ${list.updatedAt}",
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                            IconButton(onClick = { listToDelete = list }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = DarkTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (suggestedRecipes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggested for Your Next Meal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "Discover",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberBadgeText,
                        modifier = Modifier
                            .clickable(onClick = onDiscoverClick)
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    suggestedRecipes.forEach { recipe ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LightGrayBg)
                                .padding(12.dp)
                        ) {
                            Column {
                                RecipeImageBox(
                                    imageUri = recipe.imageUri,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${recipe.durationMinutes} min",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(recipe.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText, maxLines = 1)
                                Text("${recipe.ingredients.size} ingredients", fontSize = 11.sp, color = DarkTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        if (isLoggedIn) {
                                            viewModel.exportRecipeToGrocery(recipe, recipe.defaultServings) { result ->
                                                exportResult = result
                                            }
                                        } else {
                                            onSignInClick()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("+ Export", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroceryDetailScreen(
    listId: String,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val groceryLists by viewModel.groceryLists.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val list = groceryLists.find { it.id == listId }

    if (list == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Grocery list not found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryYellowButton(text = "Back to Grocery Lists", onClick = onBackClick, showArrow = false)
        }
        return
    }

    val aisleSections = GroceryCategories.AISLE_SECTIONS
    var selectedSection by remember { mutableStateOf(GroceryCategories.DEFAULT_SECTION) }
    var newCustomItem by remember { mutableStateOf("") }
    val isMetricUnit by viewModel.isMetricUnit.collectAsState()
    val groupedItems = list.items
        .groupBy { GroceryCategories.refineCategory(it.name, it.category) }
        .toSortedMap(compareBy<String> { GroceryCategories.aisleSortIndex(it) }.thenBy { it })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Text(
                text = "Grocery List",
                fontSize = 18.sp,
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
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DarkAvatarBg),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LightGrayBg)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val headerImageUri = recipes.find { it.title.equals(list.title, ignoreCase = true) }?.imageUri.orEmpty()
            RecipeImageBox(
                imageUri = headerImageUri,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECIPE NOTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberBadgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${list.remainingItems}/${list.totalItems} left",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberBadgeText
                        )
                    }
                }
                Text(
                    text = list.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Organized by store aisle",
                    fontSize = 11.sp,
                    color = DarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INGREDIENTS CHECKLIST",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextSecondary
            )
            Text(
                text = "Tap to check off",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (groupedItems.isEmpty()) {
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
                            text = "No items in this list yet. Add an item below.",
                            fontSize = 13.sp,
                            color = DarkTextSecondary
                        )
                    }
                }
            } else {
                groupedItems.forEach { (categoryName, categoryItems) ->
                    item(key = "header_$categoryName") {
                        Text(
                            text = categoryName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberBadgeText,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    items(categoryItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightGrayBg)
                                .clickable { viewModel.toggleGroceryItemCheck(list.id, item.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { viewModel.toggleGroceryItemCheck(list.id, item.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryYellow,
                                    checkmarkColor = DarkText
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayLabel(isMetric = isMetricUnit),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isChecked) DarkTextSecondary else DarkText,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = item.category,
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Add to section:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aisleSections.forEach { section ->
                        val isSelected = selectedSection == section
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryYellow else LightGrayBg)
                                .clickable { selectedSection = section }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = section,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = DarkText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCustomItem,
                        onValueChange = { newCustomItem = it },
                        singleLine = true,
                        placeholder = { Text("Add to $selectedSection...", fontSize = 13.sp, color = DarkTextSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryYellow)
                            .clickable {
                                if (newCustomItem.isNotBlank()) {
                                    viewModel.addCustomGroceryItem(list.id, newCustomItem, selectedSection)
                                    newCustomItem = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Add to $selectedSection", tint = DarkText)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TipBannerBg)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = AmberBadgeText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (list.remainingItems == 0 && list.totalItems > 0) {
                            "All ${list.totalItems} items checked off and ready to cook."
                        } else {
                            "${list.remainingItems} items remaining across ${groupedItems.size} aisle category(s)."
                        },
                        fontSize = 12.sp,
                        color = TipBannerText
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

package com.example.easycooking.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.model.AlarmSoundPreset
import com.example.easycooking.model.GroceryCategories
import com.example.easycooking.model.ScalingMode
import com.example.easycooking.ui.components.AlarmSoundPickerDialog
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
import com.example.easycooking.viewmodel.CookingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadStep1Screen(
    viewModel: CookingViewModel,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val title by viewModel.newRecipeTitle.collectAsState()
    val servings by viewModel.newRecipeServings.collectAsState()
    val duration by viewModel.newRecipeDuration.collectAsState()
    val category by viewModel.newRecipeCategory.collectAsState()
    val selectedBadges by viewModel.newRecipeDietaryBadges.collectAsState()
    val recipeImageUri by viewModel.newRecipeImageUri.collectAsState()
    var titleError by remember { mutableStateOf<String?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.savePickedRecipeImageFromUri(uri) { ok ->
                photoError = if (ok) null else "Could not load selected image."
            }
        }
    }

    val durations = listOf("< 15 min", "30 min", "45+ min")
    val mealCategories = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Dessert")
    val dietaryList = listOf("Gluten-Free", "Dairy-Free", "Nut-Free", "Vegetarian", "Vegan")

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
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onCancelClick)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "New Recipe Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "Step 1 of 2",
                        fontSize = 11.sp,
                        color = DarkTextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RECIPE FOUNDATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary)
                Text("Smart Scaling Active", fontSize = 11.sp, color = AmberBadgeText)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Recipe Title *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    viewModel.updateNewRecipeTitle(it)
                    titleError = null
                },
                singleLine = true,
                isError = titleError != null,
                placeholder = { Text("e.g., Creamy Garlic Pasta", color = DarkTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
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
            if (titleError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(titleError ?: "", fontSize = 12.sp, color = Color(0xFFB91C1C))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Recipe Photo (Optional)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            if (recipeImageUri.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGrayBg)
                        .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Add Recipe Photo",
                            tint = DarkText,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap to Add Dish Photo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Choose an image from your device gallery",
                            fontSize = 11.sp,
                            color = DarkTextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecipeImageBox(
                        imageUri = recipeImageUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(164.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Change Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                        }
                        OutlinedButton(
                            onClick = { viewModel.clearNewRecipeImage() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Remove", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB91C1C))
                        }
                    }
                }
            }
            if (photoError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(photoError ?: "", fontSize = 12.sp, color = Color(0xFFB91C1C))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightGrayBg)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Base Servings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Text("Base ratio for auto-scaling ingredients", fontSize = 11.sp, color = DarkTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clickable { viewModel.decrementNewRecipeServings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease servings", tint = DarkText, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("$servings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(horizontal = 4.dp))
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clickable { viewModel.incrementNewRecipeServings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(PrimaryYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase servings", tint = DarkText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Total Duration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.forEach { dur ->
                    val isSelected = duration == dur
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .sizeIn(minHeight = 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryYellow else LightGrayBg)
                            .clickable { viewModel.updateNewRecipeDuration(dur) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dur,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = DarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealCategories.forEach { cat ->
                    val isSelected = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) DarkText else LightGrayBg)
                            .clickable { viewModel.updateNewRecipeCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else DarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Dietary Badges", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dietaryList.forEach { badge ->
                    val isSelected = selectedBadges.contains(badge)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PrimaryYellow else LightGrayBg)
                            .clickable { viewModel.toggleNewRecipeDietaryBadge(badge) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = DarkText, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = badge,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = DarkText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryYellowButton(
            text = "Next: Ingredients & Steps",
            onClick = {
                if (title.isBlank()) {
                    titleError = "Recipe title is required."
                } else {
                    onNextClick()
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadStep2Screen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit
) {
    val title by viewModel.newRecipeTitle.collectAsState()
    val servings by viewModel.newRecipeServings.collectAsState()
    val duration by viewModel.newRecipeDuration.collectAsState()
    val ingredients by viewModel.newRecipeIngredients.collectAsState()
    val step1Title by viewModel.newRecipeStep1Title.collectAsState()
    val step1Desc by viewModel.newRecipeStep1Desc.collectAsState()
    val step2Title by viewModel.newRecipeStep2Title.collectAsState()
    val step2Desc by viewModel.newRecipeStep2Desc.collectAsState()
    val recipeImageUri by viewModel.newRecipeImageUri.collectAsState()
    val alarmSoundId by viewModel.alarmSoundId.collectAsState()
    val customAlarmName by viewModel.customAlarmName.collectAsState()
    val isPreviewingAlarm by viewModel.isPreviewingAlarm.collectAsState()

    val aisleSections = GroceryCategories.AISLE_SECTIONS
    val quickUnits = listOf("g", "ml", "tbsp", "tsp", "pcs", "cup", "cloves")
    var selectedSection by remember { mutableStateOf(GroceryCategories.DEFAULT_SECTION) }
    var selectedScalingMode by remember { mutableStateOf(ScalingMode.PROPORTIONAL) }
    var ingredientAmount by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("g") }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientError by remember { mutableStateOf<String?>(null) }
    var showAlarmDialog by remember { mutableStateOf(false) }

    val activeAlarmPreset = AlarmSoundPreset.fromId(alarmSoundId)
    val activeAlarmSummary = if (activeAlarmPreset == AlarmSoundPreset.CUSTOM && customAlarmName.isNotBlank()) {
        "Custom: $customAlarmName"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "New Recipe Instructions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Step 2 of 2",
                    fontSize = 11.sp,
                    color = DarkTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Final details before publishing", fontSize = 11.sp, color = AmberBadgeText)
            Text(
                text = if (ingredients.isNotEmpty()) "Ready" else "Add 1+ ingredient",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GreenSuccessText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(
                    text = "Ingredients (${ingredients.size} items)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (ingredients.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGrayBg)
                            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "No ingredients added yet. Use the form below to add your first ingredient.",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            itemsIndexed(
                ingredients,
                key = { index, item -> "${index}_${item.name}_${item.amount}_${item.unit}_${item.category}" }
            ) { index, item ->
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.DragIndicator, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(item.formattedSummary(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(item.category, fontSize = 11.sp, color = AmberBadgeText)
                                if (item.scalingMode != ScalingMode.PROPORTIONAL) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AmberBadgeBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.scalingMode.shortLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AmberBadgeText
                                        )
                                    }
                                }
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.removeNewRecipeIngredientRow(index) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGrayBg)
                        .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Add Ingredient & Measurement",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("1. Target Category", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
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
                                    .background(if (isSelected) PrimaryYellow else Color.White)
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

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "2. Enter Quantity & Unit *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ingredientAmount,
                            onValueChange = {
                                ingredientAmount = it
                                ingredientError = null
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            label = { Text("Amount (e.g. 200)", fontSize = 11.sp) },
                            placeholder = { Text("200", fontSize = 12.sp, color = DarkTextSecondary) },
                            modifier = Modifier.weight(0.48f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DarkText,
                                unfocusedTextColor = DarkText,
                                cursorColor = DarkText,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PrimaryYellow,
                                unfocusedBorderColor = BorderGray
                            )
                        )
                        OutlinedTextField(
                            value = ingredientUnit,
                            onValueChange = {
                                ingredientUnit = it
                                ingredientError = null
                            },
                            singleLine = true,
                            label = { Text("Unit", fontSize = 11.sp) },
                            placeholder = { Text("g / ml / tbsp", fontSize = 12.sp, color = DarkTextSecondary) },
                            modifier = Modifier.weight(0.52f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DarkText,
                                unfocusedTextColor = DarkText,
                                cursorColor = DarkText,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PrimaryYellow,
                                unfocusedBorderColor = BorderGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickUnits.forEach { unitChip ->
                            val isSelected = ingredientUnit.equals(unitChip, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryYellow else Color.White)
                                    .clickable { ingredientUnit = unitChip }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = unitChip,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = DarkText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "3. Portion Scaling Behavior",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose Cooking Base for cooking oil, butter, broth, or sauces that should not double when servings increase (e.g. 200 ml for 2 servings becomes 250 ml for 3 servings).",
                        fontSize = 11.sp,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ScalingMode.entries.forEach { mode ->
                            val isSelected = selectedScalingMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PrimaryYellow.copy(alpha = 0.25f) else Color.White)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PrimaryYellow else BorderGray,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedScalingMode = mode }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = DarkText
                                    )
                                    Text(
                                        text = mode.description,
                                        fontSize = 10.sp,
                                        color = DarkTextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = DarkText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("4. Ingredient Name *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ingredientName,
                        onValueChange = {
                            ingredientName = it
                            ingredientError = null
                        },
                        singleLine = true,
                        label = { Text("Ingredient name", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Chicken breast, Garlic", fontSize = 12.sp, color = DarkTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkText,
                            unfocusedTextColor = DarkText,
                            cursorColor = DarkText,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryYellow,
                            unfocusedBorderColor = BorderGray
                        )
                    )

                    if (ingredientError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ingredientError ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val added = viewModel.addNewRecipeIngredientRow(
                                name = ingredientName,
                                amountText = ingredientAmount,
                                unit = ingredientUnit,
                                category = selectedSection,
                                scalingMode = selectedScalingMode
                            )
                            if (added) {
                                ingredientAmount = ""
                                ingredientName = ""
                                selectedScalingMode = ScalingMode.PROPORTIONAL
                                ingredientError = null
                            } else {
                                ingredientError = "Enter a valid quantity (> 0) and ingredient name."
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to $selectedSection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = "Instructions (2 steps)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))

                StepEditorCard(
                    stepNumber = "1",
                    titleValue = step1Title,
                    onTitleChange = { viewModel.updateNewRecipeStep1Title(it) },
                    descValue = step1Desc,
                    onDescChange = { viewModel.updateNewRecipeStep1Desc(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                StepEditorCard(
                    stepNumber = "2",
                    titleValue = step2Title,
                    onTitleChange = { viewModel.updateNewRecipeStep2Title(it) },
                    descValue = step2Desc,
                    onDescChange = { viewModel.updateNewRecipeStep2Desc(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightGrayBg)
                        .clickable { showAlarmDialog = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = DarkText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cooking Timer Alarm Sound", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            Text(activeAlarmSummary, fontSize = 11.sp, color = DarkTextSecondary)
                        }
                    }
                    OutlinedButton(
                        onClick = { showAlarmDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Customize", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightGrayBg)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RecipeImageBox(
                                imageUri = recipeImageUri,
                                fallbackColor = Color(0xFFCBD5E1),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title.ifBlank { "Untitled Recipe" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Estimated: $duration | $servings Servings",
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(GreenSuccessBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Ready",
                                tint = GreenSuccessText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (ingredients.isEmpty()) {
            Text(
                text = "Add at least 1 ingredient above before publishing.",
                fontSize = 11.sp,
                color = AmberBadgeText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.publishNewRecipe(asDraft = true) {
                        onPublishSuccess()
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Save Draft", fontWeight = FontWeight.Bold, color = DarkText)
            }

            val canPublish = ingredients.isNotEmpty()
            Button(
                onClick = {
                    viewModel.publishNewRecipe(asDraft = false) {
                        onPublishSuccess()
                    }
                },
                enabled = canPublish,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = DarkText,
                    disabledContainerColor = LightGrayBg,
                    disabledContentColor = DarkTextSecondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Publish",
                    fontWeight = FontWeight.Bold,
                    color = if (canPublish) DarkText else DarkTextSecondary
                )
            }
        }
    }
}

@Composable
private fun StepEditorCard(
    stepNumber: String,
    titleValue: String,
    onTitleChange: (String) -> Unit,
    descValue: String,
    onDescChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightGrayBg)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PrimaryYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stepNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = titleValue,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    placeholder = { Text("Step title", fontSize = 12.sp, color = DarkTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        cursorColor = DarkText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryYellow,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descValue,
                onValueChange = onDescChange,
                placeholder = { Text("Step instructions...", fontSize = 12.sp, color = DarkTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                    cursorColor = DarkText,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryYellow,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

package com.example.easycooking.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.ui.components.PrimaryYellowButton
import com.example.easycooking.ui.theme.BorderGray
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.viewmodel.CookingViewModel

@Composable
fun UploadStep1Screen(
    viewModel: CookingViewModel,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val title by viewModel.newRecipeTitle.collectAsState()
    val servings by viewModel.newRecipeServings.collectAsState()
    val duration by viewModel.newRecipeDuration.collectAsState()

    val durations = listOf("< 15 min", "30 min", "45+ min")
    val dietaryList = listOf("Gluten-Free", "Dairy-Free", "Nut-Free")

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
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.clickable(onClick = onCancelClick)
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
                Spacer(modifier = Modifier.width(36.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RECIPE FOUNDATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary)
                Text("Smart Scaling Active", fontSize = 11.sp, color = Color(0xFFB45309))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Recipe Title *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateNewRecipeTitle(it) },
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
                Column {
                    Text("Base Servings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Text("Base ratio for auto-scaling ingredients", fontSize = 11.sp, color = DarkTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { viewModel.decrementNewRecipeServings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("$servings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimaryYellow)
                            .clickable { viewModel.incrementNewRecipeServings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
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

            Text("Dietary Badges", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dietaryList.forEach { badge ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGrayBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = badge, fontSize = 11.sp, color = DarkText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGrayBg)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = DarkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Cover Photo *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        Text("Tap to snap or select", fontSize = 10.sp, color = DarkTextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGrayBg)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Videocam, contentDescription = null, tint = DarkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Short Video", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        Text("Optional (15-60s)", fontSize = 10.sp, color = DarkTextSecondary)
                    }
                }
            }
        }

        PrimaryYellowButton(
            text = "Next: Ingredients & Steps",
            onClick = onNextClick
        )
    }
}

@Composable
fun UploadStep2Screen(
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit
) {
    val ingredients by viewModel.newRecipeIngredients.collectAsState()
    var newIngredientInput by remember { mutableStateOf("") }

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
            Text("Final details before publishing", fontSize = 11.sp, color = Color(0xFFB45309))
            Text("100% Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients (${ingredients.size} items)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(ingredients) { index, item ->
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DragIndicator, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item, fontSize = 13.sp, color = DarkText)
                    }
                    IconButton(
                        onClick = { viewModel.removeNewRecipeIngredientRow(index) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newIngredientInput,
                        onValueChange = { newIngredientInput = it },
                        placeholder = { Text("Add new ingredient...", fontSize = 12.sp, color = DarkTextSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
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
                    Button(
                        onClick = {
                            if (newIngredientInput.isNotBlank()) {
                                viewModel.addNewRecipeIngredientRow(newIngredientInput)
                                newIngredientInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText)
                    ) {
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sear & Golden Crisp", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            }
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Heat olive oil in a deep skillet over medium-high heat. Sear seasoned chicken breasts 4-5 minutes per side until deeply golden brown.",
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simmer Cream Sauce", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            }
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Stir in minced garlic, sundried tomatoes, and heavy cream. Simmer gently on low heat for 5 minutes until velvet-smooth.",
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            lineHeight = 18.sp
                        )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFCBD5E1))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Tuscan Garlic Skillet",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Estimated: 25 mins | 2 Servings",
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Ready",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Save Draft", fontWeight = FontWeight.Bold, color = DarkText)
            }

            Button(
                onClick = {
                    viewModel.publishNewRecipe {
                        onPublishSuccess()
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DarkText),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Publish", fontWeight = FontWeight.Bold, color = DarkText)
            }
        }
    }
}

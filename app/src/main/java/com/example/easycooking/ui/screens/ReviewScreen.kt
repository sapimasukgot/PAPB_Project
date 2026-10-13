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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
fun WriteReviewScreen(
    recipeId: String,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull() ?: return

    var selectedRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var selectedHighlights by remember { mutableStateOf(setOf("Easy to Follow", "Great Flavor")) }

    val highlightTags = listOf("Easy to Follow", "Accurate Time", "Great Flavor")

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
                    text = "Write A Review",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LightGrayBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFCBD5E1))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = recipe.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "How was your cooking experience?",
                        fontSize = 12.sp,
                        color = DarkTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$star stars",
                            tint = if (star <= selectedRating) StarYellow else Color.LightGray,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { selectedRating = star }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (selectedRating == 5) "Excellent! (5/5)" else "Rating: $selectedRating/5",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = reviewComment,
                onValueChange = { if (it.length <= 250) reviewComment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                placeholder = {
                    Text(
                        text = "Share your cooking tips, ingredient adjustments, or thoughts...",
                        fontSize = 13.sp,
                        color = DarkTextSecondary
                    )
                },
                shape = RoundedCornerShape(14.dp),
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cooking tips help others!",
                    fontSize = 11.sp,
                    color = Color(0xFFB45309)
                )
                Text(
                    text = "${reviewComment.length}/250",
                    fontSize = 11.sp,
                    color = DarkTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Highlight details", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                Text("Tap to toggle", fontSize = 11.sp, color = DarkTextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                highlightTags.forEach { tag ->
                    val isSelected = selectedHighlights.contains(tag)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) DarkText else LightGrayBg)
                            .clickable {
                                selectedHighlights = if (isSelected) {
                                    selectedHighlights - tag
                                } else {
                                    selectedHighlights + tag
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else DarkText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightGrayBg)
                    .clickable { }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Photo (Optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                }
            }
        }

        PrimaryYellowButton(
            text = "Submit Review",
            onClick = {
                val commentToSubmit = if (reviewComment.isBlank()) "Amazing recipe! Perfect taste and easy to make." else reviewComment
                viewModel.addReview(recipe.id, selectedRating, commentToSubmit)
                onSubmitSuccess()
            }
        )
    }
}

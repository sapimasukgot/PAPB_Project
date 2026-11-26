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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow
import com.example.easycooking.viewmodel.CookingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteReviewScreen(
    recipeId: String,
    initialRating: Int = 5,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId } ?: recipes.firstOrNull() ?: return

    var selectedRating by remember(initialRating) { mutableIntStateOf(initialRating.coerceIn(1, 5)) }
    var reviewComment by remember { mutableStateOf("") }
    var commentError by remember { mutableStateOf<String?>(null) }
    var selectedHighlights by remember { mutableStateOf(setOf("Easy to Follow", "Great Flavor")) }
    var isSubmitting by remember { mutableStateOf(false) }

    val highlightTags = listOf("Easy to Follow", "Accurate Time", "Great Flavor")

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
                    text = "Write A Review",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onBackClick)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
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
                        Box(
                            modifier = Modifier
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable { selectedRating = star },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= selectedRating) StarYellow else Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
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
                onValueChange = {
                    if (it.length <= 250) {
                        reviewComment = it
                        commentError = null
                    }
                },
                isError = commentError != null,
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

            if (commentError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = commentError ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFFB91C1C),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cooking tips help others!",
                    fontSize = 11.sp,
                    color = AmberBadgeText
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

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else DarkText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryYellowButton(
            text = "Submit Review",
            enabled = !isSubmitting,
            onClick = {
                if (isSubmitting) return@PrimaryYellowButton
                if (reviewComment.isBlank()) {
                    commentError = "Please write a brief comment before submitting."
                } else {
                    isSubmitting = true
                    viewModel.addReview(recipe.id, selectedRating, reviewComment, selectedHighlights) {
                        onSubmitSuccess()
                    }
                }
            }
        )
    }
}

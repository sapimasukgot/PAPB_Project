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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onDiscoverClick: () -> Unit = {}
) {
    val groceryLists by viewModel.groceryLists.collectAsState()
    val activeLists = groceryLists.filter { !it.isCompleted }
    val completedLists = groceryLists.filter { it.isCompleted }

    val suggestedMeals = listOf(
        Triple("Mushroom Risotto", "6 ingredients", "25 min"),
        Triple("Grilled Salmon", "5 ingredients", "20 min")
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
                    text = "Grocery",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Button(
                    onClick = {
                        val firstList = activeLists.firstOrNull()
                        if (firstList != null) onListClick(firstList.id)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightGrayBg,
                        contentColor = DarkText
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New List", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            text = "8 items to buy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Across 2 active lists",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        val firstList = activeLists.firstOrNull()
                        if (firstList != null) onListClick(firstList.id)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DarkText
                    )
                ) {
                    Text("Start Shopping", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

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

        items(activeLists) { list ->
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
                        .background(PlaceholderImageBg)
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
                IconButton(onClick = { viewModel.deleteGroceryList(list.id) }) {
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

            items(completedLists) { list ->
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
                    IconButton(onClick = { viewModel.deleteGroceryList(list.id) }) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete",
                            tint = DarkTextSecondary
                        )
                    }
                }
            }
        }

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
                    modifier = Modifier.clickable(onClick = onDiscoverClick)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                suggestedMeals.forEach { (name, count, time) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightGrayBg)
                            .padding(12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PlaceholderImageBg)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText, maxLines = 1)
                            Text(count, fontSize = 11.sp, color = DarkTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val first = activeLists.firstOrNull()
                                    if (first != null) {
                                        viewModel.addCustomGroceryItem(first.id, name)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun GroceryDetailScreen(
    listId: String,
    viewModel: CookingViewModel,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val groceryLists by viewModel.groceryLists.collectAsState()
    val list = groceryLists.find { it.id == listId } ?: groceryLists.firstOrNull() ?: return

    var newCustomItem by remember { mutableStateOf("") }

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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkAvatarBg)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LightGrayBg)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PlaceholderImageBg)
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
                    text = "Generated from Recipe Hub",
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
                text = "Toggle Done",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(list.items) { item ->
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
                            text = item.name,
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

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCustomItem,
                        onValueChange = { newCustomItem = it },
                        placeholder = { Text("+ Add custom ingredient...", fontSize = 13.sp, color = DarkTextSecondary) },
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
                                    viewModel.addCustomGroceryItem(list.id, newCustomItem)
                                    newCustomItem = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Add", tint = DarkText)
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
                        text = "Double the cream & garlic for an extra rich pasta sauce base.",
                        fontSize = 12.sp,
                        color = TipBannerText
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

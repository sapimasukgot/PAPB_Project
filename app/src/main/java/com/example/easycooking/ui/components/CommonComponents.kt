package com.example.easycooking.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycooking.R
import com.example.easycooking.model.AlarmSoundPreset
import com.example.easycooking.model.ExportGroceryResult
import com.example.easycooking.model.ExportGroceryStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.example.easycooking.model.Recipe
import com.example.easycooking.navigation.BottomTab
import com.example.easycooking.ui.theme.AmberBadgeBg
import com.example.easycooking.ui.theme.AmberBadgeText
import com.example.easycooking.ui.theme.BorderGray
import com.example.easycooking.ui.theme.DarkAvatarBg
import com.example.easycooking.ui.theme.DarkText
import com.example.easycooking.ui.theme.DarkTextSecondary
import com.example.easycooking.ui.theme.GreenSuccessBg
import com.example.easycooking.ui.theme.GreenSuccessText
import com.example.easycooking.ui.theme.IconGray
import com.example.easycooking.ui.theme.LightGrayBg
import com.example.easycooking.ui.theme.PlaceholderImageBg
import com.example.easycooking.ui.theme.PrimaryYellow
import com.example.easycooking.ui.theme.StarYellow

@Composable
fun AppBottomNavigationBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = BorderGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTabItem(
                    tab = BottomTab.DISCOVER,
                    isSelected = currentTab == BottomTab.DISCOVER,
                    activeIcon = Icons.Filled.Explore,
                    inactiveIcon = Icons.Outlined.Explore,
                    onClick = { onTabSelected(BottomTab.DISCOVER) }
                )
                BottomTabItem(
                    tab = BottomTab.SAVED,
                    isSelected = currentTab == BottomTab.SAVED,
                    activeIcon = Icons.Filled.Bookmark,
                    inactiveIcon = Icons.Outlined.BookmarkBorder,
                    onClick = { onTabSelected(BottomTab.SAVED) }
                )
                BottomTabItem(
                    tab = BottomTab.GROCERY,
                    isSelected = currentTab == BottomTab.GROCERY,
                    activeIcon = Icons.Filled.ShoppingBag,
                    inactiveIcon = Icons.Outlined.ShoppingBag,
                    onClick = { onTabSelected(BottomTab.GROCERY) }
                )
                BottomTabItem(
                    tab = BottomTab.ACCOUNT,
                    isSelected = currentTab == BottomTab.ACCOUNT,
                    activeIcon = Icons.Filled.Person,
                    inactiveIcon = Icons.Outlined.Person,
                    onClick = { onTabSelected(BottomTab.ACCOUNT) }
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    tab: BottomTab,
    isSelected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    onClick: () -> Unit
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryYellow else Color.Transparent,
        label = "tab_pill_bg"
    )
    val textColor = if (isSelected) DarkText else IconGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(pillBgColor)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) activeIcon else inactiveIcon,
                contentDescription = tab.title,
                tint = if (isSelected) DarkText else IconGray,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun PrimaryYellowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showArrow: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryYellow,
            contentColor = DarkText,
            disabledContainerColor = LightGrayBg,
            disabledContentColor = DarkTextSecondary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            if (showArrow) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = DarkText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkText
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            actions?.invoke()
            if (onProfileClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkAvatarBg)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCardGrid(
    recipe: Recipe,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightGrayBg)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 12.dp)
    ) {
        Column {
            RecipeImageBox(
                imageUri = recipe.imageUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = recipe.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(onClick = onBookmarkToggle)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (recipe.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (recipe.isBookmarked) PrimaryYellow else IconGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = recipe.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.durationMinutes}m | ${recipe.difficulty}",
                    fontSize = 12.sp,
                    color = DarkTextSecondary
                )
            }
        }
    }
}

private fun resolveBundledRecipeDrawable(imageUri: String): Int? {
    return when (imageUri) {
        "res:recipe_1" -> R.drawable.recipe_1
        "res:recipe_2" -> R.drawable.recipe_2
        "res:recipe_3" -> R.drawable.recipe_3
        "res:recipe_4" -> R.drawable.recipe_4
        "res:recipe_5" -> R.drawable.recipe_5
        "res:recipe_6" -> R.drawable.recipe_6
        "res:recipe_7" -> R.drawable.recipe_7
        "res:recipe_8" -> R.drawable.recipe_8
        "res:recipe_9" -> R.drawable.recipe_9
        "res:recipe_10" -> R.drawable.recipe_10
        "res:recipe_11" -> R.drawable.recipe_11
        "res:recipe_12" -> R.drawable.recipe_12
        "res:recipe_13" -> R.drawable.recipe_13
        else -> null
    }
}

@Composable
fun RecipeImageBox(
    imageUri: String,
    modifier: Modifier = Modifier,
    fallbackColor: Color = PlaceholderImageBg,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val bundledDrawableRes = remember(imageUri) { resolveBundledRecipeDrawable(imageUri) }
    val loadedBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = if (imageUri.isBlank() || bundledDrawableRes != null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(imageUri)
                    if (!file.exists()) return@runCatching null
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, bounds)
                    var sampleSize = 1
                    val maxDim = 900
                    while (bounds.outWidth / sampleSize > maxDim || bounds.outHeight / sampleSize > maxDim) {
                        sampleSize *= 2
                    }
                    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeFile(file.absolutePath, decodeOpts)?.asImageBitmap()
                }.getOrNull()
            }
        }
    }

    Box(modifier = modifier.background(fallbackColor)) {
        when {
            bundledDrawableRes != null -> {
                Image(
                    painter = painterResource(id = bundledDrawableRes),
                    contentDescription = "Recipe photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            loadedBitmap != null -> {
                Image(
                    bitmap = loadedBitmap!!,
                    contentDescription = "Recipe photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        content()
    }
}

@Composable
fun AlarmSoundPickerDialog(
    selectedSoundId: String,
    customAudioName: String,
    isPreviewing: Boolean,
    onSelectPreset: (String) -> Unit,
    onPickCustomAudio: (android.net.Uri, (Boolean, String) -> Unit) -> Unit,
    onTogglePreview: () -> Unit,
    onStopPreview: () -> Unit,
    onDismiss: () -> Unit
) {
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onPickCustomAudio(uri) { ok, detail ->
                statusMessage = if (ok) {
                    "Selected custom sound: $detail"
                } else {
                    detail
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onStopPreview()
        }
    }

    AlertDialog(
        onDismissRequest = {
            onStopPreview()
            onDismiss()
        },
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = DarkText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Timer Alarm Sound", fontWeight = FontWeight.Bold, color = DarkText)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlarmSoundPreset.entries.forEach { preset ->
                    val isSelected = selectedSoundId == preset.id
                    val subtitleText = if (preset == AlarmSoundPreset.CUSTOM && customAudioName.isNotBlank()) {
                        "File: $customAudioName"
                    } else {
                        preset.description
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryYellow.copy(alpha = 0.25f) else LightGrayBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PrimaryYellow else BorderGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (preset == AlarmSoundPreset.CUSTOM && customAudioName.isBlank()) {
                                    audioPickerLauncher.launch("audio/*")
                                } else {
                                    onSelectPreset(preset.id)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) DarkText else IconGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Text(
                                text = subtitleText,
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                        if (preset == AlarmSoundPreset.CUSTOM) {
                            TextButton(
                                onClick = { audioPickerLauncher.launch("audio/*") }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = DarkText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Choose", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            }
                        }
                    }
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage ?: "",
                        fontSize = 11.sp,
                        color = GreenSuccessText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onTogglePreview,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = DarkText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPreviewing) "Stop Sound Preview" else "Preview Selected Sound",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStopPreview()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = DarkText
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ExportGroceryResultDialog(
    result: ExportGroceryResult,
    onDismiss: () -> Unit,
    onOpenList: ((String) -> Unit)? = null
) {
    val badgeBg = when (result.status) {
        ExportGroceryStatus.SUCCESS -> GreenSuccessBg
        ExportGroceryStatus.ALREADY_ACTIVE -> AmberBadgeBg
        ExportGroceryStatus.FAILED -> Color(0xFFFEE2E2)
    }
    val badgeTint = when (result.status) {
        ExportGroceryStatus.SUCCESS -> GreenSuccessText
        ExportGroceryStatus.ALREADY_ACTIVE -> AmberBadgeText
        ExportGroceryStatus.FAILED -> Color(0xFFB91C1C)
    }
    val statusIcon = when (result.status) {
        ExportGroceryStatus.SUCCESS -> Icons.Filled.CheckCircle
        ExportGroceryStatus.ALREADY_ACTIVE -> Icons.Filled.Info
        ExportGroceryStatus.FAILED -> Icons.Filled.ErrorOutline
    }
    val titleText = when (result.status) {
        ExportGroceryStatus.SUCCESS -> "Exported to Grocery List"
        ExportGroceryStatus.ALREADY_ACTIVE -> "Recipe Already Active"
        ExportGroceryStatus.FAILED -> "Export Failed"
    }
    val canOpenList = result.listId != null && onOpenList != null &&
        (result.status == ExportGroceryStatus.SUCCESS || result.status == ExportGroceryStatus.ALREADY_ACTIVE)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        },
        text = {
            Text(
                text = result.message,
                fontSize = 13.sp,
                color = DarkTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = DarkText
                )
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (canOpenList) {
            {
                TextButton(
                    onClick = {
                        val targetId = result.listId!!
                        onDismiss()
                        onOpenList!!.invoke(targetId)
                    }
                ) {
                    Text("View List", fontWeight = FontWeight.SemiBold, color = DarkText)
                }
            }
        } else {
            null
        }
    )
}

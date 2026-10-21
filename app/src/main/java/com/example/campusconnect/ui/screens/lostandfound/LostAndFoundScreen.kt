package com.example.campusconnect.ui.screens.lostandfound

import android.app.Application
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.campusconnect.data.local.entity.LostFoundItemEntity
import com.example.campusconnect.ui.components.*
import com.example.campusconnect.ui.theme.*
import com.example.campusconnect.viewmodel.LostFoundViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LostAndFoundScreen(
    onNavigateToReport: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: LostFoundViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LostFoundViewModel(app) as T
    })

    val items by viewModel.items.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val totalLost by viewModel.totalLost.collectAsStateWithLifecycle()
    val totalFound by viewModel.totalFound.collectAsStateWithLifecycle()
    val resolved by viewModel.resolved.collectAsStateWithLifecycle()

    var itemToClaim by remember { mutableStateOf<LostFoundItemEntity?>(null) }
    var claimAnswerText by remember { mutableStateOf("") }
    var claimErrorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            // Dual FABs for Report Lost and Report Found
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { onNavigateToReport("Lost") },
                    containerColor = ErrorColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.SearchOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Report Lost", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                FloatingActionButton(
                    onClick = { onNavigateToReport("Found") },
                    containerColor = Emerald,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Report Found", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    GradientHeader(
                        title = "Lost & Found",
                        subtitle = "Help fellow students locate and reclaim lost campus items",
                        icon = Icons.Rounded.FindInPage,
                        gradientColors = listOf(LostFoundColor, Color(0xFFFFA726), WarningContainer)
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 12.dp, top = 8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label = "Total Lost",
                        value = "$totalLost",
                        icon = Icons.Rounded.SearchOff,
                        color = ErrorColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Total Found",
                        value = "$totalFound",
                        icon = Icons.Rounded.SavedSearch,
                        color = Emerald,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Resolved",
                        value = "$resolved",
                        icon = Icons.Rounded.Verified,
                        color = Sapphire,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Row: Lost Items vs Found Items vs All
            item {
                val tabTypes = listOf("All", "Lost", "Found")
                PrimaryTabRow(
                    selectedTabIndex = tabTypes.indexOf(selectedType).coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WarningColor,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    tabTypes.forEach { type ->
                        Tab(
                            selected = selectedType == type,
                            onClick = { viewModel.setType(type) },
                            text = {
                                Text(
                                    text = when (type) {
                                        "Lost" -> "Lost Items"
                                        "Found" -> "Found Items"
                                        else -> "All Items"
                                    },
                                    fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Item Cards
            if (items.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Rounded.CheckCircleOutline,
                        title = "No ${if (selectedType == "All") "" else selectedType} Items",
                        subtitle = "Everything seems to be accounted for! Use the buttons below to report an item.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    LostFoundCard(
                        item = item,
                        onClaimClick = {
                            itemToClaim = item
                            claimAnswerText = ""
                            claimErrorText = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Security Verification / Claim Dialog
    itemToClaim?.let { item ->
        val isFoundItem = item.type.equals("Found", ignoreCase = true)
        val hasSecurityQ = item.securityQuestion.isNotBlank()

        AlertDialog(
            onDismissRequest = { itemToClaim = null },
            icon = {
                Icon(
                    imageVector = if (isFoundItem) Icons.Rounded.Security else Icons.Rounded.HelpOutline,
                    contentDescription = null,
                    tint = if (isFoundItem) Emerald else Sapphire,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isFoundItem) "Verify Ownership" else "Found this Item?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Item: ${item.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (hasSecurityQ && isFoundItem) {
                        Text(
                            text = "To prevent wrongful claims, please answer the finder's security question:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Q: ${item.securityQuestion}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = claimAnswerText,
                            onValueChange = {
                                claimAnswerText = it
                                claimErrorText = null
                            },
                            placeholder = { Text("Your answer...") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = claimErrorText != null,
                            supportingText = claimErrorText?.let { { Text(it, color = ErrorColor) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = if (isFoundItem)
                                "Confirm that you are the rightful owner of this item. This will mark the listing as resolved."
                            else
                                "Did you locate this item? Marking this will notify ${item.reporterName} and resolve the listing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hasSecurityQ && isFoundItem) {
                            if (item.securityAnswer.isNotBlank() &&
                                !claimAnswerText.trim().equals(item.securityAnswer.trim(), ignoreCase = true)
                            ) {
                                claimErrorText = "Incorrect answer. Please check details."
                                return@Button
                            }
                        }
                        viewModel.resolveItem(item.id)
                        itemToClaim = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFoundItem) Emerald else Sapphire
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isFoundItem) "Confirm & Claim" else "Mark Resolved", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToClaim = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LostFoundCard(
    item: LostFoundItemEntity,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLost = item.type.equals("Lost", ignoreCase = true)
    val typeColor = if (isLost) ErrorColor else Emerald
    val typeBg = if (isLost) ErrorContainer else EmeraldSurface
    val typeIcon = if (isLost) Icons.Rounded.SearchOff else Icons.Rounded.CheckCircle

    LuxuryCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Type Badge + Resolved Badge + TimeAgo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = typeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = item.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = typeColor
                            )
                        }
                    }

                    if (item.isResolved) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = SuccessColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "RESOLVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessColor
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val shareText = "[CampusConnect - ${item.type.uppercase()}]\nItem: ${item.title}\nLocation: ${item.location}\nDetails: ${item.description}\nReported by: ${item.reporterName}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share item"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    TimeAgo(timestamp = item.createdAt)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Icon Placeholder + Title/Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(typeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageUrl.isNotBlank()) {
                        GlideImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (isLost) Icons.Rounded.HelpCenter else Icons.Rounded.ShoppingBag,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Location Tag & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (item.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (!item.isResolved) {
                    Button(
                        onClick = onClaimClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLost) Sapphire else Emerald
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLost) Icons.Rounded.Visibility else Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLost) "I Found This" else "Claim Item",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

package com.example.campusconnect.ui.screens.marketplace

import android.app.Application
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.campusconnect.data.local.entity.MarketplaceItemEntity
import com.example.campusconnect.ui.components.*
import com.example.campusconnect.ui.theme.*
import com.example.campusconnect.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun MarketplaceScreen(
    onNavigateToCreateListing: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: MarketplaceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MarketplaceViewModel(app) as T
    })

    val items by viewModel.items.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showMyListings by viewModel.showMyListings.collectAsStateWithLifecycle()

    var selectedItemForDetail by remember { mutableStateOf<MarketplaceItemEntity?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("All", "Textbooks", "Electronics", "Lab Equipment", "Cycles", "Stationery", "Other")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateListing,
                containerColor = MarketplaceColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.AddShoppingCart, contentDescription = null) },
                text = { Text("Sell Item", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Header Item spanning both columns
            item(span = { GridItemSpan(2) }) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    GradientHeader(
                        title = "Marketplace",
                        subtitle = "Student-only campus buy, sell & trade hub",
                        icon = Icons.Rounded.Storefront,
                        gradientColors = listOf(MarketplaceColor, GoldLight, GoldSurface)
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

            // Search Bar
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    SearchBarComponent(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        placeholder = "Search textbooks, cycles, electronics..."
                    )
                }
            }

            // Listing Toggle (Available vs My Listings)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = !showMyListings,
                            onClick = { if (showMyListings) viewModel.toggleMyListings() },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { SegmentedButtonDefaults.Icon(active = !showMyListings) },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MarketplaceColor.copy(alpha = 0.15f),
                                activeContentColor = MarketplaceColor
                            )
                        ) {
                            Text("Available Market", fontWeight = FontWeight.SemiBold)
                        }
                        SegmentedButton(
                            selected = showMyListings,
                            onClick = { if (!showMyListings) viewModel.toggleMyListings() },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { SegmentedButtonDefaults.Icon(active = showMyListings) },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MarketplaceColor.copy(alpha = 0.15f),
                                activeContentColor = MarketplaceColor
                            )
                        ) {
                            Text("My Listings", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Categories Filter Row
            item(span = { GridItemSpan(2) }) {
                ChipRow(
                    items = categories,
                    selectedItem = selectedCategory,
                    onSelect = { viewModel.setCategory(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // Empty State
            if (items.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyStateView(
                        icon = Icons.Rounded.ProductionQuantityLimits,
                        title = if (showMyListings) "No Listings Posted" else "No Items Available",
                        subtitle = if (showMyListings)
                            "Tap 'Sell Item' below to list textbooks, electronics or campus essentials."
                        else
                            "Try searching for something else or explore another category.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    MarketplaceItemCard(
                        item = item,
                        onClick = { selectedItemForDetail = item },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

    // Detail Bottom Sheet
    selectedItemForDetail?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedItemForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            ItemDetailSheet(
                item = item,
                onMarkAsSold = {
                    viewModel.markAsSold(item.id)
                    selectedItemForDetail = null
                },
                onDelete = {
                    viewModel.deleteListing(item)
                    selectedItemForDetail = null
                },
                onDismiss = { selectedItemForDetail = null }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun MarketplaceItemCard(
    item: MarketplaceItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = getMarketplaceIcon(item.category)
    val categoryBgColor = getCategoryColor(item.category)

    LuxuryCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image Box with SOLD overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(categoryBgColor.copy(alpha = 0.12f)),
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
                        imageVector = categoryIcon,
                        contentDescription = item.category,
                        tint = categoryBgColor,
                        modifier = Modifier.size(54.dp)
                    )
                }

                // Condition Badge Top-Start
                ConditionBadge(
                    condition = item.condition,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                // SOLD Overlay
                if (item.isSold) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = ErrorColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SOLD",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Card Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isSold) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "₹${item.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isSold) MaterialTheme.colorScheme.onSurfaceVariant else RichGold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.sellerName.ifBlank { "Student" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    TimeAgo(timestamp = item.createdAt)
                }
            }
        }
    }
}

@Composable
private fun ConditionBadge(
    condition: String,
    modifier: Modifier = Modifier
) {
    val (badgeColor, containerColor) = when (condition.lowercase()) {
        "new" -> Emerald to EmeraldSurface
        "like new" -> Sapphire to SapphireSurface
        "good" -> RichGold to GoldSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor
    ) {
        Text(
            text = condition,
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ItemDetailSheet(
    item: MarketplaceItemEntity,
    onMarkAsSold: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val categoryIcon = getMarketplaceIcon(item.category)
    val categoryColor = getCategoryColor(item.category)
    var showContactInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = categoryColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = item.category,
                    color = categoryColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val context = LocalContext.current
                IconButton(onClick = {
                    val shareText = "Check out this item on CampusConnect Marketplace!\nItem: ${item.title}\nPrice: ₹${item.price.toInt()}\nCondition: ${item.condition}\nSeller: ${item.sellerName}\nDescription: ${item.description}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share listing"))
                }) {
                    Icon(Icons.Rounded.Share, contentDescription = "Share listing", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Graphic / Image Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(categoryColor.copy(alpha = 0.12f)),
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
                    imageVector = categoryIcon,
                    contentDescription = item.category,
                    tint = categoryColor,
                    modifier = Modifier.size(72.dp)
                )
            }
            if (item.isSold) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SOLD OUT",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "₹${item.price.toInt()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = RichGold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConditionBadge(condition = item.condition)
            Text(
                text = "• Listed by ${item.sellerName.ifBlank { "Student" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = "•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TimeAgo(timestamp = item.createdAt)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Description",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.description.ifBlank { "No detailed description provided." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (showContactInfo) {
            LuxuryCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.ContactMail, contentDescription = null, tint = Sapphire)
                    Column {
                        Text(
                            text = "Connect with ${item.sellerName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Campus Chat or in-person pickup available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!item.isSold) {
                Button(
                    onClick = { showContactInfo = !showContactInfo },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Sapphire),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact Seller", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMarkAsSold,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Sold", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove Listing", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getMarketplaceIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "textbooks" -> Icons.Rounded.MenuBook
        "electronics" -> Icons.Rounded.Devices
        "lab equipment" -> Icons.Rounded.Biotech
        "cycles" -> Icons.Rounded.DirectionsBike
        "stationery" -> Icons.Rounded.Create
        else -> Icons.Rounded.ShoppingBag
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "textbooks" -> Sapphire
        "electronics" -> LavenderAccent
        "lab equipment" -> Emerald
        "cycles" -> Coral
        "stationery" -> WarningColor
        else -> RichGold
    }
}

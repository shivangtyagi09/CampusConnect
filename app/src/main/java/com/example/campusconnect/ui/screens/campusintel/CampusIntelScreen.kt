package com.example.campusconnect.ui.screens.campusintel

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.data.local.dao.CategoryStat
import com.example.campusconnect.data.local.entity.CampusFeedbackEntity
import com.example.campusconnect.ui.components.*
import com.example.campusconnect.ui.theme.*
import com.example.campusconnect.viewmodel.CampusIntelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusIntelScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: CampusIntelViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CampusIntelViewModel(app) as T
    })

    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var showSubmitSheet by remember { mutableStateOf(false) }

    val filterCategories = listOf("All", "WiFi", "Classroom", "Lab", "Library", "Canteen", "Cleanliness", "Other")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSubmitSheet = true },
                containerColor = CampusIntelColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.AddComment, contentDescription = null) },
                text = { Text("Report Intel", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    GradientHeader(
                        title = "Campus Intel",
                        subtitle = "Anonymous facility feedback & real-time campus pulse",
                        icon = Icons.Rounded.Insights,
                        gradientColors = listOf(CampusIntelColor, LavenderAccent, LavenderSurface)
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

            // Facility Pulse Grid Overview
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Facility Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Anonymous Ratings",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FacilityOverviewGrid(categoryStats = categoryStats)
                }
            }

            // Category Filter Row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Recent Feedback Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChipRow(
                        items = filterCategories,
                        selectedItem = selectedCategory,
                        onSelect = { viewModel.setCategory(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Feedback Items List
            if (feedbackList.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Rounded.RateReview,
                        title = "No Feedback Yet",
                        subtitle = "Be the first to share anonymous intelligence about $selectedCategory facilities!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                items(feedbackList, key = { it.id }) { feedback ->
                    AnonymousFeedbackCard(
                        feedback = feedback,
                        onUpvote = { viewModel.upvoteFeedback(feedback.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (showSubmitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSubmitSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            SubmitFeedbackSheet(
                onSubmit = { category, rating, comment, location ->
                    viewModel.submitFeedback(category, rating, comment, location)
                    showSubmitSheet = false
                },
                onDismiss = { showSubmitSheet = false }
            )
        }
    }
}

@Composable
private fun FacilityOverviewGrid(
    categoryStats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    val predefinedCategories = listOf(
        "WiFi" to Icons.Rounded.Wifi,
        "Classroom" to Icons.Rounded.MeetingRoom,
        "Lab" to Icons.Rounded.Science,
        "Library" to Icons.Rounded.LocalLibrary,
        "Canteen" to Icons.Rounded.Restaurant,
        "Cleanliness" to Icons.Rounded.CleaningServices
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (i in predefinedCategories.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val first = predefinedCategories[i]
                val firstStat = categoryStats.firstOrNull { it.category.equals(first.first, ignoreCase = true) }
                FacilityStatTile(
                    name = first.first,
                    icon = first.second,
                    avgRating = firstStat?.avgRating ?: 0f,
                    count = firstStat?.count ?: 0,
                    modifier = Modifier.weight(1f)
                )

                if (i + 1 < predefinedCategories.size) {
                    val second = predefinedCategories[i + 1]
                    val secondStat = categoryStats.firstOrNull { it.category.equals(second.first, ignoreCase = true) }
                    FacilityStatTile(
                        name = second.first,
                        icon = second.second,
                        avgRating = secondStat?.avgRating ?: 0f,
                        count = secondStat?.count ?: 0,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FacilityStatTile(
    name: String,
    icon: ImageVector,
    avgRating: Float,
    count: Int,
    modifier: Modifier = Modifier
) {
    val ratingColor = when {
        count == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        avgRating >= 4.0f -> Emerald
        avgRating >= 3.0f -> WarningColor
        else -> ErrorColor
    }

    LuxuryCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CampusIntelColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = CampusIntelColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ratingColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = ratingColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (count > 0) String.format("%.1f", avgRating) else "N/A",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ratingColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "$count reviews",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnonymousFeedbackCard(
    feedback: CampusFeedbackEntity,
    onUpvote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (feedback.category.lowercase()) {
        "wifi" -> Icons.Rounded.Wifi
        "classroom" -> Icons.Rounded.MeetingRoom
        "lab" -> Icons.Rounded.Science
        "library" -> Icons.Rounded.LocalLibrary
        "canteen" -> Icons.Rounded.Restaurant
        "cleanliness" -> Icons.Rounded.CleaningServices
        else -> Icons.Rounded.Sensors
    }

    LuxuryCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header Row: Category Badge + Star Rating + TimeAgo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CampusIntelColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = CampusIntelColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = feedback.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CampusIntelColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StarRatingDisplay(rating = feedback.rating)
                    TimeAgo(timestamp = feedback.createdAt)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feedback Text
            Text(
                text = feedback.feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Location Tag & Anonymous Upvote Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (feedback.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = feedback.location,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Anonymous Student",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onUpvote,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CampusIntelColor.copy(alpha = 0.1f),
                        contentColor = CampusIntelColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = "Upvote",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${feedback.upvotes}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val rounded = rating.coerceIn(0f, 5f)
        for (i in 1..5) {
            val filled = i <= rounded
            Icon(
                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (filled) RichGold else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitFeedbackSheet(
    onSubmit: (category: String, rating: Float, comment: String, location: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("WiFi") }
    var rating by remember { mutableFloatStateOf(4f) }
    var feedbackText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val categories = listOf("WiFi", "Classroom", "Lab", "Library", "Canteen", "Cleanliness", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Submit Intel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "100% anonymous facility review",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category Selector
        Text(
            text = "Facility Category",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Star Rating Selector
        Text(
            text = "Rate Facility Condition",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                IconButton(
                    onClick = { rating = i.toFloat() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = "Rating $i",
                        tint = if (i <= rating) RichGold else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Location Field
        Text(
            text = "Location (Optional)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = locationText,
            onValueChange = { locationText = it },
            placeholder = { Text("e.g. Block C 3rd Floor, Central Library") },
            leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = CampusIntelColor) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Feedback Comments
        Text(
            text = "Intel Feedback",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            placeholder = { Text("Describe the current condition, issues, or compliments...") },
            shape = RoundedCornerShape(14.dp),
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (feedbackText.isNotBlank()) {
                    onSubmit(selectedCategory, rating, feedbackText, locationText)
                }
            },
            enabled = feedbackText.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CampusIntelColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Rounded.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Submit Anonymously",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

package com.example.campusconnect.ui.screens.assignments

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.data.local.entity.AssignmentEntity
import com.example.campusconnect.ui.components.*
import com.example.campusconnect.ui.theme.*
import com.example.campusconnect.viewmodel.AssignmentViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(
    onNavigateToAcademic: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: AssignmentViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AssignmentViewModel(app) as T
    })

    val assignments by viewModel.filteredAssignments.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val overdueCount by viewModel.overdueCount.collectAsState()

    val selectedType by viewModel.selectedType.collectAsState()
    val showCompleted by viewModel.showCompleted.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val typeFilters = listOf("All", "Assignment", "Project", "Exam", "Registration", "Internship", "Competition")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = AssignmentColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Task",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GradientHeader(
                title = "Assignments",
                subtitle = "Track deadlines & academic progress",
                gradientColors = listOf(AssignmentColor, Coral),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = onNavigateToAcademic,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "Academic Companion",
                            tint = Color.White
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Academic Companion Quick Banner
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToAcademic() },
                        color = CoralSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Coral.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Coral.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Coral,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Smart Academic Companion",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Calculate required TEE exam scores & track target grades",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Coral
                            )
                        }
                    }
                }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Total",
                            value = totalCount.toString(),
                            icon = Icons.Rounded.ListAlt,
                            accentColor = Sapphire,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pending",
                            value = pendingCount.toString(),
                            icon = Icons.Rounded.PendingActions,
                            accentColor = WarningColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Done",
                            value = completedCount.toString(),
                            icon = Icons.Rounded.CheckCircle,
                            accentColor = SuccessColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Overdue",
                            value = overdueCount.toString(),
                            icon = Icons.Rounded.Alarm,
                            accentColor = ErrorColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Tab Switcher: Upcoming | Completed
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabButton(
                            text = "Upcoming ($pendingCount)",
                            isSelected = !showCompleted,
                            onClick = { viewModel.setShowCompleted(false) },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            text = "Completed ($completedCount)",
                            isSelected = showCompleted,
                            onClick = { viewModel.setShowCompleted(true) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filter & Sort Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ChipRow(
                                items = typeFilters,
                                selectedItem = selectedType,
                                onItemSelected = { viewModel.setTypeFilter(it) }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sort,
                                    contentDescription = "Sort",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                listOf("Due Date", "Priority", "Subject").forEach { sortOption ->
                                    DropdownMenuItem(
                                        text = { Text(sortOption) },
                                        onClick = {
                                            viewModel.setSortBy(sortOption)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            if (sortBy == sortOption) {
                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = AssignmentColor)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (assignments.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = if (showCompleted) Icons.Rounded.TaskAlt else Icons.Rounded.AssignmentTurnedIn,
                            title = if (showCompleted) "No completed assignments" else "All caught up!",
                            message = if (showCompleted) "Tasks marked as completed will appear here." else "You have no upcoming assignments. Relax or add a new one!",
                            actionText = if (!showCompleted) "Add Assignment" else null,
                            onActionClick = { showAddSheet = true }
                        )
                    }
                } else {
                    items(assignments, key = { it.id }) { assignment ->
                        AssignmentCard(
                            assignment = assignment,
                            onToggleComplete = { viewModel.toggleCompleted(assignment.id, !assignment.isCompleted) },
                            onDelete = { viewModel.deleteAssignment(assignment) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddAssignmentModalSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { title, subject, description, dueDate, type, priority ->
                viewModel.addAssignment(
                    title = title,
                    subject = subject,
                    description = description,
                    dueDate = dueDate,
                    type = type,
                    priority = priority
                )
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) AssignmentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val diffMillis = assignment.dueDate - now
    val daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val isOverdue = !assignment.isCompleted && diffMillis < 0

    val dueText = when {
        assignment.isCompleted -> "Completed"
        isOverdue -> "Overdue by ${-daysLeft}d"
        daysLeft == 0L -> "Due today!"
        daysLeft == 1L -> "Due tomorrow"
        else -> "$daysLeft days left"
    }

    val dueColor = when {
        assignment.isCompleted -> SuccessColor
        isOverdue -> ErrorColor
        daysLeft <= 2L -> WarningColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LuxuryCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = assignment.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessColor,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = assignment.subject,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AssignmentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                if (assignment.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = assignment.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Type Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getTypeIcon(assignment.type),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = assignment.type,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        PriorityBadge(priority = assignment.priority)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = dueColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dueText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = dueColor
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun getTypeIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "project" -> Icons.Rounded.Engineering
        "exam" -> Icons.Rounded.MenuBook
        "registration" -> Icons.Rounded.AppRegistration
        "internship" -> Icons.Rounded.Work
        "competition" -> Icons.Rounded.EmojiEvents
        else -> Icons.Rounded.Assignment
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentModalSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String, subject: String, description: String, dueDate: Long, type: String, priority: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Assignment") }
    var priority by remember { mutableStateOf(1) } // 0=Low, 1=Medium, 2=High
    var daysFromNow by remember { mutableStateOf(3) }

    val typeOptions = listOf("Assignment", "Project", "Exam", "Registration", "Internship", "Competition")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "New Assignment / Deadline",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject / Course") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Type Selector
            Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            ChipRow(
                items = typeOptions,
                selectedItem = type,
                onItemSelected = { type = it }
            )

            // Priority Selector
            Text("Priority", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0 to "Low", 1 to "Medium", 2 to "High").forEach { (level, label) ->
                    val isSelected = priority == level
                    val pColor = when (level) {
                        2 -> ErrorColor
                        1 -> WarningColor
                        else -> SuccessColor
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { priority = level },
                        color = if (isSelected) pColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) pColor else Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Days left selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Due in $daysFromNow days (${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + daysFromNow * 86400000L))})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (daysFromNow > 1) daysFromNow-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Decrease")
                    }
                    IconButton(
                        onClick = { if (daysFromNow < 60) daysFromNow++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && subject.isNotBlank()) {
                        val dueMillis = System.currentTimeMillis() + (daysFromNow * 86400000L)
                        onAdd(title.trim(), subject.trim(), description.trim(), dueMillis, type, priority)
                    }
                },
                enabled = title.isNotBlank() && subject.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AssignmentColor),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Save Assignment", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

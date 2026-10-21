package com.example.campusconnect.ui.screens.teambuilder

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.data.local.entity.TeamRequestEntity
import com.example.campusconnect.ui.components.*
import com.example.campusconnect.ui.theme.*
import com.example.campusconnect.viewmodel.TeamBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBuilderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: TeamBuilderViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TeamBuilderViewModel(app) as T
    })

    val requests by viewModel.filteredRequests.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    var showCreateSheet by remember { mutableStateOf(false) }

    val typeOptions = listOf("All", "Mini Project", "Final Year", "Hackathon", "Presentation", "Competition")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = TeamBuilderColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GroupAdd,
                        contentDescription = "New Team",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Post Request",
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
                title = "Team Builder",
                subtitle = "Find your perfect team for projects & competitions",
                gradientColors = listOf(TeamBuilderColor, EmeraldLight),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SearchBarComponent(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    placeholder = "Search skills, project names, or departments..."
                )

                Spacer(modifier = Modifier.height(12.dp))

                ChipRow(
                    items = typeOptions,
                    selectedItem = selectedType,
                    onItemSelected = { viewModel.setTypeFilter(it) }
                )
            }

            if (requests.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Rounded.Groups,
                    title = "No team requests found",
                    message = "Assemble your dream team! Be the first to create a project or competition team request.",
                    actionText = "Post Team Request",
                    onActionClick = { showCreateSheet = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(requests, key = { it.id }) { req ->
                        TeamRequestCard(
                            request = req,
                            onApply = { viewModel.applyToRequest(req.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateTeamRequestSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { title, desc, skills, dept, year, spots, type ->
                viewModel.createRequest(
                    projectTitle = title,
                    description = desc,
                    skills = skills,
                    department = dept,
                    year = year,
                    spots = spots,
                    type = type
                )
                showCreateSheet = false
            }
        )
    }
}

@Composable
fun TeamRequestCard(
    request: TeamRequestEntity,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var applied by remember { mutableStateOf(false) }

    val skillsList = remember(request.requiredSkills) {
        request.requiredSkills.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    val isFull = request.filledSpots >= request.totalSpots
    val progress = (request.filledSpots.toFloat() / request.totalSpots.toFloat()).coerceIn(0f, 1f)

    LuxuryCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.projectTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TeamBuilderColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = request.type,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TeamBuilderColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (request.department.isNotBlank()) {
                        Text(
                            text = "${request.department}${if (request.year > 0) " • Year ${request.year}" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isFull) WarningColor.copy(alpha = 0.12f) else SuccessColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = if (isFull) "Team Full" else "${request.totalSpots - request.filledSpots} spots left",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isFull) WarningColor else SuccessColor
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (request.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (skillsList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(skillsList) { skill ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spots Progress
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Team Size",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "${request.filledSpots}/${request.totalSpots} members",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (isFull) WarningColor else TeamBuilderColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Creator and Apply Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = EmeraldSurface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = request.userName.ifBlank { "T" }.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Emerald
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = request.userName.ifBlank { "Project Lead" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    TimeAgo(timestamp = request.createdAt)
                }
            }

            if (!isFull && !applied) {
                Button(
                    onClick = {
                        applied = true
                        onApply()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TeamBuilderColor),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = "Apply",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else if (applied) {
                FilledTonalButton(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Applied ✓", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamRequestSheet(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, skills: String, dept: String, year: Int, spots: Int, type: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(2) }
    var totalSpots by remember { mutableStateOf(4) }
    var selectedType by remember { mutableStateOf("Mini Project") }

    val typeOptions = listOf("Mini Project", "Final Year", "Hackathon", "Presentation", "Competition")

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
                text = "Post Team Request",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Project / Team Title") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Project Goal & Expectations") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = skillsInput,
                onValueChange = { skillsInput = it },
                label = { Text("Required Skills (e.g. Kotlin, Compose, Figma)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Dept (e.g. CS / IT)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Year $year", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { if (year > 1) year-- }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null)
                        }
                        IconButton(onClick = { if (year < 5) year++ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.AddCircleOutline, contentDescription = null)
                        }
                    }
                }
            }

            // Project Type
            Text("Project Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            ChipRow(
                items = typeOptions,
                selectedItem = selectedType,
                onItemSelected = { selectedType = it }
            )

            // Total Spots
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Team Size: $totalSpots members", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Row {
                    IconButton(onClick = { if (totalSpots > 2) totalSpots-- }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null)
                    }
                    IconButton(onClick = { if (totalSpots < 10) totalSpots++ }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = null)
                    }
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(
                            title.trim(),
                            description.trim(),
                            skillsInput.trim(),
                            department.trim(),
                            year,
                            totalSpots,
                            selectedType
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeamBuilderColor),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Publish Request", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

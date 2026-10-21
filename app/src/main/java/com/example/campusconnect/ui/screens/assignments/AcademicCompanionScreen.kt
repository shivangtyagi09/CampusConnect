package com.example.campusconnect.ui.screens.assignments

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.data.local.entity.AcademicRecordEntity
import com.example.campusconnect.ui.components.*
import com.example.campusconnect.ui.theme.*
import com.example.campusconnect.viewmodel.AssignmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCompanionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: AssignmentViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AssignmentViewModel(app) as T
    })

    val records by viewModel.academicRecords.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Summary calculations
    val totalSubjects = records.size
    val atRiskSubjects = records.count { record ->
        val requiredTee = calculateRequiredTee(record)
        requiredTee > record.teeTotal * 0.85f // If needed > 85% of TEE total
    }
    val avgInternalPercentage = if (records.isNotEmpty()) {
        records.map { r ->
            val totalInternal = r.m1Total + r.m2Total + r.icaTotal
            val earnedInternal = r.m1Score + r.m2Score + r.icaScore
            if (totalInternal > 0) (earnedInternal / totalInternal) * 100f else 0f
        }.average().toInt()
    } else 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Coral,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Subject",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Subject",
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
                title = "Academic Companion",
                subtitle = "Smart TEE target estimator & performance companion",
                gradientColors = listOf(Coral, Color(0xFFFF8A65)),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Subjects",
                            value = totalSubjects.toString(),
                            icon = Icons.Rounded.School,
                            accentColor = Sapphire,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "At Risk",
                            value = atRiskSubjects.toString(),
                            icon = Icons.Rounded.WarningAmber,
                            accentColor = if (atRiskSubjects > 0) ErrorColor else SuccessColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Avg Internal",
                            value = "$avgInternalPercentage%",
                            icon = Icons.Rounded.TrendingUp,
                            accentColor = Coral,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (records.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Rounded.AutoGraph,
                            title = "No subjects tracked yet",
                            message = "Add your semester subjects with M1, M2, and ICA scores to calculate required TEE scores for your target grades!",
                            actionText = "Add First Subject",
                            onActionClick = { showAddDialog = true }
                        )
                    }
                } else {
                    items(records, key = { it.id }) { record ->
                        SubjectAcademicCard(
                            record = record,
                            onDelete = { viewModel.deleteAcademicRecord(record) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                viewModel.addAcademicRecord(record)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubjectAcademicCard(
    record: AcademicRecordEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalInternalEarned = record.m1Score + record.m2Score + record.icaScore
    val totalInternalMax = record.m1Total + record.m2Total + record.icaTotal
    val internalPercentage = if (totalInternalMax > 0) (totalInternalEarned / totalInternalMax) * 100f else 0f

    val requiredTee = calculateRequiredTee(record)
    val maxTee = record.teeTotal.coerceAtLeast(100f)
    val requiredTeePercentage = (requiredTee / maxTee).coerceIn(0f, 1f)

    val (statusLabel, statusColor) = when {
        requiredTee <= 0 -> "Guaranteed Target" to SuccessColor
        requiredTee <= maxTee * 0.55f -> "Easily Achievable" to SuccessColor
        requiredTee <= maxTee * 0.75f -> "Tight Target" to WarningColor
        requiredTee <= maxTee -> "Challenging" to Coral
        else -> "Very Difficult (>100%)" to ErrorColor
    }

    LuxuryCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.subject,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Target Grade: ${record.targetGrade} (${getCutoffPercentage(record.targetGrade)}% Cutoff)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Coral,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // M1 and M2 side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // M1
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("M1 Exam", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${record.m1Score.toInt()}/${record.m1Total.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (record.m1Score / record.m1Total.coerceAtLeast(1f)).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Sapphire,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // M2
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("M2 Exam", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${record.m2Score.toInt()}/${record.m2Total.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (record.m2Score / record.m2Total.coerceAtLeast(1f)).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = SapphireLight,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ICA Progress Bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ICA (Internal Continuous Assessment)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${record.icaScore.toInt()}/${record.icaTotal.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (record.icaScore / record.icaTotal.coerceAtLeast(1f)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = RichGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        // Calculated TEE Requirement Box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = statusColor.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Required TEE Score",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "${"%.1f".format(requiredTee.coerceAtLeast(0f))} / ${record.teeTotal.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { requiredTeePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Internal avg: ${"%.1f".format(internalPercentage)}% • Need ${"%.1f".format((requiredTee / record.teeTotal * 100f).coerceAtLeast(0f))}% on Term End Exam",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

private fun getCutoffPercentage(grade: String): Int {
    return when (grade.uppercase()) {
        "O" -> 90
        "A+" -> 80
        "A" -> 70
        "B+" -> 60
        "B" -> 50
        "C" -> 40
        "P" -> 35
        else -> 50
    }
}

private fun calculateRequiredTee(record: AcademicRecordEntity): Float {
    val totalMarks = record.m1Total + record.m2Total + record.icaTotal + record.teeTotal
    val gradeCutoffFraction = getCutoffPercentage(record.targetGrade) / 100f
    val requiredTotal = gradeCutoffFraction * totalMarks
    val earnedInternal = record.m1Score + record.m2Score + record.icaScore
    return requiredTotal - earnedInternal
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAdd: (AcademicRecordEntity) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var m1Score by remember { mutableStateOf("24") }
    var m1Total by remember { mutableStateOf("30") }
    var m2Score by remember { mutableStateOf("26") }
    var m2Total by remember { mutableStateOf("30") }
    var icaScore by remember { mutableStateOf("18") }
    var icaTotal by remember { mutableStateOf("20") }
    var teeTotal by remember { mutableStateOf("100") }
    var targetGrade by remember { mutableStateOf("A+") }

    val gradeOptions = listOf("O", "A+", "A", "B+", "B", "C", "P")
    var gradeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Subject Assessment", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Name") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = m1Score,
                        onValueChange = { m1Score = it },
                        label = { Text("M1 Score") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = m1Total,
                        onValueChange = { m1Total = it },
                        label = { Text("M1 Total") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = m2Score,
                        onValueChange = { m2Score = it },
                        label = { Text("M2 Score") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = m2Total,
                        onValueChange = { m2Total = it },
                        label = { Text("M2 Total") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = icaScore,
                        onValueChange = { icaScore = it },
                        label = { Text("ICA Score") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = icaTotal,
                        onValueChange = { icaTotal = it },
                        label = { Text("ICA Total") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Target Grade Dropdown
                Box {
                    OutlinedTextField(
                        value = "Target Grade: $targetGrade",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { gradeMenuExpanded = true }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = gradeMenuExpanded,
                        onDismissRequest = { gradeMenuExpanded = false }
                    ) {
                        gradeOptions.forEach { g ->
                            DropdownMenuItem(
                                text = { Text("$g (${getCutoffPercentage(g)}%)") },
                                onClick = {
                                    targetGrade = g
                                    gradeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        val record = AcademicRecordEntity(
                            subject = subject.trim(),
                            m1Score = m1Score.toFloatOrNull() ?: 0f,
                            m1Total = m1Total.toFloatOrNull() ?: 30f,
                            m2Score = m2Score.toFloatOrNull() ?: 0f,
                            m2Total = m2Total.toFloatOrNull() ?: 30f,
                            icaScore = icaScore.toFloatOrNull() ?: 0f,
                            icaTotal = icaTotal.toFloatOrNull() ?: 20f,
                            teeTotal = teeTotal.toFloatOrNull() ?: 100f,
                            targetGrade = targetGrade
                        )
                        onAdd(record)
                    }
                },
                enabled = subject.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

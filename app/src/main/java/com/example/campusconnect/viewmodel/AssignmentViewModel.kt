package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.AcademicRecordEntity
import com.example.campusconnect.data.local.entity.AssignmentEntity
import com.example.campusconnect.data.remote.FirebaseSyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssignmentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val assignmentDao = db.assignmentDao()
    private val academicRecordDao = db.academicRecordDao()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    private val _sortBy = MutableStateFlow("Due Date")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    val assignments: StateFlow<List<AssignmentEntity>> = assignmentDao.getAllAssignments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val academicRecords: StateFlow<List<AcademicRecordEntity>> = academicRecordDao.getRecordsByUser(0L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalCount: StateFlow<Int> = assignments.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> = assignments.map { list -> list.count { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingCount: StateFlow<Int> = assignments.map { list ->
        val now = System.currentTimeMillis()
        list.count { !it.isCompleted && it.dueDate >= now }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overdueCount: StateFlow<Int> = assignments.map { list ->
        val now = System.currentTimeMillis()
        list.count { !it.isCompleted && it.dueDate < now }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredAssignments: StateFlow<List<AssignmentEntity>> = combine(
        assignments,
        _selectedType,
        _showCompleted,
        _sortBy
    ) { list, typeFilter, isCompletedTab, sortCriteria ->
        var filtered = list.filter { it.isCompleted == isCompletedTab }

        if (typeFilter != "All") {
            filtered = filtered.filter { it.type.equals(typeFilter, ignoreCase = true) }
        }

        when (sortCriteria) {
            "Priority" -> filtered.sortedByDescending { it.priority }
            "Subject" -> filtered.sortedBy { it.subject.lowercase() }
            else -> filtered.sortedBy { it.dueDate } // "Due Date"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setTypeFilter(type: String) {
        _selectedType.value = type
    }

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }

    fun setSortBy(sortBy: String) {
        _sortBy.value = sortBy
    }

    fun addAssignment(
        title: String,
        subject: String,
        description: String = "",
        dueDate: Long,
        type: String = "Assignment",
        priority: Int = 0,
        userId: Long = 0L
    ) {
        viewModelScope.launch {
            val entity = AssignmentEntity(
                userId = userId,
                title = title,
                subject = subject,
                description = description,
                dueDate = dueDate,
                type = type,
                isCompleted = false,
                priority = priority
            )
            val insertedId = assignmentDao.insertAssignment(entity)
            FirebaseSyncManager.publishAssignment(entity.copy(id = insertedId))
        }
    }

    fun toggleCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            assignmentDao.toggleCompleted(id, completed)
            FirebaseSyncManager.updateAssignmentCompletion(id, completed)
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            assignmentDao.deleteAssignment(assignment)
            FirebaseSyncManager.deleteAssignment(assignment)
        }
    }

    fun addAcademicRecord(record: AcademicRecordEntity) {
        viewModelScope.launch {
            academicRecordDao.insertRecord(record)
        }
    }

    fun updateAcademicRecord(record: AcademicRecordEntity) {
        viewModelScope.launch {
            academicRecordDao.updateRecord(record)
        }
    }

    fun deleteAcademicRecord(record: AcademicRecordEntity) {
        viewModelScope.launch {
            academicRecordDao.deleteRecord(record)
        }
    }
}

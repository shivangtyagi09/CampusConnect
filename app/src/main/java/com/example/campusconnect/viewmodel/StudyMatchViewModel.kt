package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.StudyMatchEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyMatchViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val studyMatchDao = db.studyMatchDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val matches: StateFlow<List<StudyMatchEntity>> = studyMatchDao.getOpenMatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredMatches: StateFlow<List<StudyMatchEntity>> = combine(
        matches,
        _searchQuery,
        _selectedFilter
    ) { matchList, query, filter ->
        matchList.filter { match ->
            val matchesQuery = if (query.isBlank()) true else {
                match.subject.contains(query, ignoreCase = true) ||
                        match.examName.contains(query, ignoreCase = true) ||
                        match.description.contains(query, ignoreCase = true)
            }

            val matchesFilter = when (filter) {
                "Exam Prep" -> match.examName.isNotBlank() || match.subject.contains("Prep", ignoreCase = true)
                "Subject Study" -> match.examName.isBlank() || match.subject.contains("Study", ignoreCase = true)
                "Group Discussion" -> match.maxMembers >= 4 || match.description.contains("discussion", ignoreCase = true)
                else -> true // "All"
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun createMatch(
        subject: String,
        examName: String = "",
        description: String = "",
        userId: Long = 1L,
        userName: String = "Student",
        maxMembers: Int = 6
    ) {
        viewModelScope.launch {
            val newMatch = StudyMatchEntity(
                userId = userId,
                userName = userName,
                subject = subject,
                examName = examName,
                description = description,
                status = "open",
                memberCount = 1,
                maxMembers = maxMembers,
                createdAt = System.currentTimeMillis()
            )
            studyMatchDao.insertMatch(newMatch)
        }
    }

    fun joinMatch(matchId: Long) {
        viewModelScope.launch {
            val current = matches.value.find { it.id == matchId } ?: return@launch
            if (current.memberCount < current.maxMembers) {
                val updatedCount = current.memberCount + 1
                val updatedStatus = if (updatedCount >= current.maxMembers) "matched" else "open"
                studyMatchDao.updateMatch(
                    current.copy(
                        memberCount = updatedCount,
                        status = updatedStatus
                    )
                )
            }
        }
    }

    fun deleteMatch(match: StudyMatchEntity) {
        viewModelScope.launch {
            studyMatchDao.deleteMatch(match)
        }
    }

    fun getMatchById(id: Long): Flow<StudyMatchEntity?> {
        return studyMatchDao.getMatchById(id)
    }
}

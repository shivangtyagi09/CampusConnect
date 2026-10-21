/**
 * CampusConnect - Academic & Collaboration Module
 * Contributed by: Diya Ponda (Member 2)
 */
package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.TeamRequestEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TeamBuilderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val teamRequestDao = db.teamRequestDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    val requests: StateFlow<List<TeamRequestEntity>> = teamRequestDao.getAllRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredRequests: StateFlow<List<TeamRequestEntity>> = combine(
        requests,
        _searchQuery,
        _selectedType
    ) { requestList, query, typeFilter ->
        requestList.filter { req ->
            val matchesQuery = if (query.isBlank()) true else {
                req.projectTitle.contains(query, ignoreCase = true) ||
                        req.description.contains(query, ignoreCase = true) ||
                        req.requiredSkills.contains(query, ignoreCase = true) ||
                        req.department.contains(query, ignoreCase = true)
            }

            val matchesType = if (typeFilter == "All") true else {
                req.type.equals(typeFilter, ignoreCase = true)
            }

            matchesQuery && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: String) {
        _selectedType.value = type
    }

    fun createRequest(
        projectTitle: String,
        description: String,
        skills: String,
        department: String,
        year: Int,
        spots: Int,
        type: String,
        userId: Long = 1L,
        userName: String = "Student"
    ) {
        viewModelScope.launch {
            val entity = TeamRequestEntity(
                userId = userId,
                userName = userName,
                projectTitle = projectTitle,
                description = description,
                requiredSkills = skills,
                department = department,
                year = year,
                totalSpots = spots.coerceAtLeast(2),
                filledSpots = 1,
                type = type,
                createdAt = System.currentTimeMillis()
            )
            teamRequestDao.insertRequest(entity)
        }
    }

    fun applyToRequest(requestId: Long) {
        viewModelScope.launch {
            val current = requests.value.find { it.id == requestId } ?: return@launch
            if (current.filledSpots < current.totalSpots) {
                teamRequestDao.updateRequest(
                    current.copy(filledSpots = current.filledSpots + 1)
                )
            }
        }
    }

    fun deleteRequest(request: TeamRequestEntity) {
        viewModelScope.launch {
            teamRequestDao.deleteRequest(request)
        }
    }
}

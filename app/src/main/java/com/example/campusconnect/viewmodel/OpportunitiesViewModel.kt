package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.OpportunityEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OpportunitiesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val opportunityDao = db.opportunityDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val allOpportunities: StateFlow<List<OpportunityEntity>> = opportunityDao.getAllOpportunities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val opportunities: StateFlow<List<OpportunityEntity>> = combine(
        allOpportunities,
        _searchQuery,
        _selectedType
    ) { list, query, type ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.organization.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)

            val matchesType = type.equals("All", ignoreCase = true) ||
                    item.type.equals(type, ignoreCase = true)

            matchesQuery && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val total: StateFlow<Int> = allOpportunities.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiringSoon: StateFlow<Int> = allOpportunities.map { list ->
        val now = System.currentTimeMillis()
        val sevenDaysLater = now + (7L * 24 * 60 * 60 * 1000)
        list.count { it.deadline in now..sevenDaysLater }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val newThisWeek: StateFlow<Int> = allOpportunities.map { list ->
        val oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        list.count { it.createdAt >= oneWeekAgo }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setType(type: String) {
        _selectedType.value = type
    }

    fun addOpportunity(
        title: String,
        description: String,
        type: String,
        organization: String,
        deadline: Long,
        link: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val entity = OpportunityEntity(
                title = title.trim(),
                description = description.trim(),
                type = type.trim(),
                organization = organization.trim(),
                deadline = deadline,
                link = link.trim(),
                createdAt = System.currentTimeMillis()
            )
            opportunityDao.insertOpportunity(entity)
        }
    }

    fun deleteOpportunity(opportunity: OpportunityEntity) {
        viewModelScope.launch {
            opportunityDao.deleteOpportunity(opportunity)
        }
    }
}

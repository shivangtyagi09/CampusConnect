package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.LostFoundItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LostFoundViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val lostFoundDao = db.lostFoundDao()

    private val _selectedType = MutableStateFlow("All") // "All", "Lost", "Found"
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _allItems = lostFoundDao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val items: StateFlow<List<LostFoundItemEntity>> = combine(
        _allItems,
        _selectedType
    ) { itemList, type ->
        when (type) {
            "Lost" -> itemList.filter { it.type.equals("Lost", ignoreCase = true) }
            "Found" -> itemList.filter { it.type.equals("Found", ignoreCase = true) }
            else -> itemList
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalLost: StateFlow<Int> = _allItems.map { list ->
        list.count { it.type.equals("Lost", ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalFound: StateFlow<Int> = _allItems.map { list ->
        list.count { it.type.equals("Found", ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val resolved: StateFlow<Int> = _allItems.map { list ->
        list.count { it.isResolved }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setType(type: String) {
        _selectedType.value = type
    }

    fun reportItem(
        title: String,
        description: String,
        type: String,
        location: String,
        securityQuestion: String = "",
        securityAnswer: String = "",
        userId: Long = 1L,
        reporterName: String = "Student",
        imageUrl: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = LostFoundItemEntity(
                userId = userId,
                reporterName = reporterName,
                title = title,
                description = description,
                imageUrl = imageUrl,
                type = type,
                location = location,
                securityQuestion = securityQuestion,
                securityAnswer = securityAnswer,
                isResolved = false,
                createdAt = System.currentTimeMillis()
            )
            lostFoundDao.insertItem(item)
        }
    }

    fun resolveItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            lostFoundDao.resolveItem(id)
        }
    }
}

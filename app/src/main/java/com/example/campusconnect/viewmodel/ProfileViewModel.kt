package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.MarketplaceItemEntity
import com.example.campusconnect.data.local.entity.PostEntity
import com.example.campusconnect.data.local.entity.StudyMatchEntity
import com.example.campusconnect.data.local.entity.UserEntity
import com.example.campusconnect.data.remote.FirebaseSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val userDao = db.userDao()
    private val postDao = db.postDao()
    private val marketplaceDao = db.marketplaceDao()
    private val studyMatchDao = db.studyMatchDao()

    private val _currentUserId = MutableStateFlow(1L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    fun setUserId(id: Long) {
        if (id > 0) {
            _currentUserId.value = id
        }
    }

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val user: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        userDao.getUserById(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userPosts: StateFlow<List<PostEntity>> = _currentUserId.flatMapLatest { id ->
        postDao.getPostsByUser(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userListings: StateFlow<List<MarketplaceItemEntity>> = _currentUserId.flatMapLatest { id ->
        marketplaceDao.getItemsByUser(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userStudyGroups: StateFlow<List<StudyMatchEntity>> = _currentUserId.flatMapLatest { id ->
        studyMatchDao.getMatchesByUser(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val postCount: StateFlow<Int> = userPosts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val listingCount: StateFlow<Int> = userListings.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val studyGroupCount: StateFlow<Int> = userStudyGroups.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun loadUser(userId: Long) {
        _currentUserId.value = userId
    }

    fun setTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            userDao.updateUser(user)
            FirebaseSyncManager.publishUser(user)
        }
    }
}

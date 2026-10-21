package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.AssignmentEntity
import com.example.campusconnect.data.local.entity.PostEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val assignmentDao = db.assignmentDao()
    private val postDao = db.postDao()

    private val _upcomingAssignments = MutableStateFlow<List<AssignmentEntity>>(emptyList())
    val upcomingAssignments: StateFlow<List<AssignmentEntity>> = _upcomingAssignments.asStateFlow()

    private val _recentPosts = MutableStateFlow<List<PostEntity>>(emptyList())
    val recentPosts: StateFlow<List<PostEntity>> = _recentPosts.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            assignmentDao.getUpcomingAssignments(System.currentTimeMillis(), 5).collect {
                _upcomingAssignments.value = it
            }
        }
        viewModelScope.launch {
            postDao.getAllPosts().collect {
                _recentPosts.value = it.take(5)
            }
        }
    }
}

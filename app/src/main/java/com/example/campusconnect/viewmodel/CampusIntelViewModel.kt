package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.dao.CategoryStat
import com.example.campusconnect.data.local.entity.CampusFeedbackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CampusIntelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val campusFeedbackDao = db.campusFeedbackDao()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _rawFeedbackList = campusFeedbackDao.getAllFeedback()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val feedbackList: StateFlow<List<CampusFeedbackEntity>> = combine(
        _rawFeedbackList,
        _selectedCategory
    ) { feedbacks, category ->
        if (category == "All") {
            feedbacks
        } else {
            feedbacks.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categoryStats: StateFlow<List<CategoryStat>> = campusFeedbackDao.getCategoryStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun submitFeedback(
        category: String,
        rating: Float,
        feedback: String,
        location: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = CampusFeedbackEntity(
                category = category,
                rating = rating,
                feedback = feedback,
                location = location,
                upvotes = 0,
                createdAt = System.currentTimeMillis()
            )
            campusFeedbackDao.insertFeedback(entity)
        }
    }

    fun upvoteFeedback(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            campusFeedbackDao.upvoteFeedback(id)
        }
    }
}

package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.PostEntity
import com.example.campusconnect.data.remote.FirebaseSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SocialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val postDao = db.postDao()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val allPosts: StateFlow<List<PostEntity>> = postDao.getAllPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val posts: StateFlow<List<PostEntity>> = combine(allPosts, _selectedCategory) { postList, category ->
        if (category.equals("All", ignoreCase = true) || category.isBlank()) {
            postList
        } else {
            postList.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun createPost(
        content: String,
        imageUrl: String = "",
        category: String = "General",
        userId: Long = 1L,
        userName: String = "Campus Member",
        userAvatar: String = ""
    ) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val post = PostEntity(
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                content = content.trim(),
                imageUrl = imageUrl.trim(),
                category = category,
                likes = 0,
                isLiked = false,
                commentsCount = 0,
                createdAt = System.currentTimeMillis()
            )
            val insertedId = postDao.insertPost(post)
            FirebaseSyncManager.publishPost(post.copy(id = insertedId))
        }
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            postDao.likePost(postId)
        }
    }

    fun unlikePost(postId: Long) {
        viewModelScope.launch {
            postDao.unlikePost(postId)
        }
    }

    fun toggleLike(post: PostEntity) {
        if (post.isLiked) {
            unlikePost(post.id)
        } else {
            likePost(post.id)
        }
    }

    fun deletePost(post: PostEntity) {
        viewModelScope.launch {
            postDao.deletePost(post)
        }
    }
}

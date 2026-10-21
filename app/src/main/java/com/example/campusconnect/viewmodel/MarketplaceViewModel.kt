package com.example.campusconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.MarketplaceItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val marketplaceDao = db.marketplaceDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _showMyListings = MutableStateFlow(false)
    val showMyListings: StateFlow<Boolean> = _showMyListings.asStateFlow()

    private val _currentUserId = MutableStateFlow(1L)

    private val _sourceItems = combine(_showMyListings, _currentUserId) { showMine, userId ->
        Pair(showMine, userId)
    }.flatMapLatest { (showMine, userId) ->
        if (showMine) {
            marketplaceDao.getItemsByUser(userId)
        } else {
            marketplaceDao.getAvailableItems()
        }
    }

    val items: StateFlow<List<MarketplaceItemEntity>> = combine(
        _sourceItems,
        _searchQuery,
        _selectedCategory
    ) { itemList, query, category ->
        itemList.filter { item ->
            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleMyListings(userId: Long = 1L) {
        _currentUserId.value = userId
        _showMyListings.value = !_showMyListings.value
    }

    fun createListing(
        title: String,
        description: String,
        price: Double,
        category: String,
        condition: String,
        userId: Long,
        sellerName: String,
        imageUrl: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = MarketplaceItemEntity(
                userId = userId,
                sellerName = sellerName,
                title = title,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                condition = condition,
                isSold = false,
                createdAt = System.currentTimeMillis()
            )
            marketplaceDao.insertItem(item)
        }
    }

    fun markAsSold(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            marketplaceDao.markAsSold(id)
        }
    }

    fun deleteListing(item: MarketplaceItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            marketplaceDao.deleteItem(item)
        }
    }
}

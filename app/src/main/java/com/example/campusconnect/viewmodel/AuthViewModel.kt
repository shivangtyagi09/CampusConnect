package com.example.campusconnect.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.CampusConnectApp
import com.example.campusconnect.data.local.entity.UserEntity
import com.example.campusconnect.data.remote.FirebaseSyncManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusConnectApp).database
    private val userDao = db.userDao()
    private val prefs = application.getSharedPreferences("campus_auth_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val savedUserId = prefs.getLong("saved_user_id", -1L)
        if (savedUserId != -1L) {
            viewModelScope.launch(Dispatchers.IO) {
                val user = userDao.findUserById(savedUserId)
                if (user != null) {
                    _currentUser.value = user
                    FirebaseSyncManager.publishUser(user)
                }
            }
        }
    }

    companion object {
        fun isNmimsEmail(email: String): Boolean {
            val clean = email.trim().lowercase()
            return clean.endsWith("@nmims.in") || clean.endsWith("@nmims.edu")
        }

        fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val cleanEmail = email.trim().lowercase()
                val cleanPassword = password.trim()
                if (!isNmimsEmail(cleanEmail)) {
                    _errorMessage.value = "Please use an official NMIMS email (@nmims.in or @nmims.edu)"
                    return@launch
                }

                var user = userDao.getUserByEmail(cleanEmail)
                if (user == null) {
                    // Check cloud Firestore for accounts registered on Appetize or other devices
                    try {
                        val doc = FirebaseFirestore.getInstance().collection("users").document(cleanEmail).get().await()
                        if (doc.exists()) {
                            val cloudUser = UserEntity(
                                id = 0L,
                                name = doc.getString("name") ?: "Student",
                                email = cleanEmail,
                                password = doc.getString("password") ?: "",
                                department = doc.getString("department") ?: "Computer Science",
                                year = doc.getLong("year")?.toInt() ?: 1,
                                bio = doc.getString("bio") ?: "",
                                skills = doc.getString("skills") ?: "",
                                profileImageUrl = doc.getString("profileImageUrl") ?: "",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            val insertedId = userDao.insertUser(cloudUser)
                            user = cloudUser.copy(id = insertedId)
                        }
                    } catch (e: Exception) {
                        // Offline fallback
                    }
                }

                if (user == null) {
                    _errorMessage.value = "No account found for $cleanEmail. Please tap 'Register' below to create one."
                    return@launch
                }

                val hashed = hashPassword(cleanPassword)
                if (user.password == hashed || user.password == cleanPassword) {
                    if (user.password == cleanPassword && user.password != hashed) {
                        userDao.updatePassword(cleanEmail, hashed)
                    }
                    prefs.edit().putLong("saved_user_id", user.id).apply()
                    _currentUser.value = user
                    FirebaseSyncManager.publishUser(user)
                } else {
                    _errorMessage.value = "Incorrect password. If you forgot your password, tap 'Forgot Password?' below."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(email: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanEmail = email.trim().lowercase()
                val cleanPassword = newPass.trim()
                if (!isNmimsEmail(cleanEmail)) {
                    onResult(false, "Please use an official NMIMS email (@nmims.in or @nmims.edu)")
                    return@launch
                }
                if (cleanPassword.length < 6) {
                    onResult(false, "Password must be at least 6 characters")
                    return@launch
                }
                val existing = userDao.getUserByEmail(cleanEmail)
                if (existing == null) {
                    onResult(false, "No account found with this email. Please register first.")
                    return@launch
                }
                val hashed = hashPassword(cleanPassword)
                userDao.updatePassword(cleanEmail, hashed)
                try {
                    FirebaseFirestore.getInstance().collection("users").document(cleanEmail).update("password", hashed).await()
                } catch (e: Exception) {
                    // Offline fallback
                }
                onResult(true, "Password reset successfully! Please sign in with your new password.")
            } catch (e: Exception) {
                onResult(false, "Password reset failed: ${e.message}")
            }
        }
    }

    fun register(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val cleanEmail = user.email.trim().lowercase()
                val cleanPassword = user.password.trim()
                if (!isNmimsEmail(cleanEmail)) {
                    _errorMessage.value = "Please use an official NMIMS email (@nmims.in or @nmims.edu)"
                    return@launch
                }
                if (cleanPassword.length < 6) {
                    _errorMessage.value = "Password must be at least 6 characters"
                    return@launch
                }
                val existing = userDao.getUserByEmail(cleanEmail)
                if (existing != null) {
                    _errorMessage.value = "An account with this email already exists"
                } else {
                    val secureUser = user.copy(
                        email = cleanEmail,
                        password = hashPassword(cleanPassword)
                    )
                    val id = userDao.insertUser(secureUser)
                    val registeredUser = secureUser.copy(id = id)
                    prefs.edit().putLong("saved_user_id", id).apply()
                    _currentUser.value = registeredUser
                    // Publish to cloud Firestore so all peers see them immediately
                    FirebaseSyncManager.publishUser(registeredUser)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Registration failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        prefs.edit().remove("saved_user_id").apply()
        _currentUser.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

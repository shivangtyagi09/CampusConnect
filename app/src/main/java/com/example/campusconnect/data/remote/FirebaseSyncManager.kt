package com.example.campusconnect.data.remote

import android.content.Context
import android.net.Uri
import com.example.campusconnect.data.local.AppDatabase
import com.example.campusconnect.data.local.entity.AssignmentEntity
import com.example.campusconnect.data.local.entity.PostEntity
import com.example.campusconnect.data.local.entity.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseSyncManager {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun isSampleEmail(email: String): Boolean {
        val clean = email.trim().lowercase()
        return clean in listOf(
            "aarav.sharma22@nmims.edu",
            "ananya.iyer23@nmims.edu",
            "rohan.mehta21@nmims.edu",
            "priya.nair24@nmims.edu",
            "student@nmims.in"
        ) || clean.contains("aarav") || clean.contains("ananya") || clean.contains("priya.nair")
    }

    fun startSync(context: Context) {
        val db = AppDatabase.getDatabase(context)

        try {
            // 0. Auto-sync only REAL registered students to Firestore (never samples)
            scope.launch {
                try {
                    val localUsers = db.userDao().getAllUsersDirect()
                    localUsers.filter { it.password.isNotBlank() && !isSampleEmail(it.email) }.forEach { user ->
                        publishUser(user)
                    }
                } catch (e: Exception) {
                    // Ignore offline
                }
            }

            // Immediately wipe any sample / fake profiles from local database and Firestore
            scope.launch {
                try {
                    val locals = db.userDao().getAllUsersDirect()
                    locals.forEach { user ->
                        if (user.password.isBlank() || isSampleEmail(user.email)) {
                            db.userDao().deleteUser(user)
                        }
                    }
                    val sampleEmails = listOf(
                        "aarav.sharma22@nmims.edu",
                        "ananya.iyer23@nmims.edu",
                        "rohan.mehta21@nmims.edu",
                        "priya.nair24@nmims.edu",
                        "student@nmims.in"
                    )
                    sampleEmails.forEach { email ->
                        db.userDao().deleteUserByEmail(email)
                        firestore.collection("users").document(email).delete()
                    }
                } catch (e: Exception) {
                    // Ignore offline
                }
            }

            // 1. Real-time Post Sync
            firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            try {
                                val content = doc.getString("content") ?: return@mapNotNull null
                                PostEntity(
                                    id = doc.getLong("localId") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                    userId = doc.getLong("userId") ?: 1L,
                                    userName = doc.getString("userName") ?: "Campus Member",
                                    userAvatar = doc.getString("userAvatar") ?: "",
                                    content = content,
                                    imageUrl = doc.getString("imageUrl") ?: "",
                                    likes = doc.getLong("likes")?.toInt() ?: 0,
                                    isLiked = doc.getBoolean("isLiked") ?: false,
                                    commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                                    category = doc.getString("category") ?: "General",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        posts.forEach { post ->
                            db.postDao().insertPost(post)
                        }
                    }
                }

            // 2. Real-time Assignment Sync
            firestore.collection("assignments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        val remoteAssignments = snapshot.documents.mapNotNull { doc ->
                            try {
                                val title = doc.getString("title") ?: return@mapNotNull null
                                val id = doc.getLong("localId") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                                AssignmentEntity(
                                    id = id,
                                    userId = doc.getLong("userId") ?: 0L,
                                    title = title,
                                    subject = doc.getString("subject") ?: "",
                                    description = doc.getString("description") ?: "",
                                    dueDate = doc.getLong("dueDate") ?: System.currentTimeMillis(),
                                    type = doc.getString("type") ?: "Assignment",
                                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                                    priority = doc.getLong("priority")?.toInt() ?: 0
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        // Remove locally any assignments that were deleted on cloud
                        val remoteIds = remoteAssignments.map { it.id }.toSet()
                        val localList = db.assignmentDao().getAllAssignmentsDirect()
                        localList.forEach { local ->
                            if (!remoteIds.contains(local.id)) {
                                db.assignmentDao().deleteAssignmentById(local.id)
                            }
                        }

                        // Insert or update remaining assignments
                        remoteAssignments.forEach { assignment ->
                            db.assignmentDao().insertAssignment(assignment)
                        }
                    }
                }

            // 3. Real-time User / Peer Sync (Syncs all students across Android & Appetize)
            firestore.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        val validRemoteUsers = snapshot.documents.mapNotNull { doc ->
                            try {
                                val email = doc.getString("email")?.trim()?.lowercase() ?: return@mapNotNull null
                                val pass = doc.getString("password") ?: ""
                                if (pass.isBlank() || isSampleEmail(email)) {
                                    // Permanently delete sample / fake user from Firestore
                                    doc.reference.delete()
                                    return@mapNotNull null
                                }
                                val name = doc.getString("name") ?: "Student"
                                UserEntity(
                                    id = 0L,
                                    name = name,
                                    email = email,
                                    password = pass,
                                    department = doc.getString("department") ?: "Computer Science",
                                    year = doc.getLong("year")?.toInt() ?: 1,
                                    bio = doc.getString("bio") ?: "",
                                    skills = doc.getString("skills") ?: "",
                                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        // Remove locally any fake users or sample accounts
                        val localList = db.userDao().getAllUsersDirect()
                        localList.forEach { local ->
                            val clean = local.email.trim().lowercase()
                            if (local.password.isBlank() || isSampleEmail(clean)) {
                                db.userDao().deleteUserByEmail(clean)
                            }
                        }

                        // Insert or update only genuine registered students
                        validRemoteUsers.forEach { incoming ->
                            val existing = db.userDao().getUserByEmail(incoming.email)
                            if (existing != null) {
                                db.userDao().updateUser(incoming.copy(id = existing.id))
                            } else {
                                db.userDao().insertUser(incoming)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            // Firebase initialization safety guard
        }
    }

    suspend fun publishUser(user: UserEntity) {
        try {
            val cleanEmail = user.email.trim().lowercase()
            if (cleanEmail.isBlank() || isSampleEmail(cleanEmail) || user.password.isBlank()) return
            val data = hashMapOf(
                "name" to user.name,
                "email" to cleanEmail,
                "password" to user.password,
                "department" to user.department,
                "year" to user.year,
                "bio" to user.bio,
                "skills" to user.skills,
                "profileImageUrl" to user.profileImageUrl,
                "createdAt" to user.createdAt
            )
            firestore.collection("users").document(cleanEmail).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun publishPost(post: PostEntity) {
        try {
            var finalImageUrl = post.imageUrl
            if (finalImageUrl.startsWith("file://") || finalImageUrl.startsWith("content://")) {
                try {
                    val ref = storage.reference.child("posts/${UUID.randomUUID()}.jpg")
                    val uploadUri = Uri.parse(finalImageUrl)
                    ref.putFile(uploadUri).await()
                    finalImageUrl = ref.downloadUrl.await().toString()
                } catch (e: Exception) {
                    // Fallback to local URI if storage upload fails
                }
            }

            val data = hashMapOf(
                "localId" to post.id,
                "userId" to post.userId,
                "userName" to post.userName,
                "userAvatar" to post.userAvatar,
                "content" to post.content,
                "imageUrl" to finalImageUrl,
                "likes" to post.likes,
                "isLiked" to post.isLiked,
                "commentsCount" to post.commentsCount,
                "category" to post.category,
                "createdAt" to post.createdAt
            )
            firestore.collection("posts").add(data).await()
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun publishAssignment(assignment: AssignmentEntity) {
        try {
            val docId = if (assignment.id > 0) assignment.id.toString() else UUID.randomUUID().toString()
            val data = hashMapOf(
                "localId" to assignment.id,
                "userId" to assignment.userId,
                "title" to assignment.title,
                "subject" to assignment.subject,
                "description" to assignment.description,
                "dueDate" to assignment.dueDate,
                "type" to assignment.type,
                "isCompleted" to assignment.isCompleted,
                "priority" to assignment.priority
            )
            firestore.collection("assignments").document(docId).set(data).await()
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateAssignmentCompletion(id: Long, completed: Boolean) {
        try {
            firestore.collection("assignments").document(id.toString()).update("isCompleted", completed).await()
        } catch (e: Exception) {
            try {
                val query = firestore.collection("assignments").whereEqualTo("localId", id).get().await()
                for (doc in query.documents) {
                    doc.reference.update("isCompleted", completed).await()
                }
            } catch (e2: Exception) {
                // Offline fallback
            }
        }
    }

    suspend fun deleteAssignment(assignment: AssignmentEntity) {
        try {
            firestore.collection("assignments").document(assignment.id.toString()).delete().await()
        } catch (e: Exception) {}
        try {
            val query = firestore.collection("assignments").whereEqualTo("localId", assignment.id).get().await()
            for (doc in query.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {}
    }
}

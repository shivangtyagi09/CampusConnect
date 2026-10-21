package com.example.campusconnect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val department: String = "",
    val year: Int = 1,
    val bio: String = "",
    val skills: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String = "",
    val userAvatar: String = "",
    val content: String,
    val imageUrl: String = "",
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val title: String,
    val subject: String,
    val description: String = "",
    val dueDate: Long,
    val type: String = "Assignment",
    val isCompleted: Boolean = false,
    val priority: Int = 0
)

@Entity(tableName = "study_matches")
data class StudyMatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String = "",
    val subject: String,
    val examName: String = "",
    val description: String = "",
    val status: String = "open",
    val memberCount: Int = 1,
    val maxMembers: Int = 6,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "team_requests")
data class TeamRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String = "",
    val projectTitle: String,
    val description: String = "",
    val requiredSkills: String = "",
    val department: String = "",
    val year: Int = 0,
    val totalSpots: Int = 4,
    val filledSpots: Int = 1,
    val type: String = "Project",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "campus_feedback")
data class CampusFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val rating: Float,
    val feedback: String,
    val location: String = "",
    val upvotes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketplace_items")
data class MarketplaceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val sellerName: String = "",
    val title: String,
    val description: String = "",
    val price: Double,
    val category: String = "",
    val imageUrl: String = "",
    val condition: String = "Good",
    val isSold: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lost_found_items")
data class LostFoundItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val reporterName: String = "",
    val title: String,
    val description: String = "",
    val imageUrl: String = "",
    val type: String = "Lost",
    val location: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: String = "",
    val organization: String = "",
    val deadline: Long = 0,
    val link: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val userId: Long,
    val userName: String = "",
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "academic_records")
data class AcademicRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val subject: String,
    val m1Score: Float = 0f,
    val m1Total: Float = 30f,
    val m2Score: Float = 0f,
    val m2Total: Float = 30f,
    val icaScore: Float = 0f,
    val icaTotal: Float = 20f,
    val teeScore: Float = 0f,
    val teeTotal: Float = 100f,
    val targetGrade: String = "O",
    val semester: Int = 1
)

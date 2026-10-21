package com.example.campusconnect.data.local.dao

import androidx.room.*
import com.example.campusconnect.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET password = :newHashedPassword WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email))")
    suspend fun updatePassword(email: String, newHashedPassword: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersDirect(): List<UserEntity>

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email))")
    suspend fun deleteUserByEmail(email: String)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE category = :category ORDER BY createdAt DESC")
    fun getPostsByCategory(category: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: Long): Flow<PostEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("UPDATE posts SET likes = likes + 1, isLiked = 1 WHERE id = :postId")
    suspend fun likePost(postId: Long)

    @Query("UPDATE posts SET likes = CASE WHEN likes > 0 THEN likes - 1 ELSE 0 END, isLiked = 0 WHERE id = :postId")
    suspend fun unlikePost(postId: Long)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE userId = :userId ORDER BY dueDate ASC")
    fun getAssignmentsByUser(userId: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE isCompleted = 0 AND dueDate >= :now ORDER BY dueDate ASC LIMIT :limit")
    fun getUpcomingAssignments(now: Long, limit: Int = 5): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE subject = :subject ORDER BY dueDate ASC")
    fun getAssignmentsBySubject(subject: String): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    @Query("UPDATE assignments SET isCompleted = :completed WHERE id = :id")
    suspend fun toggleCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)

    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignmentsDirect(): List<AssignmentEntity>
}

@Dao
interface StudyMatchDao {
    @Query("SELECT * FROM study_matches WHERE status = 'open' ORDER BY createdAt DESC")
    fun getOpenMatches(): Flow<List<StudyMatchEntity>>

    @Query("SELECT * FROM study_matches WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMatchesByUser(userId: Long): Flow<List<StudyMatchEntity>>

    @Query("SELECT * FROM study_matches WHERE subject LIKE '%' || :query || '%' OR examName LIKE '%' || :query || '%'")
    fun searchMatches(query: String): Flow<List<StudyMatchEntity>>

    @Query("SELECT * FROM study_matches WHERE id = :id")
    fun getMatchById(id: Long): Flow<StudyMatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: StudyMatchEntity): Long

    @Update
    suspend fun updateMatch(match: StudyMatchEntity)

    @Delete
    suspend fun deleteMatch(match: StudyMatchEntity)
}

@Dao
interface TeamRequestDao {
    @Query("SELECT * FROM team_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<TeamRequestEntity>>

    @Query("SELECT * FROM team_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRequestsByUser(userId: Long): Flow<List<TeamRequestEntity>>

    @Query("SELECT * FROM team_requests WHERE requiredSkills LIKE '%' || :skill || '%'")
    fun getRequestsBySkill(skill: String): Flow<List<TeamRequestEntity>>

    @Query("SELECT * FROM team_requests WHERE department = :dept OR department = ''")
    fun getRequestsByDepartment(dept: String): Flow<List<TeamRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: TeamRequestEntity): Long

    @Update
    suspend fun updateRequest(request: TeamRequestEntity)

    @Delete
    suspend fun deleteRequest(request: TeamRequestEntity)
}

@Dao
interface CampusFeedbackDao {
    @Query("SELECT * FROM campus_feedback ORDER BY createdAt DESC")
    fun getAllFeedback(): Flow<List<CampusFeedbackEntity>>

    @Query("SELECT * FROM campus_feedback WHERE category = :category ORDER BY createdAt DESC")
    fun getFeedbackByCategory(category: String): Flow<List<CampusFeedbackEntity>>

    @Query("SELECT category, AVG(rating) as avgRating, COUNT(*) as count FROM campus_feedback GROUP BY category")
    fun getCategoryStats(): Flow<List<CategoryStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: CampusFeedbackEntity): Long

    @Query("UPDATE campus_feedback SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun upvoteFeedback(id: Long)
}

data class CategoryStat(
    val category: String,
    val avgRating: Float,
    val count: Int
)

@Dao
interface MarketplaceDao {
    @Query("SELECT * FROM marketplace_items WHERE isSold = 0 ORDER BY createdAt DESC")
    fun getAvailableItems(): Flow<List<MarketplaceItemEntity>>

    @Query("SELECT * FROM marketplace_items WHERE category = :category AND isSold = 0 ORDER BY createdAt DESC")
    fun getItemsByCategory(category: String): Flow<List<MarketplaceItemEntity>>

    @Query("SELECT * FROM marketplace_items WHERE userId = :userId ORDER BY createdAt DESC")
    fun getItemsByUser(userId: Long): Flow<List<MarketplaceItemEntity>>

    @Query("SELECT * FROM marketplace_items WHERE title LIKE '%' || :query || '%' AND isSold = 0")
    fun searchItems(query: String): Flow<List<MarketplaceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MarketplaceItemEntity): Long

    @Update
    suspend fun updateItem(item: MarketplaceItemEntity)

    @Query("UPDATE marketplace_items SET isSold = 1 WHERE id = :id")
    suspend fun markAsSold(id: Long)

    @Delete
    suspend fun deleteItem(item: MarketplaceItemEntity)
}

@Dao
interface LostFoundDao {
    @Query("SELECT * FROM lost_found_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<LostFoundItemEntity>>

    @Query("SELECT * FROM lost_found_items WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getActiveItems(): Flow<List<LostFoundItemEntity>>

    @Query("SELECT * FROM lost_found_items WHERE type = :type AND isResolved = 0 ORDER BY createdAt DESC")
    fun getItemsByType(type: String): Flow<List<LostFoundItemEntity>>

    @Query("SELECT * FROM lost_found_items WHERE id = :id")
    fun getItemById(id: Long): Flow<LostFoundItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LostFoundItemEntity): Long

    @Update
    suspend fun updateItem(item: LostFoundItemEntity)

    @Query("UPDATE lost_found_items SET isResolved = 1 WHERE id = :id")
    suspend fun resolveItem(id: Long)
}

@Dao
interface OpportunityDao {
    @Query("SELECT * FROM opportunities ORDER BY deadline ASC")
    fun getAllOpportunities(): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE type = :type ORDER BY deadline ASC")
    fun getOpportunitiesByType(type: String): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE deadline >= :now ORDER BY deadline ASC")
    fun getUpcomingOpportunities(now: Long): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE title LIKE '%' || :query || '%' OR organization LIKE '%' || :query || '%'")
    fun searchOpportunities(query: String): Flow<List<OpportunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunity(opportunity: OpportunityEntity): Long

    @Update
    suspend fun updateOpportunity(opportunity: OpportunityEntity)

    @Delete
    suspend fun deleteOpportunity(opportunity: OpportunityEntity)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsByPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCount(postId: Long): Int
}

@Dao
interface AcademicRecordDao {
    @Query("SELECT * FROM academic_records WHERE userId = :userId ORDER BY subject ASC")
    fun getRecordsByUser(userId: Long): Flow<List<AcademicRecordEntity>>

    @Query("SELECT * FROM academic_records WHERE userId = :userId AND semester = :semester")
    fun getRecordsBySemester(userId: Long, semester: Int): Flow<List<AcademicRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AcademicRecordEntity): Long

    @Update
    suspend fun updateRecord(record: AcademicRecordEntity)

    @Delete
    suspend fun deleteRecord(record: AcademicRecordEntity)
}

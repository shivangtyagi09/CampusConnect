package com.example.campusconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campusconnect.data.local.dao.*
import com.example.campusconnect.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        AssignmentEntity::class,
        StudyMatchEntity::class,
        TeamRequestEntity::class,
        CampusFeedbackEntity::class,
        MarketplaceItemEntity::class,
        LostFoundItemEntity::class,
        OpportunityEntity::class,
        CommentEntity::class,
        AcademicRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun studyMatchDao(): StudyMatchDao
    abstract fun teamRequestDao(): TeamRequestDao
    abstract fun campusFeedbackDao(): CampusFeedbackDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun lostFoundDao(): LostFoundDao
    abstract fun opportunityDao(): OpportunityDao
    abstract fun commentDao(): CommentDao
    abstract fun academicRecordDao(): AcademicRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_connect_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

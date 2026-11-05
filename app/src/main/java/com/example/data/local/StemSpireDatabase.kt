package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ArtistProfileDao
import com.example.data.local.dao.ReviewFeedbackDao
import com.example.data.local.dao.TrackDao
import com.example.data.local.entity.ArtistProfileEntity
import com.example.data.local.entity.ReviewFeedbackEntity
import com.example.data.local.entity.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ArtistProfileEntity::class,
        TrackEntity::class,
        ReviewFeedbackEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StemSpireDatabase : RoomDatabase() {

    abstract fun artistProfileDao(): ArtistProfileDao
    abstract fun trackDao(): TrackDao
    abstract fun reviewFeedbackDao(): ReviewFeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: StemSpireDatabase? = null

        fun getInstance(context: Context): StemSpireDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StemSpireDatabase::class.java,
                    "stemspire_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

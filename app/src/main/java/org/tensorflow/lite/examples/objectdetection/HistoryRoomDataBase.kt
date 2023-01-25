package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryDao

// Annotates class to be a Room Database with a table (entity) of the History class
@Database(entities = arrayOf(History::class), version = 1, exportSchema = false)
public abstract class HistoryRoomDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HistoryRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope): HistoryRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryRoomDatabase::class.java,
                    "history_database"
                )
                .addCallback(HistoryDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class HistoryDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.historyDao())
                }
            }
        }

        suspend fun populateDatabase(historyDao: HistoryDao) {
            // Delete all content here.
            historyDao.deleteAll()

            // Sample entries
            var hist = History("2022-10-10", "AniCheck", "30mg/ml", "img2.png")
            historyDao.insert(hist)
            hist = History("2022-10-10", "AniCheck", "20mg/ml", "img1.png")
            historyDao.insert(hist)

            // TODO: Add your own words!
        }
    }
}
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
import org.tensorflow.lite.examples.objectdetection.adapter.Models
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsDao

// Annotates class to be a Room Database with a table (entity) of the History class
// Note that uninstalling the app doesn't remove the app data.
// manual deletion required, or you will get DB version miss-match error.
@Database(entities = [History::class, Models::class], version = 1)//, exportSchema = false)
abstract class HistoryRoomDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun modelsDao(): ModelsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HistoryRoomDatabase? = null

        fun getDatabase(
            context: Context,
            // Do I need to specify the scope?
            scope: CoroutineScope
        ): HistoryRoomDatabase {

            println("INSIDE getDatabase ------------------------------")
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryRoomDatabase::class.java,
                    "history_database"
                )
                .addCallback(HistoryDatabaseCallback(scope)) //
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class HistoryDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        // FIXME
        // The following method will overwrite existing DB.
        // I don't want this.
        // keeping it just for testing purpose
        override fun onCreate(db: SupportSQLiteDatabase) {
            println("INSIDE HistoryDatabaseCallback")
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
            // id = null, will be auto-generated
            val hist = History(null, "Test", 2022003, "20mg/ml", "2023-02-10")
            historyDao.insert(hist)
        }
    }
}
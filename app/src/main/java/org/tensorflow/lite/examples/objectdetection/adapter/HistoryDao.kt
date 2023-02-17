package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.room.*
import org.tensorflow.lite.examples.objectdetection.adapter.History
//import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_table ORDER BY date ASC")
    suspend fun getOrderedHistory(): List<History> // Flow<List<History>>??
//
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: History)
    @Delete
    suspend fun delete(history: History)

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()
}
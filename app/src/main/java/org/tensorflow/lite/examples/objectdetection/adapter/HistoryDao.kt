package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.tensorflow.lite.examples.objectdetection.adapter.History
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_table ORDER BY date ASC")
    fun getOrderedHistory(): Flow<List<History>>
//
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(history: History)
//
    @Query("DELETE FROM history_table")
    suspend fun deleteAll()
}
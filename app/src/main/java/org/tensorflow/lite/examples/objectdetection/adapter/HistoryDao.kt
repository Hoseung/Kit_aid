package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_table ORDER BY date ASC")
    fun getOrderedHistory(): Flow<List<History>> // Flow<List<History>>??
//
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: History)
    @Delete
    suspend fun delete(history: History)

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()

    @Query("SELECT id||','||date||','||product||','||lot||','||density FROM history_table")
    suspend fun exportToCSV(): List<String>
}
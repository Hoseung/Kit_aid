package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelsDao {
    @Query("SELECT * FROM models_table ORDER BY product ASC")
    fun getOrderedModels(): Flow<List<Models>>
    //
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(models: Models)

    @Query("SELECT * FROM models_table WHERE hash LIKE :searchQuery")
    suspend fun searchModels(searchQuery: String) : List<Models>

    @Delete
    suspend fun delete(models: Models)

    @Query("DELETE FROM models_table")
    suspend fun deleteAll()

}
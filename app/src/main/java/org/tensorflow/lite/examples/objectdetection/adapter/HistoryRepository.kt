package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    // getOrderedHistory() can't have 'suspend' prefix in definition.
    // WHY?
    val allHistorys: Flow<List<History>> = historyDao.getOrderedHistory()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(history: History) {
        historyDao.insert(history)
    }
}
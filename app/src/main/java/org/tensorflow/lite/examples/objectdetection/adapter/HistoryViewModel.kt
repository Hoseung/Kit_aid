package org.tensorflow.lite.examples.objectdetection.adapter

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class HistoryViewModel (private val itemDao: HistoryDao) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allHistorys: LiveData<List<History>> = itemDao.getOrderedHistory().asLiveData()//
    // repository.allHistorys.asLiveData()
   // val allHistorys = itemDao.getOrderedHistory()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(history: History) = viewModelScope.launch {
        itemDao.insert(history)
    }
}

class HistoryViewModelFactory(private val itemDao: HistoryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

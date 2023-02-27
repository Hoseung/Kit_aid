package org.tensorflow.lite.examples.objectdetection.adapter

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint

class ModelsViewModel (private val itemDao: ModelsDao) : ViewModel() {
    fun insert(models: Models) = viewModelScope.launch {
        itemDao.insert(models)
    }
    var currentUri : String = ""

    fun updateCalibUri(hash: String) = viewModelScope.launch {
        val matchedModels =
            itemDao.searchModels(hash)
        currentUri = Uri.parse(matchedModels[0].uri).toString()
        //MyEntryPoint.prefs.setString("uri", currentUri )
//        val uri: Uri = if(matchedModels.size == 1) {
//            Uri.parse(matchedModels[0].uri)
//        } else {
//            println("Warning: Model Hash crash!, taking the first one")
//            Uri.parse(matchedModels[0].uri)
//        }
//        return uri
    }
}

class ModelsViewModelFactory(private val itemDao: ModelsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModelsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModelsViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

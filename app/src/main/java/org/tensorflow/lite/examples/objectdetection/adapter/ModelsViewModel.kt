package org.tensorflow.lite.examples.objectdetection.adapter

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint

class ModelsViewModel (private val itemDao: ModelsDao) : ViewModel() {
    var currentCalibUri : String = ""
    var currentModelUri : String = ""

    fun insert(models: Models) = viewModelScope.launch {
        itemDao.insert(models)
    }

    fun getAll() = viewModelScope.launch { itemDao.getOrderedModels() }

    fun updateCalibUri(hash: String) = viewModelScope.launch {
        println("ModelsViewModelllll $hash")
        val matchedModels = itemDao.searchModels(hash)
        val nMatched = matchedModels.size
        if (nMatched>0){
            println("ModelsViewModelllll $nMatched")
            println("ModelsViewModelllll ${matchedModels[nMatched-1].uri}")
            println("ModelsViewModelllll ${Uri.parse(matchedModels[nMatched-1].uri)}")
            //currentCalibUri = Uri.parse(matchedModels[0].uri).toString()
            currentCalibUri = matchedModels[nMatched-1].uri!!
            MyEntryPoint.prefs.setString("CalibUri", matchedModels[nMatched-1].uri!!)
        }

    }

    fun updateModelUri(hash: String) = viewModelScope.launch {
        val matchedModels =
            itemDao.searchModels(hash)
        currentModelUri = Uri.parse(matchedModels[0].uri).toString()
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

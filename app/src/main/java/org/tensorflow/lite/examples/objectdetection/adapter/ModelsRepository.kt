package org.tensorflow.lite.examples.objectdetection.adapter

import android.net.Uri
import android.widget.Toast
import androidx.annotation.WorkerThread

class ModelsRepository(private val modelsDao: ModelsDao){
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(models: Models) {
        modelsDao.insert(models)
    }

    // Size of results is needed. searchModel therefore returns a List, NOT a Flow.
    suspend fun getUri(hash: String) : Uri {
        val matchedModels = modelsDao.searchModels(hash)
        val uri: Uri = if(matchedModels.size == 1) {
            Uri.parse(matchedModels[0].uri)
        } else {
            println("Warning: Model Hash crash!, taking the first one")
            Uri.parse(matchedModels[0].uri)
        }
        return uri
    }
}
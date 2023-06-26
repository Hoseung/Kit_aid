package org.tensorflow.lite.examples.objectdetection.firebaseML

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.firebase.storage.StorageReference
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint
import java.io.File

class ModelLib(context: Context) {
    private val context = context
    var downloadCompCount: Int = 0

    /**
     * Download file from firebase storage.
     * @param filePath The name of file in firebase. e.g AniCheck-bIgG.tflite or files/aaa.dat
     * @param storageRef firebase storage reference.
     * @param targetPath The path of file that will be downloaded. I will be saved to externalFiles.
     */
    fun getFileFromStorage(
        filePath: String,
        storageRef: StorageReference,
        downloadDialog: androidx.appcompat.app.AlertDialog,
        localFileList: Array<String>
    ) {
        val fileRef: StorageReference = storageRef.child(filePath)

        fileRef.downloadUrl.addOnSuccessListener {
            val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(it)
                .setTitle("download $filePath")
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // 나중에 주석처리 할 것
                .setAllowedOverMetered(true)
                .setDestinationInExternalFilesDir(context, "Models", filePath)


            Log.d("downloading", "$filePath -> download start!")

            (context as Activity).runOnUiThread{
                downloadDialog.setMessage("Downloading $filePath")
            }

            val downloadId = downloadManager.enqueue(request)
            MyEntryPoint.prefs.setString("downloadReady", "false")
            println("DownloadId: $downloadId")

            val onComplete = object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val referenceId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (referenceId == downloadId) {
                        // download is complete, do something
                        println("$filePath is downloaded!")
                        MyEntryPoint.prefs.setString("downloadReady", "true")

                        // delete old models
                        val endIDX = filePath.indexOf(".tflite")
                        val prodName = filePath.substring(0, endIDX-5)
                        localFileList.forEach {localFile ->
                            if (localFile.contains(prodName)) {
                                File(context?.getExternalFilesDir("Models"), localFile).delete()
                            }
                        }

                        // count completed files
                        downloadCompCount ++
                    }
                }

            }

            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            println("register receivers!")
        }
    }
}
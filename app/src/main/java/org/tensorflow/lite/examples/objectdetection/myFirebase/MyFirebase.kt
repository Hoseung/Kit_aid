package org.tensorflow.lite.examples.objectdetection.myFirebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint

class MyFirebase(gs:String, context: Context){
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val storage: FirebaseStorage = Firebase.storage(gs)
    val storageRef = storage.reference
    var latestLotNum: String? = null
    var totalFileList: MutableList<String>? = null

    /* dat list */
    var datListBIgG: MutableList<String> = mutableListOf()
    var datListIgE: MutableList<String> = mutableListOf()
    var datListIgG: MutableList<String> = mutableListOf()

    /* model list */
    var tfliteListDetection: MutableList<String> = mutableListOf()
    var tfliteListBIgG: MutableList<String> = mutableListOf()
    var tfliteListIgE: MutableList<String> = mutableListOf()
    var tfliteListIgG: MutableList<String> = mutableListOf()

    init {
        FirebaseApp.initializeApp(context)
    }

    fun checkAuth():Boolean{
        val currentUser = auth.currentUser
        return currentUser?.let{
            MyEntryPoint.email = currentUser.email
            if (currentUser.isEmailVerified) {
                println("user email verified")
                true
            } else {
                println("user email not verified")
                false
            }
        } ?: let {
            false
        }
    }

    fun getTotalFileList() {
        val listRef = storage.reference.child("/")

        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                Log.i("getTotalFileList", "get file from firebase successfully!")
                totalFileList = mutableListOf()

                items.forEach { item ->
                    totalFileList?.add(item.toString())
                }
            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
                Log.e("getTotalFileList", "fail to get file list from firebase storage...")
            }
    }

    fun arrangeFileList() {
        datListBIgG.clear()
        datListIgE.clear()
        datListIgG.clear()

        tfliteListBIgG.clear()
        tfliteListIgE.clear()
        tfliteListIgG.clear()
        tfliteListDetection.clear()

        totalFileList?.sortDescending()
        totalFileList?.forEach {
            val fileSplit = it.split("/")
            val fileName = fileSplit[fileSplit.size-1]

            if (it.toString().contains("AniCheck-bIgG")) {
                if (it.toString().contains(".dat")) {
                    val datIndex : Int = it.indexOf(".dat")
                    val lotNum = it.substring(datIndex-5, datIndex)
                    datListBIgG.add(lotNum)
                }
                else if (it.toString().contains(".tflite")) tfliteListBIgG.add(fileName)

            } else if (it.toString().contains("ImmuneCheck-IgE")) {
                if (it.toString().contains(".dat")) {
                    val datIndex : Int = it.indexOf(".dat")
                    val lotNum = it.substring(datIndex-5, datIndex)
                    datListIgE.add(lotNum)
                }
                else if (it.toString().contains(".tflite")) tfliteListIgE.add(fileName)

            } else if (it.toString().contains("ImmuneCheck-IgG")) {
                if (it.toString().contains(".dat")) {
                    val datIndex : Int = it.indexOf(".dat")
                    val lotNum = it.substring(datIndex-5, datIndex)
                    datListIgG.add(lotNum)
                }
                else if (it.toString().contains(".tflite")) tfliteListIgG.add(fileName)

            } else if (it.toString().contains("detection")) {
                tfliteListDetection.add(fileName)
            }
        }
    }
}
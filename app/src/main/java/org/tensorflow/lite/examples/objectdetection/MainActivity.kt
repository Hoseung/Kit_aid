/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.objectdetection

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog.show
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ColorSpace.Model
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.examples.objectdetection.adapter.*
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMainBinding
import org.tensorflow.lite.examples.objectdetection.databinding.DownloadProgressDialog2Binding
import org.tensorflow.lite.examples.objectdetection.firebaseML.ModelLib
import java.io.BufferedReader
import java.io.File
import java.io.OutputStreamWriter

/**
 * Todo: No, I won't follow the single-activity pattern!!!
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private var loading: Boolean = true
    private var downloadReady: Boolean = false
    private var localModelList: Array<String> = arrayOf<String>("", "", "", "") // D, BIgG, IgE, IgG
    private var prodName = MyEntryPoint.prefs.getString("prodName", "AniCheck-bIgG")
    private var lotNum = MyEntryPoint.prefs.getString("lotNum", "00000")
    private var downloadList: MutableList<String> = mutableListOf()
    private val modelLib = ModelLib(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // activate loadinglayout
        activityMainBinding.loadingLayout.visibility = View.VISIBLE


        // copy asset calibration file to in-app repo ----------------------------------------------
        val modelInfo = File(applicationContext.filesDir, "model_info.dat")
        var inputFile = assets.open("${prodName}_00000.dat")
        var readStream: BufferedReader = inputFile.reader().buffered()


        // if modelInfo -> read prodName and lotNum
        if (modelInfo.exists()){
            val modelInfoReader: BufferedReader = modelInfo.reader().buffered()
            var i = 0
            modelInfoReader.forEachLine {
                if (i==0) {
                    prodName = it
                } else {
                    lotNum = it
                }
                i ++
            }
            val inputFile2 = File(
                applicationContext.filesDir, "lastCalib.dat")
            readStream = inputFile2.reader().buffered()
            MyEntryPoint.prefs.setString("prodName", "$prodName")
            MyEntryPoint.prefs.setString("lotNum", "$lotNum")
        } else {
            println("modelInfo nothing...")
        }
        println("removeQR: ${MyEntryPoint.prefs.getString("removeQR", "???")}")
        println("prodname: $prodName, lotnum: $lotNum")
        val outputFile = File(applicationContext.filesDir, "${prodName}_${lotNum}.dat")
        val writeStream: OutputStreamWriter = outputFile.writer()
        println("$outputFile is open...")
        readStream.forEachLine {
            writeStream.write("$it\n")
        }
        writeStream.flush()
        MyEntryPoint.prefs.setString("CalibUri", "${outputFile.toURI()}")
        println("write comp!!! model file nothing...")

        if (allPermissionsGranted()) {
            //startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        //startActivity(Intent(this, LoginActivity::class.java))
        initView()

        activityMainBinding.examineHistoryButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        activityMainBinding.kitListButton.setOnClickListener {
            startActivity(Intent(this, SelectActivity::class.java))
        }

        // go to homepage
        activityMainBinding.goToHomepage.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.proteometech.com/main"))
            startActivity(browserIntent)
        }

        // setting activity
        MyEntryPoint.prefs.setString("calibOn", "true")
        activityMainBinding.versionText.setOnClickListener{
            val settingIntent = Intent(this, SettingActivity::class.java)
            startActivity(settingIntent)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun initView()// = with(activityMainBinding)
    {
        val productTextView = activityMainBinding.loginProductName
        val lotTextView = activityMainBinding.loginLotNumber
        val myPrdName = String.format(" %s", MyEntryPoint.prefs.getString("prodName", "AniCheck-BIgG"))
        productTextView.text = myPrdName
        lotTextView.text = String.format(" %s", MyEntryPoint.prefs.getString("lotNum", "BIG22003"))

        // Todo: retain last sessions' choice
//        productTextView.text = MyEntryPoint.prefs.getString("prodName", "PRODUCT NAME")
//        lotTextView.text = MyEntryPoint.prefs.getString("lotNum", "LOT NUMBER")


        activityMainBinding.captureKit.setOnClickListener {
                startActivity(Intent(this, CameraActivity::class.java))
        }
        activityMainBinding.scanQRButton.setOnClickListener {
            startActivity(Intent(this, QrActivity::class.java))
            // Todo: can I move this somewhere else and not use GlobalScope?
//            GlobalScope.launch{
//                MyEntryPoint.prefs.setString("uri",
//                    modelsRepo.getUri(MyEntryPoint.prefs.getString("hash", "abc123")).toString())
//            }

//            CoroutineScope(Dispatchers.IO).launch {
//                modelsViewModel.updateCalibUri(MyEntryPoint.prefs.getString("hash", "abc123"))
//            }

            //finish() // todo: finish가 하는 일은?
        }

    }
//    private fun loadLocalModels(){
//        // todo: 임시. ASSET을 internal storage에 저장
//        //val modelCalibration = "AniCheck-bIgG_BIG23006.dat"
//        val prodName = MyEntryPoint.prefs.getString("prodName", "Bovine-IgG")
//        val lotNum = MyEntryPoint.prefs.getString("lotNum", "2022003")
//        val date = MyEntryPoint.prefs.getString("date", "20231225")
//        val hash = MyEntryPoint.prefs.getString("hash", "abcdefg123")
//
//        val modelCalibration = "${prodName}_${lotNum}.dat"
//
//        val copiedFile = File(applicationContext.filesDir, modelCalibration)
//        applicationContext.assets.open(modelCalibration).use { input ->
//            copiedFile.outputStream().use { output ->
//                input.copyTo(output, 1024)
//            }
//        }
//        // Get Uri of the file
//        val copiedURI = copiedFile.toURI()
//        modelsViewModel.insert(
//            Models(null, prodName, lotNum.toInt(), date, hash, copiedURI.toString())
//        )
//
////        val modelPredict = "230213_new_regression_float16.tflite"
////        modelsViewModel.insert(
////            Models(null, "Bovine_IgG", 20230009, "20230226","a3dcex2745", Uri.fromFile(File(modelPredict)).toString())
////        )
//
//        // ToDo: modelsViewModel.updateCalibUri(hash) 하기 전엔 modelViewMdoel은 빈 string.
//        modelsViewModel.updateCalibUri(hash)
//    }


    //    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        finishAffinity()
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
//            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
//            // (https://issuetracker.google.com/issues/139738913)
//        } else {
//            super.onBackPressed()
//        }
//    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit").setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }.setNegativeButton("No") { _, _ ->
//                alertDialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

    }
//    companion object {
//        /** Use external media if it is available, our app's file directory otherwise */
//        fun getOutputDirectory(context: Context): File {
//            val appContext = context.applicationContext
//            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
//                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
//            return if (mediaDir != null && mediaDir.exists())
//                mediaDir else appContext.filesDir
//        }
//    }
    companion object {
        private const val TAG = "Kit-Aid"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).toTypedArray()
}

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initView()
        loading = true
        downloadList.clear()
        downloadReady = false
        modelLib.downloadCompCount = 0

        // get firebase model list
        MyEntryPoint.myFirebase.getTotalFileList()
        Thread {
            while (loading) {
                if (MyEntryPoint.myFirebase.totalFileList != null) {
                    println("getting Firebase file list...")
                    MyEntryPoint.myFirebase.arrangeFileList()
                    loading = false
                }
                Thread.sleep(100)
            }
        }.start()

        // update variable
        prodName = MyEntryPoint.prefs.getString("prodName", "AniCheck-bIgG")
        lotNum = MyEntryPoint.prefs.getString("lotNum", "00000")

        // get device file list
        val modelDir = File(getExternalFilesDir("Models"), "")
        val tfliteListFromDevice = modelDir.listFiles()

        tfliteListFromDevice.forEach {
            val fileSplit = it.toString().split("/")
            val fileName = fileSplit[fileSplit.size - 1]

            if (it.toString().contains("detection")) {
                localModelList[0] = fileName
            } else if (it.toString().contains("AniCheck-bIgG")) {
                localModelList[1] = fileName
            } else if (it.toString().contains("ImmuneCheck-IgE")) {
                localModelList[2] = fileName
            } else if (it.toString().contains("ImmuneCheck-IgG")) {
                localModelList[3] = fileName
            }
        }

        println("local file list:${localModelList.toList()}} + loading: $loading")

        // compare local file to firebase storage file
        Thread {
            while (true) {
                if (!loading) {
                    println("start comparing local and cloud...")

                    // if storage tf detection file is nothing -> call Jungyeon Kim ^^
                    addToDownloadList(
                        MyEntryPoint.myFirebase.tfliteListDetection,
                        localModelList[0],
                        "There is no AI model for kit detection. Please contact manager."
                    )

                    // check for regression file on server
                    if (prodName.lowercase().contains("bigg")) {
                        addToDownloadList(
                            MyEntryPoint.myFirebase.tfliteListBIgG,
                            localModelList[1],
                            "There is no AI model for AniCheck-BIgG regression on server." +
                                    " Please contact manager."
                        )
                    } else if (prodName.lowercase().contains("-ige")) {
                        addToDownloadList(
                            MyEntryPoint.myFirebase.tfliteListIgE,
                            localModelList[2],
                            "There is no AI model for ImmuneCheck-IgE " +
                                    "regression on server. Please contact manager."
                        )
                    } else if (prodName.lowercase().contains("-igg")) {
                        addToDownloadList(
                            MyEntryPoint.myFirebase.tfliteListIgG,
                            localModelList[3],
                            "There is no AI model for ImmuneCheck-IgG " +
                                    "regression on server. Please contact manager."
                        )
                    }
                    println(prodName)
                    println("download list: $downloadList")
                    downloadReady = true
                    break
                }
                Thread.sleep(1000)
            }
        }.start()


        // Download files
        Thread {
            var fileSize = 0
            while (true) {
                if (downloadReady) {
                    // calculate file size
                    downloadList.forEach {
                        if (it.contains("detection")) {
                            fileSize += 8
                        } else {
                            fileSize += 45
                        }
                    }

                    // break the loop. because it will not working eventually... ^o^
                    if (!activityMainBinding.captureKit.isEnabled) break
                    else {
                        runOnUiThread{

                            // disable progressbar layout
                            activityMainBinding.loadingLayout.visibility = View.GONE

                            if (downloadList.isNotEmpty()) {
                                androidx.appcompat.app.AlertDialog.Builder(this).run {
                                    setIcon(android.R.drawable.ic_dialog_info)
                                    setTitle("New AI Update")
                                    setMessage("We are planning to download an AI model. " +
                                            "If you're not in a Wi-Fi environment, a significant amount of data may be consumed. " +
                                            "Would you like to continue? \n\n * Size: $fileSize MB")
                                    setNegativeButton("CANCEL", null)
                                    setPositiveButton("OK", object: DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {


                                            // Alert Dialog when downloading...
                                            val dialog2Binding = DownloadProgressDialog2Binding
                                                .inflate(layoutInflater, null, false)

                                            var downloadDialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                                                .run{
                                                    setView(dialog2Binding.root)
                                                    setMessage("Downloading...")
                                                    show()
                                                }
                                            downloadDialog.setCanceledOnTouchOutside(false)

                                            // set progress dialog
                                            downloadList.forEach {
                                                modelLib.getFileFromStorage(
                                                    it, MyEntryPoint.myFirebase.storageRef,
                                                    downloadDialog,
                                                    localModelList
                                                )
                                            }


                                            // turn off dialog
                                            Thread{
                                                while(true) {
                                                    if (modelLib.downloadCompCount == downloadList.size) {
                                                        runOnUiThread{
                                                            downloadDialog.dismiss()
                                                        }
                                                    }
                                                    Thread.sleep(1000)
                                                }
                                            }.start()
                                            dialog?.dismiss()
                                        }
                                    })
                                    show()
                                }
                            }
                        }
                    }
                    println("total file size: $fileSize")
                    downloadReady = false
                    break
                }
                Thread.sleep(1000)
            }
        }.start()


    }

    private fun addToDownloadList(checkList:MutableList<String>, localFileName:String, message:String) {
        if (checkList.isEmpty()) {
            runOnUiThread{
                androidx.appcompat.app.AlertDialog.Builder(this).run {
                    setTitle("No model on server")
                    setMessage(message)
                    setPositiveButton("OK", null)
                    show()
                }

                // disable capture kit button on main activity
                activityMainBinding.captureKit.background = ContextCompat.getDrawable(
                    this, R.drawable.rounded_rectangle_gray
                )
                activityMainBinding.captureKit.isEnabled = false
            }
        } else {
            println("1) localfileName:$localFileName")
            println("2) checklist: ${checkList[0]}")
            if (localFileName == "" ||
                localFileName != checkList[0]) {
                downloadList.add(checkList[0])
            }

            runOnUiThread {
                // enable capture kit button on main activity
                activityMainBinding.captureKit.background = ContextCompat.getDrawable(
                    this, R.drawable.rounded_rectangle_lightgreen
                )
                activityMainBinding.captureKit.isEnabled = true
            }
        }
    }
}
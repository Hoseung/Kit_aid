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
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.examples.objectdetection.adapter.*
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMainBinding
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
//    private val modelsViewModel: ModelsViewModel by viewModels {
//        ModelsViewModelFactory((application as MyEntryPoint).database.modelsDao())
//           }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // copy asset calibration file to in-app repo ----------------------------------------------
        val modelInfo = File(applicationContext.filesDir, "model_info.dat")
        var prodName = MyEntryPoint.prefs.getString("prodName", "AniCheck-bIgG")
        var lotNum = MyEntryPoint.prefs.getString("lotNum", "00000")
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
            println("point1111111111111")
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
        // -----------------------------------------------------------------------------------------

        /*
        Todo:
        It's not desirable to request permission until it is absolutely needed (right before you
        take pictures)
        But... I don't want to mess up with Permission Fragment and its interaction with activities.
         */
        if (allPermissionsGranted()) {
            //startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        //startActivity(Intent(this, LoginActivity::class.java))
        initView()

        // go to homepage
        activityMainBinding.goToHomepage.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.proteometech.com/main"))
            startActivity(browserIntent)
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


        activityMainBinding.loginButton.setOnClickListener {
//            if (idEditText.text.toString() == "user" && pwEditText.text.toString() == "1234") {
                //loadLocalModels()
                startActivity(Intent(this, CameraActivity::class.java))
//            } else {
//                Toast.makeText(this@LoginActivity, "아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT)
//                    .show()
//            }
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
    }
}



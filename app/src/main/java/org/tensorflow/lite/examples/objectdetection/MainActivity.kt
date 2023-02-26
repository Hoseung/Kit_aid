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
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMainBinding

/**
 * Todo: No, I won't follow the single-activity pattern!!!
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

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
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun initView()// = with(activityMainBinding)
    {
        val productTextView = findViewById<TextView>(R.id.loginProductName)
        val lotTextView = findViewById<TextView>(R.id.lotNumber)
        // Todo: retain last sessions' choice
//        productTextView.text = MyEntryPoint.prefs.getString("prodName", "PRODUCT NAME")
//        lotTextView.text = MyEntryPoint.prefs.getString("lotNum", "LOT NUMBER")

        activityMainBinding.loginButton.setOnClickListener {
//            if (idEditText.text.toString() == "user" && pwEditText.text.toString() == "1234") {
                startActivity(Intent(this, CameraActivity::class.java))
//            } else {
//                Toast.makeText(this@LoginActivity, "아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT)
//                    .show()
//            }
        }
        activityMainBinding.scanQRButton.setOnClickListener {
            startActivity(Intent(this, QrActivity::class.java))
            //finish() // todo: finish가 하는 일은?
        }
    }

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
}



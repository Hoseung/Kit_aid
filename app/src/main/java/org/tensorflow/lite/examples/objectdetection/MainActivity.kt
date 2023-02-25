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

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMainBinding
import org.tensorflow.lite.examples.objectdetection.new.LoginActivity
import java.io.File

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
        startActivity(Intent(this, LoginActivity::class.java))
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
        } else {
            super.onBackPressed()
        }
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
}



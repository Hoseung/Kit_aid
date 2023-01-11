package org.tensorflow.lite.examples.objectdetection

import android.app.Application

class MyEntryPoint : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}
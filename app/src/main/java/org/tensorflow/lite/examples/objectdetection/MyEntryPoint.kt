package org.tensorflow.lite.examples.objectdetection

import android.app.Application
//import org.tensorflow.lite.examples.objectdetection.HistoryRoomDatabase
//import org.tensorflow.lite.examples.objectdetection.HistoryRoomRepository


class MyEntryPoint : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    //val database by lazy { HistoryRoomDatabase.getDatabase(this) }
   // val repository by lazy { HistoryRepository(database.wordDao()) }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}

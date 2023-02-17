package org.tensorflow.lite.examples.objectdetection

import android.app.Application
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.SupervisorJob


class MyEntryPoint : Application() {

    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var database: HistoryRoomDatabase
    }

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    //val applicationScope = CoroutineScope(SupervisorJob())
    //val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
    //val repository by lazy { HistoryRepository(database.historyDao()) }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
        database = HistoryRoomDatabase.getDatabase(this)

    }
}

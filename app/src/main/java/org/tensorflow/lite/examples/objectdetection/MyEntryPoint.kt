package org.tensorflow.lite.examples.objectdetection

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryRepository


class MyEntryPoint : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { HistoryRepository(database.historyDao()) }
    companion object {
        lateinit var prefs: PreferenceUtil
        //lateinit var database: HistoryRoomDatabase

    }

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    //val applicationScope = CoroutineScope(SupervisorJob())
    //val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }


    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
        //database = HistoryRoomDatabase.getDatabase(this, applicationScope)

    }
}

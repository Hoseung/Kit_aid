package org.tensorflow.lite.examples.objectdetection

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryRepository


class MyEntryPoint : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
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
        prefs.setString("prodName", "AniCheck-bIgG")
        prefs.setString("lotNum", "BIG22003")
        prefs.setString("date", "20231225")
        prefs.setString("hash", "abcdefg123")
        super.onCreate()
        //database = HistoryRoomDatabase.getDatabase(this, applicationScope)

    }
}

package org.tensorflow.lite.examples.objectdetection

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryRepository
import org.tensorflow.lite.examples.objectdetection.myFirebase.MyFirebase


class MyEntryPoint : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { HistoryRepository(database.historyDao()) }
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var myFirebase: MyFirebase
        var email: String? = null

        //lateinit var database: HistoryRoomDatabase

    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        prefs.setString("prodName", "AniCheck-bIgG")
        prefs.setString("lotNum", "00000")
        prefs.setString("date", "20231225")
        prefs.setString("hash", "abcdefg123")
        prefs.setString("CalibUri", "-")

        // user info
        prefs.setString("email", "")
        prefs.setString("password", "")
        super.onCreate()

        // successful camera capture info
        prefs.setCnt("count1", 0)
        prefs.setCnt("count2", 0)


        // QR
        prefs.setString("removeQR", "false")

        // firebase
        myFirebase = MyFirebase("gs://kitaid.appspot.com/", applicationContext)

        // download
        prefs.setString("downloadReady", "true")


    }
}

package org.tensorflow.lite.examples.objectdetection

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryRepository


class MyEntryPoint : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { HistoryRepository(database.historyDao()) }
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var auth: FirebaseAuth
        lateinit var storage: FirebaseStorage
        var email: String? = null
        fun checkAuth():Boolean{
            val currentUser = auth.currentUser
            return currentUser?.let{
                email = currentUser.email
                if (currentUser.isEmailVerified) {
                    println("user email verified")
                    true
                } else {
                    println("user email not verified")
                    false
                }
            } ?: let {
                false
            }
        }
        //lateinit var database: HistoryRoomDatabase

    }

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    //val applicationScope = CoroutineScope(SupervisorJob())
    //val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }

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

        auth = Firebase.auth
        //database = HistoryRoomDatabase.getDatabase(this, applicationScope)

    }
}

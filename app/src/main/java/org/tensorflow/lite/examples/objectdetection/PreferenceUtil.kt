package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getCnt(key:String, value:Int): Int{
        return prefs.getInt(key, 0)
    }

    fun setCnt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun increaseOneCnt(key:String) {
        prefs.edit().putInt(key, getCnt(key, 0) + 1).apply()
    }

}
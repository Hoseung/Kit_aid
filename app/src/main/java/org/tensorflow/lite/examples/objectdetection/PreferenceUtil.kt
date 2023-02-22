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

    fun getCnt(): Int{
        return prefs.getInt("cnt", 0)
    }

    fun setCnt(cnt: Int) {
        prefs.edit().putInt("cnt", cnt).apply()
    }

}
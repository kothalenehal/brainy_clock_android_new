package com.brainyclockuser.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The type Shared pref utils.
 *
 * @author Nishant Srivastava
 */
@Singleton
class PrefUtils @Inject constructor() {
    private val PREF_APP = "pref_app"

    /**
     * Gets boolean data.
     *
     * @param context the context
     * @param key     the key
     * @return the boolean data
     */
    fun getBooleanData(context: Context, key: String?): Boolean {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getBoolean(key, false)
    }

    /**
     * Gets int data.
     *
     * @param context the context
     * @param key     the key
     * @return the int data
     */
    fun getIntData(context: Context, key: String?): Int {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getInt(key, 0)
    }

    /**
     * Gets string data.
     *
     * @param context the context
     * @param key     the key
     * @return the string data
     */
    // Get Data
    fun getStringData(context: Context, key: String?): String? {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString(key, null)
    }


    /**
     * Gets long data.
     *
     * @param context the context
     * @param key     the key
     * @return the long data
     */
    // Get Data
    fun getLongData(context: Context, key: String?): Long {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getLong(key, 0)
    }

    /**
     * Save data.
     *
     * @param context the context
     * @param key     the key
     * @param val     the val
     */
    // Save Data
    fun saveData(context: Context, key: String?, `val`: String?) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString(key, `val`)
            .apply()
    }

    /**
     * Save data.
     *
     * @param context the context
     * @param key     the key
     * @param val     the val
     */
    fun saveData(context: Context, key: String?, `val`: Int) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putInt(key, `val`)
            .apply()
    }

    /**
     * Save data.
     *
     * @param context the context
     * @param key     the key
     * @param val     the val
     */
    fun saveData(context: Context, key: String?, `val`: Long) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putLong(key, `val`)
            .apply()
    }

    /**
     * Save data.
     *
     * @param context the context
     * @param key     the key
     * @param val     the val
     */
    fun saveData(context: Context, key: String?, `val`: Boolean) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, `val`)
            .apply()
    }

    fun getSharedPrefEditor(context: Context, pref: String?): SharedPreferences.Editor {
        return context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit()
    }

    fun saveData(editor: SharedPreferences.Editor) {
        editor.apply()
    }

    fun clearData(context: Context) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
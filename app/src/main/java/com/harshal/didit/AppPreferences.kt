package com.harshal.didit

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_APP_VERSION = "app_version"
    private const val KEY_WHATS_NEW_SHOWN = "whats_new_shown"
    private const val CURRENT_VERSION = 5 // Increment this for future updates
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun getAppVersion(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_APP_VERSION, 1)
    }
    
    fun setAppVersion(context: Context, version: Int) {
        getSharedPreferences(context).edit()
            .putInt(KEY_APP_VERSION, version)
            .apply()
    }
    
    fun isWhatsNewShown(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_WHATS_NEW_SHOWN, false)
    }
    
    fun setWhatsNewShown(context: Context, shown: Boolean) {
        getSharedPreferences(context).edit()
            .putBoolean(KEY_WHATS_NEW_SHOWN, shown)
            .apply()
    }
    
    fun shouldShowWhatsNew(context: Context): Boolean {
        val currentVersion = getAppVersion(context)
        val whatsNewShown = isWhatsNewShown(context)
        
        // Show what's new if:
        // 1. App version has been updated, OR
        // 2. What's new hasn't been shown yet for current version
        return currentVersion < CURRENT_VERSION || !whatsNewShown
    }
    
    fun markUpdateAsSeen(context: Context) {
        setAppVersion(context, CURRENT_VERSION)
        setWhatsNewShown(context, true)
    }
}
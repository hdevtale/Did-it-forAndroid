package com.harshal.didit

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePreferences {
    private const val PREF_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_THEME_SELECTED = "theme_selected"

    fun saveThemeMode(context: Context, themeMode: Int) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }

    fun getThemeMode(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun setThemeSelected(context: Context, selected: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_THEME_SELECTED, selected).apply()
    }

    fun isThemeSelected(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_THEME_SELECTED, false)
    }

    fun applySavedTheme(context: Context) {
        val themeMode = getThemeMode(context)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }
    
    // Method to reset theme selection (for testing purposes)
    fun resetThemeSelection(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_THEME_SELECTED, false).apply()
    }
    
    // Method to manually override theme (for user preference)
    fun setManualThemeMode(context: Context, themeMode: Int) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt(KEY_THEME_MODE, themeMode)
            .putBoolean(KEY_THEME_SELECTED, true)
            .apply()
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }
    
    // Check if current theme mode is following system
    fun isFollowingSystemTheme(context: Context): Boolean {
        return getThemeMode(context) == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
package com.harshal.didit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user has already selected a theme
        if (ThemePreferences.isThemeSelected(this)) {
            // User has already selected a theme, go directly to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // First time user, show theme selection
            val intent = Intent(this, ThemeSelectionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
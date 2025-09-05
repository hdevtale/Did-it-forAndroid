package com.harshal.didit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.harshal.didit.databinding.ActivityThemeSelectionBinding

class ThemeSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Light theme option
        binding.lightThemeCard.setOnClickListener {
            setThemeAndProceed(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Dark theme option
        binding.darkThemeCard.setOnClickListener {
            setThemeAndProceed(AppCompatDelegate.MODE_NIGHT_YES)
        }

        // Device theme option
        binding.deviceThemeCard.setOnClickListener {
            setThemeAndProceed(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setThemeAndProceed(themeMode: Int) {
        // Save the theme preference
        ThemePreferences.saveThemeMode(this, themeMode)
        
        // Apply the theme immediately
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        // Mark that user has made their choice
        ThemePreferences.setThemeSelected(this, true)
        
        // Launch main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
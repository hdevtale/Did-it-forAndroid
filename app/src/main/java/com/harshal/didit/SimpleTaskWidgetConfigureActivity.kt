package com.harshal.didit

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.harshal.didit.databinding.ActivitySimpleTaskWidgetConfigureBinding

class SimpleTaskWidgetConfigureActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySimpleTaskWidgetConfigureBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var taskSpinner: Spinner
    private lateinit var ongoingTasks: List<Task>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED initially
        setResult(Activity.RESULT_CANCELED)
        
        binding = ActivitySimpleTaskWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Find the widget id from the intent
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        
        // If this activity was started with an intent without an app widget ID, finish with an error
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setupUI()
        loadTasks()
    }
    
    private fun setupUI() {
        taskSpinner = binding.taskSpinner
        
        binding.configureButton.setOnClickListener {
            configureWidget()
        }
        
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadTasks() {
        try {
            val allTasks = TaskRepository.loadTasks(this)
            ongoingTasks = allTasks.filter { !it.isCompleted }
            
            if (ongoingTasks.isEmpty()) {
                binding.noTasksMessage.visibility = View.VISIBLE
                binding.taskSpinner.visibility = View.GONE
                binding.configureButton.isEnabled = false
                binding.configureButton.text = "No Tasks Available"
            } else {
                binding.noTasksMessage.visibility = View.GONE
                binding.taskSpinner.visibility = View.VISIBLE
                binding.configureButton.isEnabled = true
                binding.configureButton.text = "Add Widget"
                
                // Create adapter for spinner
                val taskNames = ongoingTasks.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, taskNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                taskSpinner.adapter = adapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.noTasksMessage.visibility = View.VISIBLE
            binding.taskSpinner.visibility = View.GONE
            binding.configureButton.isEnabled = false
            binding.configureButton.text = "Error Loading Tasks"
        }
    }
    
    private fun configureWidget() {
        try {
            val selectedPosition = taskSpinner.selectedItemPosition
            if (selectedPosition >= 0 && selectedPosition < ongoingTasks.size) {
                val selectedTask = ongoingTasks[selectedPosition]
                
                // Save the selected task ID for this widget
                saveWidgetTaskId(appWidgetId, selectedTask.id.toString())
                
                // Update the widget
                val appWidgetManager = AppWidgetManager.getInstance(this)
                SimpleTaskWidget.updateWidget(this, appWidgetManager, appWidgetId)
                
                // Make sure we pass back the original appWidgetId
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun saveWidgetTaskId(appWidgetId: Int, taskId: String) {
        val prefs = getSharedPreferences("widget_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("widget_task_$appWidgetId", taskId).apply()
    }
    
    companion object {
        fun getWidgetTaskId(context: Context, appWidgetId: Int): String? {
            val prefs = context.getSharedPreferences("widget_preferences", Context.MODE_PRIVATE)
            return prefs.getString("widget_task_$appWidgetId", null)
        }
    }
}

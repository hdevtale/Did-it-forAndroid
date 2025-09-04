package com.harshal.didit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import android.util.Log
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.VibratorManager

class SimpleWidgetLogReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SimpleWidgetLogReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "SimpleWidgetLogReceiver received intent: ${intent.action}")
        
        if (intent.action == SimpleTaskWidget.ACTION_LOG_TIME) {
            try {
                val taskId = intent.getStringExtra("taskId")
                val appWidgetId = intent.getIntExtra("appWidgetId", -1)
                
                Log.d(TAG, "Logging time for task: $taskId, widget: $appWidgetId")
                
                if (taskId != null && appWidgetId != -1) {
                    // Update the task's last logged timestamp
                    val tasks = TaskRepository.loadTasks(context).toMutableList()
                    val taskIndex = tasks.indexOfFirst { it.id.toString() == taskId }
                    
                    if (taskIndex != -1) {
                        val task = tasks[taskIndex]
                        tasks[taskIndex] = task.copy(
                            lastLoggedTimestamp = System.currentTimeMillis()
                        )
                        TaskRepository.saveTasks(context, tasks)
                        
                        // Haptic feedback for widget interaction
                        try {
                            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(50)
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Haptic feedback not available", e)
                        }
                        
                        Log.d(TAG, "Task timestamp updated successfully")
                        
                        // Update the widget
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        SimpleTaskWidget().onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
                        
                        Log.d(TAG, "Widget updated after logging time")
                    } else {
                        Log.e(TAG, "Task not found with ID: $taskId")
                    }
                } else {
                    Log.e(TAG, "Missing taskId or appWidgetId in intent")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in SimpleWidgetLogReceiver", e)
            }
        }
    }
}

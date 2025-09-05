package com.harshal.didit

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class NotificationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getStringExtra("task_id") ?: ""
        val taskName = intent.getStringExtra("task_name") ?: "Task"
        val taskNotes = intent.getStringExtra("task_notes")
        
        try {
            when (action) {
                "LOG_TIME" -> {
                    logTimeForTask(context, taskId, taskName)
                }
                "SNOOZE" -> {
                    snoozeReminder(context, taskId, taskName, taskNotes)
                }
                "MARK_DONE" -> {
                    markTaskAsDone(context, taskId, taskName)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationActionReceiver", "Error handling action $action: ${e.message}")
            e.printStackTrace()
        }
        
        // Dismiss the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskName.hashCode())
    }
    
    private fun logTimeForTask(context: Context, taskId: String, taskName: String) {
        try {
            android.util.Log.d("NotificationActionReceiver", "Logging time for task: $taskName, ID: $taskId")
            
            // Load all tasks
            val allTasks = TaskRepository.loadTasks(context)
            android.util.Log.d("NotificationActionReceiver", "Loaded ${allTasks.size} tasks")
            
            // Find the task by ID (try both string comparison and UUID parsing)
            val task = allTasks.find { 
                it.id.toString() == taskId || 
                try { it.id == UUID.fromString(taskId) } catch (e: Exception) { false }
            }
            
            if (task != null) {
                android.util.Log.d("NotificationActionReceiver", "Found task: ${task.name}, current lastLogged: ${task.lastLoggedTimestamp}")
                
                // Update the task with new log time
                val updatedTask = task.copy(
                    lastLoggedTimestamp = System.currentTimeMillis()
                )
                
                // Update the task in the list
                val updatedTasks = allTasks.map { if (it.id == task.id) updatedTask else it }
                
                // Save the updated tasks
                TaskRepository.saveTasks(context, updatedTasks)
                
                android.util.Log.d("NotificationActionReceiver", "Time logged successfully for task: $taskName")
            } else {
                android.util.Log.w("NotificationActionReceiver", "Task not found: $taskName with ID: $taskId")
                // Log all task IDs for debugging
                allTasks.forEach { android.util.Log.d("NotificationActionReceiver", "Available task: ${it.name}, ID: ${it.id}") }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationActionReceiver", "Error logging time: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun snoozeReminder(context: Context, taskId: String, taskName: String, taskNotes: String?) {
        try {
            android.util.Log.d("NotificationActionReceiver", "Snoozing reminder for task: $taskName, ID: $taskId")
            
            // Load all tasks
            val allTasks = TaskRepository.loadTasks(context)
            
            // Find the task by ID (try both string comparison and UUID parsing)
            val task = allTasks.find { 
                it.id.toString() == taskId || 
                try { it.id == UUID.fromString(taskId) } catch (e: Exception) { false }
            }
            
            if (task != null) {
                // Schedule a new reminder for 15 minutes from now
                val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
                
                // Create intent for the snoozed reminder
                val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("task_id", taskId)
                    putExtra("task_name", taskName)
                    putExtra("task_notes", taskNotes)
                }
                
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId.hashCode() + 1000, // Different ID to avoid conflicts
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Schedule the snoozed reminder
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    snoozePendingIntent
                )
                
                android.util.Log.d("NotificationActionReceiver", "Reminder snoozed for 15 minutes: $taskName, next reminder at: $snoozeTime")
            } else {
                android.util.Log.w("NotificationActionReceiver", "Task not found for snooze: $taskName with ID: $taskId")
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationActionReceiver", "Error snoozing reminder: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun markTaskAsDone(context: Context, taskId: String, taskName: String) {
        try {
            android.util.Log.d("NotificationActionReceiver", "Marking task as done: $taskName, ID: $taskId")
            
            // Load all tasks
            val allTasks = TaskRepository.loadTasks(context)
            
            // Find the task by ID (try both string comparison and UUID parsing)
            val task = allTasks.find { 
                it.id.toString() == taskId || 
                try { it.id == UUID.fromString(taskId) } catch (e: Exception) { false }
            }
            
            if (task != null) {
                android.util.Log.d("NotificationActionReceiver", "Found task: ${task.name}, current status: ${task.isCompleted}")
                
                // Mark the task as completed
                val completedTask = task.copy(
                    isCompleted = true,
                    completedTimestamp = System.currentTimeMillis()
                )
                
                // Update the task in the list
                val updatedTasks = allTasks.map { if (it.id == task.id) completedTask else it }
                
                // Save the updated tasks
                TaskRepository.saveTasks(context, updatedTasks)
                
                // Cancel any existing reminder for this task
                val reminderIntent = Intent(context, ReminderReceiver::class.java)
                val reminderPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId.hashCode(),
                    reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(reminderPendingIntent)
                
                android.util.Log.d("NotificationActionReceiver", "Task marked as completed successfully: $taskName")
            } else {
                android.util.Log.w("NotificationActionReceiver", "Task not found for completion: $taskName with ID: $taskId")
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationActionReceiver", "Error marking task as done: ${e.message}")
            e.printStackTrace()
        }
    }
}
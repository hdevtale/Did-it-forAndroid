package com.harshal.didit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object ReminderScheduler {
    
    fun schedule(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.harshal.didit.REMINDER_ALARM"
            putExtra("task_id", task.id.toString())
            putExtra("task_name", task.name)
            putExtra("task_notes", task.notes)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        task.reminderTime?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                try {
                    // Try different alarm methods for better compatibility
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        // For Android 6.0+, try setAlarmClock first (more reliable)
                        try {
                            alarmManager.setAlarmClock(
                                android.app.AlarmManager.AlarmClockInfo(reminderTime, pendingIntent),
                                pendingIntent
                            )
                            android.util.Log.d("ReminderScheduler", "Alarm scheduled with setAlarmClock for task: ${task.name} at ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(reminderTime)}")
                        } catch (e: Exception) {
                            android.util.Log.w("ReminderScheduler", "setAlarmClock failed, trying setExactAndAllowWhileIdle", e)
                            // Fallback to setExactAndAllowWhileIdle
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                pendingIntent
                            )
                            android.util.Log.d("ReminderScheduler", "Alarm scheduled with setExactAndAllowWhileIdle for task: ${task.name} at ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(reminderTime)}")
                        }
                    } else {
                        // For older Android versions, use setExact
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                        )
                        android.util.Log.d("ReminderScheduler", "Alarm scheduled with setExact for task: ${task.name} at ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(reminderTime)}")
                    }
                    
                    // Also log the current time for debugging
                    val currentTime = System.currentTimeMillis()
                    val timeUntilAlarm = reminderTime - currentTime
                    android.util.Log.d("ReminderScheduler", "Current time: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault()).format(currentTime)}")
                    android.util.Log.d("ReminderScheduler", "Time until alarm: ${timeUntilAlarm / 1000} seconds")
                    
                } catch (e: Exception) {
                    android.util.Log.e("ReminderScheduler", "Error scheduling alarm", e)
                    // Try to schedule with a simple method as last resort
                    try {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                        )
                        android.util.Log.d("ReminderScheduler", "Alarm scheduled with fallback set() method for task: ${task.name}")
                    } catch (fallbackException: Exception) {
                        android.util.Log.e("ReminderScheduler", "All alarm scheduling methods failed", fallbackException)
                    }
                }
            } else {
                android.util.Log.w("ReminderScheduler", "Reminder time is in the past: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault()).format(reminderTime)}")
            }
        } ?: run {
            android.util.Log.w("ReminderScheduler", "Task has no reminderTime: ${task.name}")
        }
    }
    
    fun cancel(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.harshal.didit.REMINDER_ALARM"
            putExtra("task_id", task.id.toString())
            putExtra("task_name", task.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
}
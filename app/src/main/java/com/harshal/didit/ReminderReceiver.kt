package com.harshal.didit

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id")
        val taskName = intent.getStringExtra("task_name")
        val taskNotes = intent.getStringExtra("task_notes")
        
        if (taskId != null && taskName != null) {
            // Show notification
            showNotification(context, taskId, taskName, taskNotes)
        }
    }
    
    private fun showNotification(context: Context, taskId: String, taskName: String, taskNotes: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification content
        val contentText = if (!taskNotes.isNullOrEmpty()) {
            "Time to work on: $taskName\nNotes: $taskNotes"
        } else {
            "Time to work on: $taskName"
        }
        
        // Create action intents
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("task_id", taskId)
            putExtra("task_name", taskName)
            putExtra("task_notes", taskNotes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val logTimeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "LOG_TIME"
            putExtra("task_id", taskId)
            putExtra("task_name", taskName)
            putExtra("task_notes", taskNotes)
        }
        val logTimePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 2,
            logTimeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "MARK_DONE"
            putExtra("task_id", taskId)
            putExtra("task_name", taskName)
            putExtra("task_notes", taskNotes)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 3,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "didit_reminders")
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Did-It Reminder")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_bell, "Snooze", snoozePendingIntent)
            .addAction(R.drawable.ic_bell, "Log Time", logTimePendingIntent)
            .addAction(R.drawable.ic_bell, "Mark Done", markDonePendingIntent)
            .build()
        
        notificationManager.notify(taskId.hashCode(), notification)
        
        // Log for debugging
        android.util.Log.d("ReminderReceiver", "Notification sent for task: $taskName")
    }
}
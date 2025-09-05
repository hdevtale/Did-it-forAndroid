package com.harshal.didit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log

class MinimalTaskWidget : AppWidgetProvider() {
    
    companion object {
        private const val TAG = "MinimalTaskWidget"
        const val ACTION_LOG_TIME = "com.harshal.didit.MINIMAL_WIDGET_LOG_TIME"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "MinimalTaskWidget onUpdate called for ${appWidgetIds.size} widgets")
        
        appWidgetIds.forEach { appWidgetId ->
            try {
                updateWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget $appWidgetId", e)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Updating minimal widget $appWidgetId")
        
        try {
            val tasks = TaskRepository.loadTasks(context)
            val ongoingTasks = tasks.filter { !it.isCompleted }
            
            if (ongoingTasks.isNotEmpty()) {
                val task = ongoingTasks.first()
                showTaskWidget(context, appWidgetManager, appWidgetId, task)
            } else {
                showNoTasksWidget(context, appWidgetManager, appWidgetId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateWidget", e)
        }
    }

    private fun showTaskWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        task: Task
    ) {
        Log.d(TAG, "Showing minimal task widget for task: ${task.name}")
        
        val views = RemoteViews(context.packageName, R.layout.widget_minimal_task)
        
        // Set task name
        views.setTextViewText(R.id.taskNameText, task.name)
        
        // Set time since last logged
        val lastLoggedTime = task.lastLoggedTimestamp
        val timeSince = formatTimeElapsed(lastLoggedTime)
        views.setTextViewText(R.id.timeSinceText, "Last: $timeSince")
        
        // Set up log time button
        val logTimeIntent = Intent(context, SimpleWidgetLogReceiver::class.java).apply {
            action = ACTION_LOG_TIME
            putExtra("taskId", task.id.toString())
            putExtra("appWidgetId", appWidgetId)
        }
        val logTimePendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, logTimeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.logTimeButton, logTimePendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Minimal task widget updated successfully")
    }

    private fun showNoTasksWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Showing no tasks widget")
        
        val views = RemoteViews(context.packageName, R.layout.widget_minimal_task)
        
        views.setTextViewText(R.id.taskNameText, "No tasks")
        views.setTextViewText(R.id.timeSinceText, "Create tasks in the app")
        
        // Set up button to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.logTimeButton, openAppPendingIntent)
        views.setTextViewText(R.id.logTimeButton, "OPEN APP")
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "No tasks widget updated successfully")
    }
    
    private fun formatTimeElapsed(lastLoggedTime: Long): String {
        if (lastLoggedTime == 0L) {
            return "Never"
        }
        
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastLoggedTime
        
        return when {
            timeDiff < 60000 -> "Just now"
            timeDiff < 3600000 -> "${timeDiff / 60000}m ago"
            timeDiff < 86400000 -> "${timeDiff / 3600000}h ago"
            else -> "${timeDiff / 86400000}d ago"
        }
    }
}

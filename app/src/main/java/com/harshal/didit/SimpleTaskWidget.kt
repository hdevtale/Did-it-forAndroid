package com.harshal.didit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log

class SimpleTaskWidget : AppWidgetProvider() {
    
    companion object {
        private const val TAG = "SimpleTaskWidget"
        const val ACTION_LOG_TIME = "com.harshal.didit.SIMPLE_WIDGET_LOG_TIME"
        const val ACTION_OPEN_APP = "com.harshal.didit.SIMPLE_WIDGET_OPEN_APP"
        
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val widget = SimpleTaskWidget()
            widget.updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "SimpleTaskWidget onUpdate called for ${appWidgetIds.size} widgets")
        
        appWidgetIds.forEach { appWidgetId ->
            try {
                updateWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget $appWidgetId", e)
                showErrorWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Updating widget $appWidgetId")
        
        try {
            // Get the selected task for this widget
            val selectedTaskId = SimpleTaskWidgetConfigureActivity.getWidgetTaskId(context, appWidgetId)
            
            if (selectedTaskId != null) {
                val tasks = TaskRepository.loadTasks(context)
                val selectedTask = tasks.find { it.id.toString() == selectedTaskId }
                
                if (selectedTask != null && !selectedTask.isCompleted) {
                    showTaskWidget(context, appWidgetManager, appWidgetId, selectedTask)
                } else {
                    // Selected task no longer exists or is completed, show no tasks
                    showNoTasksWidget(context, appWidgetManager, appWidgetId)
                }
            } else {
                // No task selected yet, get the first ongoing task as fallback
                val tasks = TaskRepository.loadTasks(context)
                val ongoingTasks = tasks.filter { !it.isCompleted }
                
                if (ongoingTasks.isNotEmpty()) {
                    val task = ongoingTasks.first()
                    showTaskWidget(context, appWidgetManager, appWidgetId, task)
                } else {
                    showNoTasksWidget(context, appWidgetManager, appWidgetId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateWidget", e)
            showErrorWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun showTaskWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        task: Task
    ) {
        Log.d(TAG, "Showing task widget for task: ${task.name}")
        
        val views = RemoteViews(context.packageName, R.layout.widget_simple_task)
        
        // Set task name with proper truncation
        val displayName = if (task.name.length > 30) {
            task.name.substring(0, 27) + "..."
        } else {
            task.name
        }
        views.setTextViewText(R.id.taskNameText, displayName)
        
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
        
        // Set up open app button
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId * 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.openAppButton, openAppPendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Task widget updated successfully")
    }

    private fun showNoTasksWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Showing no tasks widget")
        
        val views = RemoteViews(context.packageName, R.layout.widget_simple_task)
        
        views.setTextViewText(R.id.taskNameText, "No Tasks")
        views.setTextViewText(R.id.timeSinceText, "Add your first task!")
        
        // Only open app button
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId * 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.openAppButton, openAppPendingIntent)
        
        // Hide log time button
        views.setViewVisibility(R.id.logTimeButton, android.view.View.GONE)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "No tasks widget updated successfully")
    }

    private fun showErrorWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Showing error widget")
        
        val views = RemoteViews(context.packageName, R.layout.widget_simple_task)
        
        views.setTextViewText(R.id.taskNameText, "Widget Error")
        views.setTextViewText(R.id.timeSinceText, "Tap to open app")
        
        // Only open app button
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId * 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.openAppButton, openAppPendingIntent)
        
        // Hide log time button
        views.setViewVisibility(R.id.logTimeButton, android.view.View.GONE)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Error widget updated successfully")
    }

    private fun formatTimeElapsed(timestamp: Long): String {
        if (timestamp == 0L) return "Never logged"
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> {
                // Show date and time for events older than 24 hours
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = timestamp
                
                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val month = calendar.get(java.util.Calendar.MONTH)
                val hour = calendar.get(java.util.Calendar.HOUR)
                val minute = calendar.get(java.util.Calendar.MINUTE)
                val amPm = calendar.get(java.util.Calendar.AM_PM)
                
                val dayNames = arrayOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                
                val amPmText = if (amPm == java.util.Calendar.AM) "AM" else "PM"
                val hour12 = if (hour == 0) 12 else hour
                val minuteFormatted = String.format("%02d", minute)
                
                // Format: "Mon, Jan 15 at 2:30 PM"
                "${dayNames[dayOfWeek]}, ${monthNames[month]} $dayOfMonth at $hour12:$minuteFormatted $amPmText"
            }
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    }
}

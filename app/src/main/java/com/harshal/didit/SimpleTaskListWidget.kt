package com.harshal.didit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log

class SimpleTaskListWidget : AppWidgetProvider() {
    
    companion object {
        private const val TAG = "SimpleTaskListWidget"
        const val ACTION_OPEN_APP = "com.harshal.didit.SIMPLE_TASK_LIST_OPEN_APP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "SimpleTaskListWidget onUpdate called for ${appWidgetIds.size} widgets")
        
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
        Log.d(TAG, "Updating task list widget $appWidgetId")
        
        try {
            val tasks = TaskRepository.loadTasks(context)
            val ongoingTasks = tasks.filter { !it.isCompleted }
            
            if (ongoingTasks.isNotEmpty()) {
                showTaskListWidget(context, appWidgetManager, appWidgetId, ongoingTasks)
            } else {
                showNoTasksWidget(context, appWidgetManager, appWidgetId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateWidget", e)
            showErrorWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun showTaskListWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        tasks: List<Task>
    ) {
        Log.d(TAG, "Showing task list widget with ${tasks.size} tasks")
        
        val views = RemoteViews(context.packageName, R.layout.widget_simple_task_list)
        
        // Set title and count
        views.setTextViewText(R.id.widgetTitle, "Ongoing Tasks")
        views.setTextViewText(R.id.taskCount, "${tasks.size} task${if (tasks.size != 1) "s" else ""}")
        
        // Set individual task names (show up to 3)
        if (tasks.isNotEmpty()) {
            views.setTextViewText(R.id.task1, tasks[0].name)
            views.setViewVisibility(R.id.task1, android.view.View.VISIBLE)
            
            if (tasks.size > 1) {
                views.setTextViewText(R.id.task2, tasks[1].name)
                views.setViewVisibility(R.id.task2, android.view.View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.task2, android.view.View.GONE)
            }
            
            if (tasks.size > 2) {
                views.setTextViewText(R.id.task3, tasks[2].name)
                views.setViewVisibility(R.id.task3, android.view.View.VISIBLE)
                
                if (tasks.size > 3) {
                    views.setTextViewText(R.id.moreTasks, "+${tasks.size - 3} more")
                    views.setViewVisibility(R.id.moreTasks, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.moreTasks, android.view.View.GONE)
                }
            } else {
                views.setViewVisibility(R.id.task3, android.view.View.GONE)
                views.setViewVisibility(R.id.moreTasks, android.view.View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.task1, android.view.View.GONE)
            views.setViewVisibility(R.id.task2, android.view.View.GONE)
            views.setViewVisibility(R.id.task3, android.view.View.GONE)
            views.setViewVisibility(R.id.moreTasks, android.view.View.GONE)
        }
        
        // Set up open app button
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId * 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, openAppPendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Task list widget updated successfully")
    }

    private fun showNoTasksWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Showing no tasks widget")
        
        val views = RemoteViews(context.packageName, R.layout.widget_simple_task_list)
        
        views.setTextViewText(R.id.widgetTitle, "No Tasks")
        views.setTextViewText(R.id.taskCount, "Add your first task!")
        
        // Hide all task texts
        views.setViewVisibility(R.id.task1, android.view.View.GONE)
        views.setViewVisibility(R.id.task2, android.view.View.GONE)
        views.setViewVisibility(R.id.task3, android.view.View.GONE)
        views.setViewVisibility(R.id.moreTasks, android.view.View.GONE)
        
        // Set up open app button
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId * 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, openAppPendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "No tasks widget updated successfully")
    }

    private fun showErrorWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "Showing error widget")
        
        val views = RemoteViews(context.packageName, R.layout.widget_simple_task_list)
        
        views.setTextViewText(R.id.widgetTitle, "Widget Error")
        views.setTextViewText(R.id.taskCount, "Tap to open app")
        
        // Hide all task texts
        views.setViewVisibility(R.id.task1, android.view.View.GONE)
        views.setViewVisibility(R.id.task2, android.view.View.GONE)
        views.setViewVisibility(R.id.task3, android.view.View.GONE)
        views.setViewVisibility(R.id.moreTasks, android.view.View.GONE)
        
        // Set up open app button
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, appWidgetId * 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, openAppPendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Error widget updated successfully")
    }
}

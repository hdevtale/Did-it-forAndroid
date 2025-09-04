package com.harshal.didit

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TaskRepository {
    private const val PREF_NAME = "task_preferences"
    private const val KEY_TASKS = "tasks"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_INTRO_TASKS_SHOWN = "intro_tasks_shown"
    private val gson = Gson()

    fun saveTasks(context: Context, tasks: List<Task>) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val tasksJson = gson.toJson(tasks)
        sharedPreferences.edit().putString(KEY_TASKS, tasksJson).apply()
        
        // Debug: Log what's being saved
        android.util.Log.d("TaskRepository", "Saving ${tasks.size} tasks:")
        tasks.forEach { task ->
            android.util.Log.d("TaskRepository", "  - '${task.name}': isCompleted=${task.isCompleted}, completedTimestamp=${task.completedTimestamp}")
        }
    }

    fun loadTasks(context: Context): List<Task> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString(KEY_TASKS, null)
        
        return if (tasksJson != null) {
            try {
                val type = object : TypeToken<List<Task>>() {}.type
                val loadedTasks: List<Task> = gson.fromJson(tasksJson, type) ?: emptyList()
                
                // Debug: Log what's being loaded
                android.util.Log.d("TaskRepository", "Loaded ${loadedTasks.size} tasks:")
                loadedTasks.forEach { task ->
                    android.util.Log.d("TaskRepository", "  - '${task.name}': isCompleted=${task.isCompleted}, completedTimestamp=${task.completedTimestamp}")
                }
                
                loadedTasks
            } catch (e: Exception) {
                android.util.Log.e("TaskRepository", "Error loading tasks: ${e.message}")
                // If there's an error parsing, return empty list
                emptyList()
            }
        } else {
            android.util.Log.d("TaskRepository", "No saved tasks found")
            emptyList()
        }
    }
    
    fun clearAllTasks(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(KEY_TASKS).apply()
    }
    
    fun isFirstLaunch(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setFirstLaunchComplete(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
    
    fun areIntroTasksShown(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_INTRO_TASKS_SHOWN, false)
    }
    
    fun setIntroTasksShown(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_INTRO_TASKS_SHOWN, true).apply()
    }
    
    fun hasAnyUserTasks(context: Context): Boolean {
        val tasks = loadTasks(context)
        return tasks.isNotEmpty()
    }
    
    fun resetAppData(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
    
    
}

package com.harshal.didit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.harshal.didit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ongoingTaskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: TaskAdapter
    private val allTasks = mutableListOf<Task>()
    private val ongoingTasks = mutableListOf<Task>()
    private val completedTasks = mutableListOf<Task>()
    private var isSelectionMode = false
    private var lastTapTime = 0L
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Enable edge-to-edge for Android 15+ compatibility
            enableEdgeToEdge()
            
            // Optimize for high refresh rate displays (up to 120Hz)
            optimizeForHighRefreshRate()
            
            // Apply the saved theme preference
            ThemePreferences.applySavedTheme(this)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
            
            // Handle window insets for edge-to-edge and keyboard
            setupWindowInsetsAndKeyboard()
        
        setupRecyclerView()
        setupDummyData()
        setupClickListeners()
            
            // Setup modern back button handling
            setupBackButtonHandling()
            
            // Simple FAB setup
            binding.addTaskFab.alpha = 1f
            
            // Create notification channel
            NotificationHelper.createNotificationChannel(this)
            
            // Request notification permission for Android 13+
            requestNotificationPermission()
            
            // Check if we should show What's New dialog
            checkAndShowWhatsNew()
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Log error silently for professional appearance
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Save tasks when resuming
        saveTasks()
    }
    
    override fun onPause() {
        super.onPause()
        // Save tasks when pausing
        saveTasks()
        
        // Memory optimizations for high refresh rate displays
        optimizeMemoryUsage()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources for high refresh rate displays
        cleanupResources()
    }
    
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Theme changes are automatically handled by AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        // The activity will automatically adapt to device theme changes
    }
    
    private fun optimizeForHighRefreshRate() {
        try {
            // Enable high refresh rate support for Android 11+ (API 30+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.attributes.preferredRefreshRate = 120f
                window.attributes.preferredDisplayModeId = 0 // Let system choose best mode
            }
            
            // Enable hardware acceleration for better performance
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            
            // Optimize window for smooth animations
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun optimizeMemoryUsage() {
        try {
            // Clear RecyclerView caches to free memory
            binding.ongoingTasksRecyclerView.recycledViewPool.clear()
            binding.completedTasksRecyclerView.recycledViewPool.clear()
            
            // Force garbage collection for high refresh rate displays
            System.gc()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun cleanupResources() {
        try {
            // Clear all task lists to free memory
            allTasks.clear()
            ongoingTasks.clear()
            completedTasks.clear()
            
            // Force garbage collection
            System.gc()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupWindowInsetsAndKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Check if keyboard is visible (IME insets > 0)
            val isKeyboardVisible = imeInsets.bottom > 0
            
            if (isKeyboardVisible) {
                // Keyboard is open - create minimal faux space
                val keyboardHeight = imeInsets.bottom
                
                // Create a very subtle faux space that doesn't jar the UI
                // This provides just enough space to see the typing area
                v.setPadding(
                    systemBars.left, 
                    systemBars.top, 
                    systemBars.right, 
                    systemBars.bottom + (keyboardHeight * 0.1f).toInt() // Minimal faux space (10%)
                )
                
                android.util.Log.d("MainActivity", "Keyboard opened - creating minimal faux space of ${(keyboardHeight * 0.1f).toInt()} pixels")
            } else {
                // Keyboard is closed - restore original padding
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                
                android.util.Log.d("MainActivity", "Keyboard closed - restoring original padding")
            }
            
            insets
        }
    }
    
    // Method to create dynamic faux space for specific typing areas
    fun createDynamicFauxSpace(typingView: android.view.View) {
        try {
            val screenHeight = resources.displayMetrics.heightPixels
            val keyboardHeight = screenHeight * 0.4f // Estimated keyboard height
            
            // Get the position of the typing view
            val typingLocation = IntArray(2)
            typingView.getLocationInWindow(typingLocation)
            val typingViewCenter = typingLocation[1] + (typingView.height / 2)
            
            // Calculate available space above keyboard
            val availableSpace = screenHeight - keyboardHeight
            val targetCenter = availableSpace / 2
            
            // Calculate how much we need to scroll to center the typing view
            val scrollNeeded = typingViewCenter - targetCenter
            
            if (scrollNeeded > 0) {
                // Find the main scroll view and scroll smoothly
                val scrollView = findViewById<androidx.core.widget.NestedScrollView>(com.harshal.didit.R.id.mainScrollView)
                scrollView?.smoothScrollBy(0, scrollNeeded.toInt())
                
                android.util.Log.d("MainActivity", "Created dynamic faux space - scrolled by $scrollNeeded pixels")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error creating dynamic faux space: ${e.message}")
        }
    }
    
    // Enhanced method to create intelligent faux space that adapts to typing position
    fun createIntelligentFauxSpace(typingView: android.view.View) {
        try {
            val screenHeight = resources.displayMetrics.heightPixels
            val keyboardHeight = screenHeight * 0.4f
            val availableSpace = screenHeight - keyboardHeight
            
            // Get typing view position
            val typingLocation = IntArray(2)
            typingView.getLocationInWindow(typingLocation)
            val typingViewTop = typingLocation[1]
            val typingViewBottom = typingViewTop + typingView.height
            
            // Check if typing view is in the lower half of available space
            val isInLowerHalf = typingViewTop > (availableSpace * 0.5f)
            
            if (isInLowerHalf) {
                // Only create faux space if the typing view is in the lower half
                val scrollView = findViewById<androidx.core.widget.NestedScrollView>(com.harshal.didit.R.id.mainScrollView)
                val scrollAmount = (typingViewTop - (availableSpace * 0.3f)).toInt()
                
                if (scrollAmount > 0) {
                    scrollView?.smoothScrollBy(0, scrollAmount)
                    android.util.Log.d("MainActivity", "Created intelligent faux space - scrolled by $scrollAmount pixels")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error creating intelligent faux space: ${e.message}")
        }
    }

    private fun setupBackButtonHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    // First priority: Exit selection mode if active
                    isSelectionMode -> {
                        exitSelectionMode()
                    }
                    // Second priority: Collapse expanded tasks if any
                    isAnyTaskExpanded() -> {
                        collapseAllExpandedTasks()
                    }
                    // Third priority: Check for unsaved changes
                    hasUnsavedChanges() -> {
                        showUnsavedChangesDialog()
                    }
                    // Default: Close the app
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        try {
        // Setup ongoing tasks adapter
        ongoingTaskAdapter = TaskAdapter(
            tasks = ongoingTasks,
            onLogTimeClick = { task -> onLogTimeClick(task) },
            onTaskClick = { task -> onTaskClick(task) },
            onSaveNotes = { task, notes -> onSaveNotes(task, notes) },
                onCompleteTask = { task -> onCompleteTask(task) },
                onLongPress = { task -> onLongPress(task) },
                onEditReminder = { task -> onEditReminder(task) },
                onUndoComplete = { task -> onUndoComplete(task) }
        )
        
        // Setup completed tasks adapter (read-only)
        completedTaskAdapter = TaskAdapter(
            tasks = completedTasks,
            onLogTimeClick = { task -> onLogTimeClick(task) },
            onTaskClick = { task -> onTaskClick(task) },
            onSaveNotes = { task, notes -> onSaveNotes(task, notes) },
                onCompleteTask = { task -> onCompleteTask(task) },
                onLongPress = { task -> onLongPress(task) },
                onEditReminder = { task -> onEditReminder(task) },
                onUndoComplete = { task -> onUndoComplete(task) }
        )
        
            // Setup ongoing tasks RecyclerView with high refresh rate optimizations
        binding.ongoingTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ongoingTaskAdapter
                
                // Optimize for high refresh rate displays
                setHasFixedSize(false) // Allow dynamic sizing for expansion animations
                setItemViewCacheSize(20) // Cache more views for smooth scrolling
                // Drawing cache methods are deprecated, using modern rendering optimizations
                
                // Use optimized item animator for smooth sliding animations
                itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                    supportsChangeAnimations = true
                    addDuration = 0L // Disable default add animation - we handle sliding custom
                    removeDuration = 0L // Disable default remove animation - we handle sliding custom
                    moveDuration = 0L // Disable move animation to prevent conflicts
                    changeDuration = 150L
                }
                
                // Enable nested scrolling for better performance
                isNestedScrollingEnabled = true
            }
            
            // Setup completed tasks RecyclerView with high refresh rate optimizations
        binding.completedTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = completedTaskAdapter
                
                // Optimize for high refresh rate displays
                setHasFixedSize(false) // Allow dynamic sizing for expansion animations
                setItemViewCacheSize(20) // Cache more views for smooth scrolling
                // Drawing cache methods are deprecated, using modern rendering optimizations
                
                // Use optimized item animator for smooth sliding animations
                itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                    supportsChangeAnimations = true
                    addDuration = 0L // Disable default add animation - we handle sliding custom
                    removeDuration = 0L // Disable default remove animation - we handle sliding custom
                    moveDuration = 0L // Disable move animation to prevent conflicts
                    changeDuration = 150L
                }
                
                // Disable nested scrolling for completed tasks to prevent conflicts
                isNestedScrollingEnabled = false
        }
        
        // Setup section headers
        setupSectionHeaders()
        } catch (e: Exception) {
            e.printStackTrace()
            // Log error silently for professional appearance
        }
    }

    private fun setupSectionHeaders() {
        binding.ongoingSectionHeader.sectionTitleTextView.text = getString(R.string.ongoing_tasks)
        binding.ongoingSectionHeader.sectionSubtitleTextView.text = getString(R.string.tasks_in_progress, ongoingTasks.size)
        
        binding.completedSectionHeader.sectionTitleTextView.text = getString(R.string.completed_tasks)
        binding.completedSectionHeader.sectionSubtitleTextView.text = getString(R.string.tasks_completed, completedTasks.size)
        
        // Dynamic section visibility and sizing
        updateSectionVisibility()
    }
    
    private fun updateSectionVisibility() {
        // Always show both sections - never hide them
        animateSectionVisibility(binding.ongoingSectionHeader.root, true)
        animateSectionVisibility(binding.ongoingTasksRecyclerView, true)
        animateSectionVisibility(binding.completedSectionHeader.root, true)
        animateSectionVisibility(binding.completedTasksRecyclerView, true)
    }
    
    private fun animateSectionVisibility(view: android.view.View, show: Boolean) {
        if (show) {
            if (view.visibility == android.view.View.GONE) {
                view.visibility = android.view.View.VISIBLE
                view.alpha = 0f
                view.animate()
                    .alpha(1f)
                    .setDuration(600) // Much smoother and slower
                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                    .start()
            }
        } else {
            if (view.visibility == android.view.View.VISIBLE) {
                view.animate()
                    .alpha(0f)
                    .setDuration(400) // Much smoother and slower
                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                    .withEndAction {
                        view.visibility = android.view.View.GONE
                    }
                    .start()
            }
        }
    }
    
    private fun saveTasks() {
        try {
            TaskRepository.saveTasks(this, allTasks)
            android.util.Log.d("MainActivity", "Tasks saved successfully: ${allTasks.size} total tasks")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainActivity", "Error saving tasks: ${e.message}")
        }
    }
    
    private fun saveTasksImmediately() {
        runOnUiThread {
            saveTasks()
        }
    }
    
    private fun updateTaskInLists(updatedTask: Task) {
        // Update in allTasks
        val allTasksIndex = allTasks.indexOfFirst { it.id == updatedTask.id }
        if (allTasksIndex != -1) {
            allTasks[allTasksIndex] = updatedTask
        }
        
        // Update in ongoingTasks
        val ongoingIndex = ongoingTasks.indexOfFirst { it.id == updatedTask.id }
        if (ongoingIndex != -1) {
            ongoingTasks[ongoingIndex] = updatedTask
            // Notify adapter of the change to update UI immediately
            ongoingTaskAdapter.notifyItemChanged(ongoingIndex)
        }
        
        // Update in completedTasks
        val completedIndex = completedTasks.indexOfFirst { it.id == updatedTask.id }
        if (completedIndex != -1) {
            completedTasks[completedIndex] = updatedTask
            // Notify adapter of the change to update UI immediately
            completedTaskAdapter.notifyItemChanged(completedIndex)
        }
        
        // Save immediately after any update
        saveTasksImmediately()
    }
    
    private fun checkAndShowWhatsNew() {
        try {
            if (AppPreferences.shouldShowWhatsNew(this)) {
                val dialog = WhatsNewDialogFragment.newInstance(object : WhatsNewDialogFragment.WhatsNewDialogListener {
                    override fun onWhatsNewDismissed() {
                        AppPreferences.markUpdateAsSeen(this@MainActivity)
                    }
                })
                dialog.show(supportFragmentManager, "WhatsNewDialog")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupDummyData() {
        try {
            // Check if this is the first launch and intro tasks haven't been shown yet
            val isFirstLaunch = TaskRepository.isFirstLaunch(this)
            val introTasksShown = TaskRepository.areIntroTasksShown(this)
        val existingTasks = TaskRepository.loadTasks(this)
            
            android.util.Log.d("MainActivity", "Setup data - First launch: $isFirstLaunch, Intro shown: $introTasksShown, Existing tasks: ${existingTasks.size}")
        
        if (existingTasks.isNotEmpty()) {
            // If we have saved tasks, use them
            allTasks.addAll(existingTasks)
                android.util.Log.d("MainActivity", "Loaded ${existingTasks.size} existing tasks")
                
                // Debug: Log task completion status
                existingTasks.forEach { task ->
                    android.util.Log.d("MainActivity", "Task: '${task.name}' - isCompleted: ${task.isCompleted}, completedTimestamp: ${task.completedTimestamp}")
                }
            } else if (isFirstLaunch && !introTasksShown) {
                // Only show intro tasks on very first launch
                createSampleTasks()
                TaskRepository.setIntroTasksShown(this)
                TaskRepository.setFirstLaunchComplete(this)
                android.util.Log.d("MainActivity", "Created intro tasks for first launch")
        } else {
                // No tasks and not first launch - start with empty list
                allTasks.clear()
                android.util.Log.d("MainActivity", "Starting with empty task list")
        }
        
        organizeTasks()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainActivity", "Error in setupDummyData: ${e.message}")
            // Create empty lists as fallback
            allTasks.clear()
            ongoingTasks.clear()
            completedTasks.clear()
        }
    }
    
    private fun createSampleTasks() {
        allTasks.clear()
        allTasks.addAll(listOf(
            Task(
                name = "Welcome to Did-It!",
                notes = "This is your first task. Tap the checkmark to complete it and see how tasks move to the completed section!",
                isCompleted = false
            ),
            Task(
                name = "Explore the app",
                notes = "Try adding more tasks, setting reminders, and organizing your day. Long press tasks to select multiple items.",
                isCompleted = false
            ),
            Task(
                name = "Set a reminder",
                notes = "Tap the bell icon to set reminders for important tasks. You'll get notifications at the scheduled time.",
                isCompleted = false
            ),
            Task(
                name = "Log your progress",
                notes = "Use the clock icon to log when you work on tasks. Track your time and stay productive!",
                isCompleted = false
            ),
            Task(
                name = "Add widgets to home screen",
                notes = "Long press on your home screen and add Did-It widgets to quickly log time or view tasks without opening the app.",
                isCompleted = false
            )
        ))
    }

    private fun organizeTasks() {
        runOnUiThread {
            try {
        ongoingTasks.clear()
        completedTasks.clear()
        
        allTasks.forEach { task ->
                    android.util.Log.d("MainActivity", "Organizing task: '${task.name}' - isCompleted: ${task.isCompleted}")
            if (task.isCompleted) {
                completedTasks.add(task)
                        android.util.Log.d("MainActivity", "Added to completed: '${task.name}'")
            } else {
                ongoingTasks.add(task)
                        android.util.Log.d("MainActivity", "Added to ongoing: '${task.name}'")
            }
        }
        
        // Update both adapters
        ongoingTaskAdapter.updateTasks(ongoingTasks)
        completedTaskAdapter.updateTasks(completedTasks)
        
        // Update section headers
        setupSectionHeaders()
                
                // Force complete UI refresh
                ongoingTaskAdapter.notifyDataSetChanged()
                completedTaskAdapter.notifyDataSetChanged()
                
                android.util.Log.d("MainActivity", "Tasks organized - ongoing: ${ongoingTasks.size}, completed: ${completedTasks.size}")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Error organizing tasks: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.addTaskFab.setOnClickListener {
            if (isSelectionMode) {
                deleteSelectedTasks()
            } else {
            showAddTaskDialog()
            }
        }
        
        // Long press on app title to reset theme selection (for testing)
        binding.lastTimeTextView.setOnLongClickListener {
            ThemePreferences.resetThemeSelection(this)
            true
        }
        
        // Add a simple test button for scheduled reminders
        binding.lastTimeTextView.setOnClickListener {
            // Single tap for immediate notification, double tap for scheduled reminder
            if (System.currentTimeMillis() - lastTapTime < 500) {
                // Double tap detected
                testScheduledReminder()
            } else {
                // Single tap
                testNotification()
            }
            lastTapTime = System.currentTimeMillis()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = layoutInflater.inflate(
            com.harshal.didit.R.layout.dialog_add_task,
            null
        )
        
        val editText = dialogBinding.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.harshal.didit.R.id.taskNameEditText
        )
        
        val notesEditText = dialogBinding.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.harshal.didit.R.id.taskNotesEditText
        )
        
        
        // Create dialog with custom view
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Add New Task")
            .setView(dialogBinding)
            .create()
        
        // Set dialog window properties for smooth animations
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
        
        // Set dialog background to match theme and set up button after dialog is shown
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(com.harshal.didit.R.color.background_black)
            
            // Animate dialog appearance
            dialogBinding.alpha = 0f
            dialogBinding.scaleX = 0.8f
            dialogBinding.scaleY = 0.8f
            dialogBinding.translationY = -50f
            
            dialogBinding.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(600) // Much smoother and slower
                .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                .start()
            
            // Set up the custom Save Task button after dialog is shown
            val saveButton = dialogBinding.findViewById<com.google.android.material.button.MaterialButton>(
                com.harshal.didit.R.id.saveTaskButton
            )
            
            if (saveButton != null) {
                android.util.Log.d("MainActivity", "Save button found successfully")
                saveButton.setOnClickListener {
                    val taskName = editText.text.toString().trim()
                    val notes = notesEditText.text.toString().trim()
                    
                    android.util.Log.d("MainActivity", "Save button clicked - Task: '$taskName', Notes: '$notes'")
                    
                    if (taskName.isNotEmpty()) {
                        android.util.Log.d("MainActivity", "Adding new task...")
                        
                        // Animate button press with smooth timing
                        saveButton.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(300) // Much smoother and slower
                            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                            .withEndAction {
                                saveButton.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300) // Much smoother and slower
                                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                    .start()
                            }
                            .start()
                        
                        // Animate dialog dismissal
                        dialogBinding.animate()
                            .alpha(0f)
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .translationY(-50f)
                            .setDuration(500) // Much smoother and slower
                            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                            .withEndAction {
                                dialog.dismiss()
                            }
                            .start()
                        
                        addNewTask(taskName, notes)
                    } else {
                        android.util.Log.d("MainActivity", "Task name is empty, not adding task")
                        // Shake animation for empty input with smooth timing
                        editText.animate()
                            .translationX(-15f)
                            .setDuration(150) // Much smoother and slower
                            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                            .withEndAction {
                                editText.animate()
                                    .translationX(15f)
                                    .setDuration(150) // Much smoother and slower
                                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                    .withEndAction {
                                        editText.animate()
                                            .translationX(-15f)
                                            .setDuration(150) // Much smoother and slower
                                            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                            .withEndAction {
                                                editText.animate()
                                                    .translationX(0f)
                                                    .setDuration(150) // Much smoother and slower
                                                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                                    .start()
                                            }
                                            .start()
                                    }
                                    .start()
                            }
                            .start()
                    }
                }
            } else {
                android.util.Log.e("MainActivity", "Save button not found!")
            }
        }
        
        dialog.show()
    }

    private fun addNewTask(taskName: String, notes: String = "") {
        android.util.Log.d("MainActivity", "addNewTask called with: '$taskName', '$notes'")
        
        val newTask = Task(name = taskName, notes = notes, isCompleted = false)
        android.util.Log.d("MainActivity", "Created new task: ${newTask.id}")
        
        // Add to all tasks list
        allTasks.add(0, newTask)
        android.util.Log.d("MainActivity", "Added to allTasks. Total tasks: ${allTasks.size}")
        
        // Add to ongoing tasks list
        ongoingTasks.add(0, newTask)
        android.util.Log.d("MainActivity", "Added to ongoingTasks. Total ongoing: ${ongoingTasks.size}")
        
        // Save immediately to ensure persistence
        saveTasksImmediately()
        android.util.Log.d("MainActivity", "Saved to SharedPreferences immediately")
        
        // Update the adapter on the main thread
        runOnUiThread {
            // Update the adapter with the new task list
            ongoingTaskAdapter.updateTasks(ongoingTasks)
            android.util.Log.d("MainActivity", "Updated adapters - ongoingTaskAdapter item count: ${ongoingTaskAdapter.itemCount}")
            
            // Force RecyclerView to refresh
            binding.ongoingTasksRecyclerView.invalidate()
            binding.ongoingTasksRecyclerView.requestLayout()
            
            // Additional refresh attempt
            binding.ongoingTasksRecyclerView.post {
                binding.ongoingTasksRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
        
        // Update section headers
        setupSectionHeaders()
        
        // Smooth scroll to the top to show the new task
        binding.ongoingTasksRecyclerView.post {
        binding.ongoingTasksRecyclerView.smoothScrollToPosition(0)
        }
        
        android.util.Log.d("MainActivity", "addNewTask completed successfully")
        
        // Task added successfully - no visual confirmation needed
    }

    private fun onLogTimeClick(task: Task) {
        val updatedTask = task.copy(
            lastLoggedTimestamp = System.currentTimeMillis()
        )
        updateTaskInLists(updatedTask)
        
        // Save to SharedPreferences
        TaskRepository.saveTasks(this, allTasks)
    }

    private fun onSaveNotes(task: Task, notes: String) {
        val updatedTask = task.copy(notes = notes)
        updateTaskInLists(updatedTask)
        
        // Save to SharedPreferences
        TaskRepository.saveTasks(this, allTasks)
        
        // Note: No toast message to avoid interrupting user's typing flow
        // The button animation provides visual feedback instead
    }

    private fun onTaskClick(task: Task) {
        // Task expansion is handled in the adapter
    }
    
    private fun onEditReminder(task: Task) {
        val fragment = SetReminderDialogFragment.newInstance(task, object : SetReminderDialogFragment.ReminderDialogListener {
            override fun onReminderSet(task: Task, reminderTime: Long) {
                val updatedTask = task.copy(
                    reminderTime = reminderTime
                )
                updateTaskInLists(updatedTask)
                
                // Schedule the reminder
                ReminderScheduler.schedule(this@MainActivity, updatedTask)
                
                // Save to SharedPreferences
                TaskRepository.saveTasks(this@MainActivity, allTasks)
                
                // Reminder set successfully
            }
            
            override fun onReminderRemoved(task: Task) {
                val updatedTask = task.copy(
                    reminderTime = null
                )
                updateTaskInLists(updatedTask)
                
                // Cancel the reminder
                ReminderScheduler.cancel(this@MainActivity, task)
                
                // Save to SharedPreferences
                TaskRepository.saveTasks(this@MainActivity, allTasks)
                
                // Reminder removed successfully
            }
        })
        
        fragment.show(supportFragmentManager, "SetReminderDialog")
    }

    private fun onCompleteTask(task: Task) {
        runOnUiThread {
            try {
        val completedTask = task.copy(
            isCompleted = true,
            completedTimestamp = System.currentTimeMillis()
        )
                
                // Update all tasks list first
                val allIndex = allTasks.indexOfFirst { it.id == task.id }
                if (allIndex != -1) {
                    allTasks[allIndex] = completedTask
                }
        
        // Remove from ongoing tasks
                val ongoingIndex = ongoingTasks.indexOfFirst { it.id == task.id }
        if (ongoingIndex != -1) {
                    // Get the view to animate before removing
                    val viewToAnimate = binding.ongoingTasksRecyclerView.findViewHolderForAdapterPosition(ongoingIndex)?.itemView
                    
                    // Remove from ongoing tasks
            ongoingTasks.removeAt(ongoingIndex)
            ongoingTaskAdapter.notifyItemRemoved(ongoingIndex)
        
        // Add to completed tasks
        completedTasks.add(0, completedTask)
        completedTaskAdapter.notifyItemInserted(0)
        
                    // Animate the slide down if we found the view
                    viewToAnimate?.let { view ->
                        view.animate()
                            .translationY(250f)
                            .alpha(0f)
                            .setDuration(500) // Smoother for 120Hz screens
                            .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
                            .start()
                    }
                    
                    // Animate the new completed task sliding up
                    binding.completedTasksRecyclerView.post {
                        val newViewHolder = binding.completedTasksRecyclerView.findViewHolderForAdapterPosition(0)
                        newViewHolder?.itemView?.let { newView ->
                            newView.translationY = -250f
                            newView.alpha = 0f
                            newView.animate()
                                .translationY(0f)
                                .alpha(1f)
                                .setDuration(550) // Smoother for 120Hz screens
                                .setInterpolator(android.view.animation.DecelerateInterpolator(1.8f))
                                .start()
                        }
                    }
        }
        
        // Update section headers
        setupSectionHeaders()
                
                // Debug: Log the task before saving
                android.util.Log.d("MainActivity", "Before saving - Task '${completedTask.name}' isCompleted: ${completedTask.isCompleted}")
        
        // Save to SharedPreferences
                TaskRepository.saveTasks(this@MainActivity, allTasks)
                
                // Force complete UI refresh with a small delay to ensure all operations complete
                binding.root.post {
                    ongoingTaskAdapter.notifyDataSetChanged()
                    completedTaskAdapter.notifyDataSetChanged()
                    
                    // Ensure RecyclerViews are properly refreshed
                    binding.ongoingTasksRecyclerView.invalidate()
                    binding.completedTasksRecyclerView.invalidate()
                    binding.ongoingTasksRecyclerView.requestLayout()
                    binding.completedTasksRecyclerView.requestLayout()
                }
                
                android.util.Log.d("MainActivity", "Task completed - ongoing: ${ongoingTasks.size}, completed: ${completedTasks.size}")
                
                // Task completed successfully - no visual confirmation needed
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Error completing task: ${e.message}")
            }
        }
    }
    
    private fun onUndoComplete(task: Task) {
        runOnUiThread {
            try {
                // Create the original task state by removing completion properties
                val originalTask = task.copy(
                    isCompleted = false,
                    completedTimestamp = 0L
                )
                
                // Update all tasks list first
                val allIndex = allTasks.indexOfFirst { it.id == task.id }
        if (allIndex != -1) {
                    allTasks[allIndex] = originalTask
                }
                
                // Remove from completed tasks
                val completedIndex = completedTasks.indexOfFirst { it.id == task.id }
                if (completedIndex != -1) {
                    // Get the view to animate before removing
                    val viewToAnimate = binding.completedTasksRecyclerView.findViewHolderForAdapterPosition(completedIndex)?.itemView
                    
                    // Remove from completed tasks
                    completedTasks.removeAt(completedIndex)
                    completedTaskAdapter.notifyItemRemoved(completedIndex)
                    
                    // Add back to ongoing tasks
                    ongoingTasks.add(0, originalTask)
                    ongoingTaskAdapter.notifyItemInserted(0)
                    
                    // Animate the slide up if we found the view
                    viewToAnimate?.let { view ->
                        view.animate()
                            .translationY(-250f)
                            .alpha(0f)
                            .setDuration(500) // Smoother for 120Hz screens
                            .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
                            .start()
                    }
                    
                    // Animate the new ongoing task sliding down
                    binding.ongoingTasksRecyclerView.post {
                        val newViewHolder = binding.ongoingTasksRecyclerView.findViewHolderForAdapterPosition(0)
                        newViewHolder?.itemView?.let { newView ->
                            newView.translationY = 250f
                            newView.alpha = 0f
                            newView.animate()
                                .translationY(0f)
                                .alpha(1f)
                                .setDuration(550) // Smoother for 120Hz screens
                                .setInterpolator(android.view.animation.DecelerateInterpolator(1.8f))
                                .start()
                        }
                    }
                }
                
                // Update section headers
                setupSectionHeaders()
                
                // Save to SharedPreferences
                TaskRepository.saveTasks(this@MainActivity, allTasks)
                
                // Force complete UI refresh
                binding.root.post {
                    ongoingTaskAdapter.notifyDataSetChanged()
                    completedTaskAdapter.notifyDataSetChanged()
                    
                    // Ensure RecyclerViews are properly refreshed
                    binding.ongoingTasksRecyclerView.invalidate()
                    binding.completedTasksRecyclerView.invalidate()
                    binding.ongoingTasksRecyclerView.requestLayout()
                    binding.completedTasksRecyclerView.requestLayout()
                }
                
                android.util.Log.d("MainActivity", "Task undone - ongoing: ${ongoingTasks.size}, completed: ${completedTasks.size}")
                
                // Task moved back to ongoing - no visual confirmation needed
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Error undoing task: ${e.message}")
            }
        }
    }
    
    private fun onLongPress(task: Task) {
        if (!isSelectionMode) {
            // Enter selection mode and select this task
            toggleSelectionMode()
            task.isSelected = true
            ongoingTaskAdapter.notifyDataSetChanged()
            completedTaskAdapter.notifyDataSetChanged()
        }
    }
    
    private fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        
        if (isSelectionMode) {
            // Enter selection mode - transform FAB to delete button
            animateFabToDelete()
            ongoingTaskAdapter.animateSelectionMode(true)
            completedTaskAdapter.animateSelectionMode(true)
        } else {
            // Exit selection mode - transform FAB back to add button
            animateFabToAdd()
            ongoingTaskAdapter.animateSelectionMode(false)
            completedTaskAdapter.animateSelectionMode(false)
        }
    }
    
    private fun animateFabToDelete() {
        // Animate FAB transformation to delete button
        binding.addTaskFab.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300) // Much smoother and slower
            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
            .withEndAction {
                // Change icon and color
                binding.addTaskFab.setImageResource(com.harshal.didit.R.drawable.ic_delete_white)
                binding.addTaskFab.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#FF4444")
                )
                
                // Scale back up
                binding.addTaskFab.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(400) // Much smoother and slower
                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                    .start()
            }
            .start()
    }
    
    private fun animateFabToAdd() {
        // Animate FAB transformation back to add button
        binding.addTaskFab.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300) // Much smoother and slower
            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
            .withEndAction {
                // Change icon and color back
                binding.addTaskFab.setImageResource(com.harshal.didit.R.drawable.ic_add)
                binding.addTaskFab.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#FF4444")
                )
                
                // Scale back up
                binding.addTaskFab.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(400) // Much smoother and slower
                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                    .start()
            }
            .start()
    }
    
    private fun deleteSelectedTasks() {
        val selectedOngoingTasks = ongoingTaskAdapter.getSelectedTasks()
        val selectedCompletedTasks = completedTaskAdapter.getSelectedTasks()
        
        if (selectedOngoingTasks.isEmpty() && selectedCompletedTasks.isEmpty()) {
            return
        }
        
        // Create custom dialog layout
        val dialogBinding = layoutInflater.inflate(
            com.harshal.didit.R.layout.dialog_delete_confirmation,
            null
        )
        
        // Set dialog title and message
        dialogBinding.findViewById<android.widget.TextView>(com.harshal.didit.R.id.dialogTitle).text = "Delete Tasks"
        dialogBinding.findViewById<android.widget.TextView>(com.harshal.didit.R.id.dialogMessage).text = 
            "Are you sure you want to delete ${selectedOngoingTasks.size + selectedCompletedTasks.size} selected task(s)?"
        
        // Create dialog
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogBinding)
            .create()
        
        // Set up button click listeners
        dialogBinding.findViewById<android.widget.Button>(com.harshal.didit.R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.findViewById<android.widget.Button>(com.harshal.didit.R.id.deleteButton).setOnClickListener {
            performDelete(selectedOngoingTasks, selectedCompletedTasks)
            dialog.dismiss()
        }
        
        // Set dialog window properties for smooth appearance
        dialog.setOnShowListener {
            dialog.window?.let { window ->
                // Set smooth window animations
                window.attributes?.windowAnimations = android.R.style.Animation_Dialog
                // Ensure proper background
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
        
        dialog.show()
    }
    
    
    private fun performDelete(selectedOngoing: List<Task>, selectedCompleted: List<Task>) {
        runOnUiThread {
            try {
                // Remove from ongoing tasks
                selectedOngoing.forEach { task ->
                    val index = ongoingTasks.indexOf(task)
                    if (index != -1) {
                        ongoingTasks.removeAt(index)
                        ongoingTaskAdapter.notifyItemRemoved(index)
                    }
                }
                
                // Remove from completed tasks
                selectedCompleted.forEach { task ->
                    val index = completedTasks.indexOf(task)
                    if (index != -1) {
                        completedTasks.removeAt(index)
                        completedTaskAdapter.notifyItemRemoved(index)
                    }
                }
                
                // Remove from all tasks
                allTasks.removeAll(selectedOngoing.toSet())
                allTasks.removeAll(selectedCompleted.toSet())
                
                // Save changes
                TaskRepository.saveTasks(this@MainActivity, allTasks)
                
                // Update section headers
                setupSectionHeaders()
                
                // Force complete UI refresh
                ongoingTaskAdapter.notifyDataSetChanged()
                completedTaskAdapter.notifyDataSetChanged()
                
                android.util.Log.d("MainActivity", "Tasks deleted - ongoing: ${ongoingTasks.size}, completed: ${completedTasks.size}")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Error deleting tasks: ${e.message}")
            }
        }
        
        // Exit selection mode and clear any remaining selections
        toggleSelectionMode()
        ongoingTaskAdapter.clearAllSelections()
        completedTaskAdapter.clearAllSelections()
    }

    // Back button handling helper methods
    private fun exitSelectionMode() {
        if (isSelectionMode) {
            // First exit selection mode
            toggleSelectionMode()
            
            // Then clear all selections and force UI update
            ongoingTaskAdapter.clearAllSelections()
            completedTaskAdapter.clearAllSelections()
            
            // Force a complete refresh to ensure visual state is reset
            ongoingTaskAdapter.notifyDataSetChanged()
            completedTaskAdapter.notifyDataSetChanged()
        }
    }

    private fun isAnyTaskExpanded(): Boolean {
        // Check if any task has expanded notes section
        return ongoingTaskAdapter.isAnyTaskExpanded() || completedTaskAdapter.isAnyTaskExpanded()
    }

    private fun collapseAllExpandedTasks() {
        // Notify adapters to collapse all expanded tasks
        ongoingTaskAdapter.collapseAllExpandedTasks()
        completedTaskAdapter.collapseAllExpandedTasks()
    }

    private fun hasUnsavedChanges(): Boolean {
        // Check if there are any unsaved changes
        // For now, we'll assume false since we save immediately
        // This could be enhanced to track actual unsaved changes
        return false
    }

    private fun showUnsavedChangesDialog() {
        // Create custom dialog layout
        val dialogBinding = layoutInflater.inflate(
            com.harshal.didit.R.layout.dialog_unsaved_changes,
            null
        )
        
        // Create dialog
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogBinding)
            .create()
        
        // Set up button click listeners
        dialogBinding.findViewById<android.widget.Button>(com.harshal.didit.R.id.stayButton).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.findViewById<android.widget.Button>(com.harshal.didit.R.id.exitButton).setOnClickListener {
            // Finish the activity to close the app
            finish()
        }
        
        // Set dialog background
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(com.harshal.didit.R.color.background_black)
        }
        
        dialog.show()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    private fun testNotification() {
        try {
            // Create a test notification
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create notification channel if it doesn't exist
            NotificationHelper.createNotificationChannel(this)
            
            // Build the notification
            val notification = NotificationCompat.Builder(this, "didit_reminders")
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification to verify the app can send notifications")
                .setSmallIcon(com.harshal.didit.R.drawable.ic_bell)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            
            // Show the notification
            notificationManager.notify(999, notification)
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error sending test notification", e)
        }
    }
    
    private fun testScheduledReminder() {
        try {
            // Create a test task with reminder in 10 seconds
            val testTask = Task(
                name = "Test Scheduled Reminder",
                notes = "This is a test reminder that should appear in 10 seconds",
                isCompleted = false
            )
            
            // Set reminder time to 10 seconds from now
            val reminderTime = System.currentTimeMillis() + (10 * 1000) // 10 seconds
            
            val updatedTask = testTask.copy(reminderTime = reminderTime)
            
            // Schedule the reminder
            ReminderScheduler.schedule(this, updatedTask)
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error scheduling test reminder", e)
        }
    }


    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}

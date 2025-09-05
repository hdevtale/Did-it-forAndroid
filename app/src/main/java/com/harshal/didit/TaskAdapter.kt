package com.harshal.didit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.RecyclerView
import com.harshal.didit.databinding.ListItemTaskBinding
import java.util.*

class TaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onLogTimeClick: (Task) -> Unit,
    private val onTaskClick: (Task) -> Unit,
    private val onSaveNotes: (Task, String) -> Unit,
    private val onCompleteTask: (Task) -> Unit,
    private val onLongPress: (Task) -> Unit,
    private val onEditReminder: (Task) -> Unit,
    private val onUndoComplete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var isSelectionMode = false
    private val expandedTasks = mutableSetOf<UUID>() // Track expanded tasks by ID
    
    // Performance optimizations for high refresh rate displays
    private val viewHolderCache = mutableMapOf<Int, TaskViewHolder>()

    fun setSelectionMode(enabled: Boolean) {
        val oldMode = isSelectionMode
        isSelectionMode = enabled
        if (oldMode != enabled) {
            notifyDataSetChanged()
        }
    }

    fun animateSelectionMode(enabled: Boolean) {
        val oldMode = isSelectionMode
        isSelectionMode = enabled
        if (oldMode != enabled) {
            notifyDataSetChanged()
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        val oldTasks = tasks
        tasks = newTasks
        
        // Always notify of data change to ensure UI updates properly
        notifyDataSetChanged()
        
        // Force RecyclerView to refresh
        android.util.Log.d("TaskAdapter", "updateTasks called - old size: ${oldTasks.size}, new size: ${newTasks.size}")
    }

    fun updateTask(updatedTask: Task) {
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun getSelectedTasks(): List<Task> {
        return tasks.filter { it.isSelected }
    }

    fun clearAllSelections() {
        tasks.forEach { it.isSelected = false }
        // Force immediate UI update
        notifyDataSetChanged()
    }

    fun isAnyTaskExpanded(): Boolean {
        return expandedTasks.isNotEmpty()
    }

    fun collapseAllExpandedTasks() {
        expandedTasks.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ListItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
        
        // Reset any previous animation states to prevent conflicts
        holder.itemView.alpha = 1f
        holder.itemView.translationY = 0f
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f
        
        // Add subtle entrance animation for new items (only if not already animated)
        if (holder.itemView.alpha == 1f && holder.itemView.translationY == 0f) {
            holder.itemView.alpha = 0.8f
            holder.itemView.translationY = 20f
            holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300) // Quick and smooth
                .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                .start()
        }
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(private val binding: ListItemTaskBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        // Performance optimizations for high refresh rate displays
        
        fun bind(task: Task) {
            try {
                binding.taskNameTextView.text = task.name
                binding.notesEditText.setText(task.notes)
                
                // Add focus change listener to handle keyboard visibility
                binding.notesEditText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        // When notes EditText gains focus, scroll to make it visible
                        scrollToNotesInput()
                        
                        // Ensure the EditText is properly configured for input
                        binding.notesEditText.isEnabled = true
                        binding.notesEditText.isFocusable = true
                        binding.notesEditText.isFocusableInTouchMode = true
                        
                        // Additional scroll for lower items to ensure visibility
                        binding.root.postDelayed({
                            scrollToNotesInput() // This now includes the intelligent faux space call
                        }, 300) // Delay to allow keyboard animation to complete
                    }
                }
                
                // Add click listener to ensure proper focus and keyboard handling
                binding.notesEditText.setOnClickListener {
                    // Ensure the EditText is properly focused and keyboard is shown
                    binding.notesEditText.isEnabled = true
                    binding.notesEditText.isFocusable = true
                    binding.notesEditText.isFocusableInTouchMode = true
                    binding.notesEditText.requestFocus()
                    
                    // Show keyboard
                    val inputMethodManager = binding.root.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    inputMethodManager.showSoftInput(binding.notesEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                }
                
                // Add key listener to ensure proper handling of Enter and Backspace
                binding.notesEditText.setOnKeyListener { _, keyCode, event ->
                    if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                        when (keyCode) {
                            android.view.KeyEvent.KEYCODE_ENTER -> {
                                // Allow Enter key to create new lines
                                android.util.Log.d("TaskAdapter", "Enter key pressed - creating new line")
                                false // Let the default behavior handle it
                            }
                            android.view.KeyEvent.KEYCODE_DEL -> {
                                // Allow Backspace key to delete text
                                android.util.Log.d("TaskAdapter", "Backspace key pressed - deleting text")
                                false // Let the default behavior handle it
                            }
                        }
                    }
                    false // Let the default behavior handle all keys
                }
                
                // Add touch listener to handle independent scrolling (only for scrolling, not text input)
                binding.notesEditText.setOnTouchListener { view, event ->
                    // Only handle scrolling events, let text input work normally
                    if (event.action == android.view.MotionEvent.ACTION_DOWN && shouldUseIndependentNotesScrolling()) {
                        // Let the notes area handle its own scrolling
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                        android.util.Log.d("TaskAdapter", "Independent notes scrolling enabled")
                    } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                        // Re-enable parent scroll interception
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false // Let the event continue to the EditText for normal text input
                }
                
                // Add touch listener to the notes scroll view
                val notesScrollView = binding.root.findViewById<androidx.core.widget.NestedScrollView>(
                    com.harshal.didit.R.id.notesScrollView
                )
                notesScrollView?.setOnTouchListener { view, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            if (shouldUseIndependentNotesScrolling()) {
                                // Prevent parent scroll views from intercepting touch events
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                android.util.Log.d("TaskAdapter", "Notes scroll view touch - independent scrolling")
                            }
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            // Re-enable parent scroll interception
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false // Let the scroll view handle the event
                }
                
                // Handle task completion state
                if (task.isCompleted) {
                    // Completed task styling - but allow notes editing
                    binding.taskNameTextView.alpha = 0.7f
                    binding.notesEditText.alpha = 0.8f // Slightly more visible for editing
                    binding.notesEditText.isEnabled = true // Enable notes editing for completed tasks
                    binding.saveNotesButton.visibility = View.VISIBLE // Show save button for completed tasks
                    binding.editReminderButton.visibility = View.GONE // Hide reminder button for completed tasks
                    
                    // Remove hint text to avoid ghosting/doubling issues
                    binding.notesEditText.hint = ""
                    
                    // Add subtle visual styling for completed task notes
                    binding.notesEditText.setBackgroundColor(
                        android.graphics.Color.parseColor("#1A1A1A") // Darker background for completed tasks
                    )
                    binding.notesEditText.setTextColor(
                        android.graphics.Color.parseColor("#CCCCCC") // Slightly dimmed text color
                    )
                    
                    // Hide complete task button for completed tasks
                    binding.logTimeButton.visibility = View.GONE
                    
                    // Show undo button for completed tasks
                    binding.undoButton.visibility = View.VISIBLE
                    
                    // Show completion time for completed tasks
                    binding.timeElapsedTextView.text = "Completed at ${formatTimeElapsed(task.completedTimestamp)}"
                    binding.timeElapsedTextView.visibility = View.VISIBLE
                    
                    // Add subtle visual cue that completed tasks can be expanded
                    binding.mainContentLayout.alpha = 0.8f
                } else {
                    // Ongoing task styling
                    binding.taskNameTextView.alpha = 1.0f
                    binding.notesEditText.alpha = 1.0f
                    binding.notesEditText.isEnabled = true
                    binding.saveNotesButton.visibility = View.VISIBLE
                    binding.editReminderButton.visibility = View.VISIBLE
                    
                    // Remove hint text to avoid ghosting/doubling issues
                    binding.notesEditText.hint = ""
                    
                    // Ensure ongoing tasks have default styling
                    binding.notesEditText.setBackgroundColor(
                        android.graphics.Color.parseColor("#2A2A2A") // Default background for ongoing tasks
                    )
                    binding.notesEditText.setTextColor(
                        android.graphics.Color.parseColor("#FFFFFF") // Default text color
                    )
                    
                    // Show complete task button for ongoing tasks
                    binding.logTimeButton.visibility = View.VISIBLE
                    
                    // Hide undo button for ongoing tasks
                    binding.undoButton.visibility = View.GONE
                    
                    // Show last logged time or creation time
                    if (task.lastLoggedTimestamp > 0) {
                        binding.timeElapsedTextView.text = "Last logged at ${formatTimeElapsed(task.lastLoggedTimestamp)}"
                    } else {
                        binding.timeElapsedTextView.text = "Created at ${formatTimeElapsed(task.creationTimestamp)}"
                    }
                    binding.timeElapsedTextView.visibility = View.VISIBLE
                    
                    // Show reminder time if reminder is set
                    if (task.reminderTime != null && task.reminderTime > 0) {
                        binding.reminderTimeTextView.text = "Reminder at ${formatTime(task.reminderTime)}"
                        binding.reminderTimeTextView.visibility = View.VISIBLE
                        binding.reminderIcon.visibility = View.VISIBLE
                        android.util.Log.d("TaskAdapter", "Showing reminder for task: ${task.name}, reminderTime: ${task.reminderTime}")
                    } else {
                        binding.reminderTimeTextView.visibility = View.GONE
                        binding.reminderIcon.visibility = View.GONE
                        android.util.Log.d("TaskAdapter", "Hiding reminder for task: ${task.name}, reminderTime: ${task.reminderTime}")
                    }
                    
                    // Reset background for ongoing tasks
                    binding.mainContentLayout.setBackgroundResource(android.R.color.transparent)
                }
                
                // Handle selection mode
                if (isSelectionMode) {
                    binding.editReminderButton.visibility = View.GONE
                    binding.saveNotesButton.visibility = View.GONE
                    binding.logTimeButton.visibility = View.GONE
                    // Don't hide undo button in selection mode for completed tasks
                    
                    if (task.isSelected) {
                        // Apply selection highlight to the entire card
                        binding.root.setBackgroundResource(com.harshal.didit.R.drawable.task_selection_highlight)
                        binding.root.setStrokeWidth(3)
                        binding.root.setStrokeColor(android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#FF4444")
                        ))
                    } else {
                        // Reset card appearance to default - let the layout handle the styling
                        binding.root.setBackgroundResource(android.R.color.transparent)
                        // Don't override stroke - let the layout handle it
                    }
                } else {
                    // Not in selection mode - ensure card appearance is reset to default
                    binding.root.setBackgroundResource(android.R.color.transparent)
                    // Don't override stroke - let the layout handle it
                }
                
                // Handle notes expansion
                val isExpanded = expandedTasks.contains(task.id)
                if (isExpanded) {
                    binding.notesSectionLayout.visibility = View.VISIBLE
                    binding.notesSectionLayout.alpha = 1f
                    binding.notesSectionLayout.translationY = 0f
                } else {
                    binding.notesSectionLayout.visibility = View.GONE
                    binding.notesSectionLayout.alpha = 0f
                    binding.notesSectionLayout.translationY = -20f
                }
                
                // Set saveNotesButton visibility if not in selection mode (for both ongoing and completed tasks)
                if (!isSelectionMode) {
                    if (isExpanded) {
                        binding.saveNotesButton.visibility = View.VISIBLE
                        binding.saveNotesButton.alpha = 1f
                    } else {
                        binding.saveNotesButton.visibility = View.GONE
                        binding.saveNotesButton.alpha = 0f
                    }
                }
                
                // Set up click listeners
                setupClickListeners(task)
            } catch (e: Exception) {
                e.printStackTrace()
                // Log error silently for professional appearance
            }
        }
        
        private fun setupClickListeners(task: Task) {
            // Set click listener for main content
            binding.mainContentLayout.setOnClickListener {
                if (isSelectionMode) {
                    // Toggle selection
                    task.isSelected = !task.isSelected
                    notifyItemChanged(adapterPosition)
                } else {
                    // Normal mode, expand/collapse notes for all tasks
                    toggleExpansion(task)
                    onTaskClick(task)
                }
            }
            
            // Set up long press listener
            binding.mainContentLayout.setOnLongClickListener {
                onLongPress(task)
                true
            }
            
            // Set up undo button click listener
            binding.undoButton.setOnClickListener {
                // Haptic feedback
                binding.undoButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                
                // Animate button press
                binding.undoButton.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(300) // Much smoother and slower
                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                    .withEndAction {
                        binding.undoButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(300) // Much smoother and slower
                            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                            .start()
                    }
                    .start()
                
                onUndoComplete(task)
            }
            
            // Set up log time button click listener (main red button)
            binding.logTimeButton.setOnClickListener {
                // Haptic feedback
                binding.logTimeButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onCompleteTask(task)
            }
            
            // Set up complete task button click listener (in notes section)
            binding.completeTaskButton.setOnClickListener {
                // Haptic feedback
                binding.completeTaskButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onLogTimeClick(task)
            }
            
            // Set up save notes button click listener
            binding.saveNotesButton.setOnClickListener {
                // Haptic feedback
                binding.saveNotesButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                val notes = binding.notesEditText.text.toString()
                onSaveNotes(task, notes)
                
                // Hide keyboard and clear focus to allow UI to return to normal position
                val inputMethodManager = binding.root.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.notesEditText.windowToken, 0)
                binding.notesEditText.clearFocus()
                
                // Ensure the faux space disappears smoothly
                binding.root.postDelayed({
                    try {
                        val activity = binding.root.context as? com.harshal.didit.MainActivity
                        // The window insets listener will automatically handle restoring the UI
                        android.util.Log.d("TaskAdapter", "Keyboard hidden - faux space will be restored automatically")
                    } catch (e: Exception) {
                        android.util.Log.e("TaskAdapter", "Error handling keyboard hide: ${e.message}")
                    }
                }, 100)
                
                // Show animated "saved" confirmation
                showSaveConfirmation()
            }
            
            // Set up edit reminder button click listener
            binding.editReminderButton.setOnClickListener {
                // Haptic feedback
                binding.editReminderButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onEditReminder(task)
            }
        }
        
        private fun toggleExpansion(task: Task) {
            val wasExpanded = expandedTasks.contains(task.id)
            if (wasExpanded) {
                // If this task is already expanded, collapse it
                expandedTasks.remove(task.id)
            } else {
                // If this task is not expanded, collapse all others first, then expand this one
                expandedTasks.clear() // Clear all expanded tasks
                expandedTasks.add(task.id) // Add only this task
            }
            
            // Simple show/hide without complex animations
            if (wasExpanded) {
                binding.notesSectionLayout.visibility = View.GONE
            } else {
                binding.notesSectionLayout.visibility = View.VISIBLE
            }
            
            // Update save button visibility (for both ongoing and completed tasks)
            if (!isSelectionMode) {
                if (wasExpanded) {
                    binding.saveNotesButton.visibility = View.GONE
                } else {
                    binding.saveNotesButton.visibility = View.VISIBLE
                }
            }
            
            // Notify adapter to refresh the UI
            notifyDataSetChanged()
        }
        
        private fun scrollToNotesInput() {
            // Intelligent scrolling that creates faux space around typing area
            binding.root.post {
                try {
                    // Always call the intelligent faux space method for UI shifting
                    try {
                        val activity = binding.root.context as? com.harshal.didit.MainActivity
                        activity?.createIntelligentFauxSpace(binding.notesEditText)
                        android.util.Log.d("TaskAdapter", "Called intelligent faux space for UI shifting")
                    } catch (e: Exception) {
                        android.util.Log.e("TaskAdapter", "Error calling intelligent faux space: ${e.message}")
                    }
                    
                    // Check if notes content requires independent scrolling
                    if (shouldUseIndependentNotesScrolling()) {
                        android.util.Log.d("TaskAdapter", "Using independent notes scrolling - main UI scroll prevented")
                        return@post
                    }
                    
                    // Get the task card (MaterialCardView)
                    val taskCard = binding.root as? com.google.android.material.card.MaterialCardView
                    if (taskCard != null) {
                        // Find the NestedScrollView by traversing up the hierarchy
                        var parent = taskCard.parent as? android.view.View
                        while (parent != null && parent !is androidx.core.widget.NestedScrollView) {
                            parent = parent.parent as? android.view.View
                        }
                        
                        if (parent is androidx.core.widget.NestedScrollView) {
                            // Get screen dimensions
                            val screenHeight = parent.context.resources.displayMetrics.heightPixels
                            val keyboardHeight = screenHeight * 0.4f // Assume keyboard takes ~40% of screen
                            
                            // Calculate the notes input position within the task card
                            val notesInput = binding.notesEditText
                            val notesInputLocation = IntArray(2)
                            notesInput.getLocationInWindow(notesInputLocation)
                            
                            // Calculate minimal scroll position to make notes input visible
                            val notesInputTop = notesInputLocation[1]
                            val availableSpaceAboveKeyboard = screenHeight - keyboardHeight
                            val targetTop = availableSpaceAboveKeyboard * 0.3f // Only scroll to 30% of available space
                            
                            // Calculate minimal scroll offset
                            val scrollOffset = (notesInputTop - targetTop).toInt()
                            
                            if (scrollOffset > 0) {
                                // Smooth scroll with reduced offset to prevent excessive shifting
                                val reducedScrollOffset = (scrollOffset * 0.5f).toInt() // Reduce scroll by 50%
                                parent.smoothScrollBy(0, reducedScrollOffset)
                                android.util.Log.d("TaskAdapter", "Minimal scroll to notes input with reduced offset: $reducedScrollOffset")
                            } else {
                                // If notes input is already well positioned, just ensure it's visible
                                val minScroll = taskCard.top - 50 // Reduced buffer from top
                                if (minScroll > 0) {
                                    parent.smoothScrollBy(0, minScroll)
                                    android.util.Log.d("TaskAdapter", "Ensured notes input visibility with minimal scroll: $minScroll")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TaskAdapter", "Error scrolling to notes input: ${e.message}")
                }
            }
        }
        
        /**
         * Determines if the notes content is long enough to require independent scrolling
         * Returns true if notes should scroll independently, false if main UI should scroll
         */
        private fun shouldUseIndependentNotesScrolling(): Boolean {
            try {
                val notesEditText = binding.notesEditText
                
                // Get the notes scroll view directly by ID
                val notesScrollView = binding.root.findViewById<androidx.core.widget.NestedScrollView>(
                    com.harshal.didit.R.id.notesScrollView
                )
                
                if (notesScrollView == null) {
                    android.util.Log.w("TaskAdapter", "Notes scroll view not found")
                    return false
                }
                
                // Measure the content height
                notesEditText.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(notesEditText.width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                )
                
                val contentHeight = notesEditText.measuredHeight
                val availableHeight = notesScrollView.height
                
                // If content height exceeds available height, use independent scrolling
                val shouldScrollIndependently = contentHeight > availableHeight
                
                android.util.Log.d("TaskAdapter", "Notes content height: $contentHeight, available height: $availableHeight, independent scroll: $shouldScrollIndependently")
                
                return shouldScrollIndependently
            } catch (e: Exception) {
                android.util.Log.e("TaskAdapter", "Error checking notes scroll requirement: ${e.message}")
                return false
            }
        }
        
        /**
         * Shows an animated "saved" confirmation on the save button
         */
        private fun showSaveConfirmation() {
            try {
                val saveButton = binding.saveNotesButton
                val originalText = saveButton.text.toString()
                
                // First, scale down the button
                saveButton.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(300) // Much smoother and slower
                    .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                    .withEndAction {
                        // Change text to "saved" and change colors to theme (red background, black text)
                        saveButton.text = "saved"
                        saveButton.setTextColor(android.graphics.Color.parseColor("#000000")) // Black text
                        saveButton.setBackgroundColor(android.graphics.Color.parseColor("#FF4444")) // Red background
                        
                        // Scale back up with the new text
                        saveButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(300) // Much smoother and slower
                            .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                            .withEndAction {
                                // After a delay, restore original text and color
                                saveButton.postDelayed({
                                    saveButton.animate()
                                        .scaleX(0.9f)
                                        .scaleY(0.9f)
                                        .setDuration(300) // Much smoother and slower
                                        .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                        .withEndAction {
                                            // Restore original text and colors
                                            saveButton.text = originalText
                                            saveButton.setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // White text
                                            saveButton.setBackgroundColor(android.graphics.Color.parseColor("#FF4444")) // Red background (original)
                                            
                                            // Scale back to normal
                                            saveButton.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(300) // Much smoother and slower
                                                .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                                .start()
                                        }
                                        .start()
                                }, 2000) // Show "saved" for 2 seconds
                            }
                            .start()
                    }
                    .start()
                
                android.util.Log.d("TaskAdapter", "Save confirmation animation started")
            } catch (e: Exception) {
                android.util.Log.e("TaskAdapter", "Error showing save confirmation: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun formatTimeElapsed(timestamp: Long): String {
        if (timestamp == 0L) return "Never"
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 1 -> formatDateWithTime(timestamp) // Show full date for older items
            days == 1L -> "Yesterday ${formatTime(timestamp)}" // Show yesterday with time
            else -> formatTime(timestamp) // Show just the time for recent items
        }
    }
    
    private fun formatTime(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        val hour = calendar.get(java.util.Calendar.HOUR)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val amPm = calendar.get(java.util.Calendar.AM_PM)
        
        val hour12 = if (hour == 0) 12 else hour
        val amPmText = if (amPm == java.util.Calendar.AM) "AM" else "PM"
        val minuteStr = if (minute < 10) "0$minute" else minute.toString()
        
        return "$hour12:$minuteStr $amPmText"
    }
    
    private fun formatDateWithTime(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        val month = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar months are 0-based
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val year = calendar.get(java.util.Calendar.YEAR)
        val time = formatTime(timestamp)
        
        return "$month/$day/$year $time"
    }
}
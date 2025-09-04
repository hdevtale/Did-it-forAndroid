
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
                .setDuration(300)
                .setInterpolator(android.view.animation.DecelerateInterpolator(1.2f))
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
                        
                        // Post a delayed action to ensure keyboard appears
                        binding.notesEditText.post {
                            binding.notesEditText.requestFocus()
                        }
                    }
                }
                
                // Handle task completion state
                if (task.isCompleted) {
                    // Completed task styling
                    binding.taskNameTextView.alpha = 0.7f
                    binding.notesEditText.alpha = 0.7f
                    binding.notesEditText.isEnabled = false
                    binding.saveNotesButton.visibility = View.GONE
                    binding.editReminderButton.visibility = View.GONE
                    
                    // Hide complete task button for completed tasks
                    binding.logTimeButton.visibility = View.GONE
                    
                    // Show undo button for completed tasks
                    binding.undoButton.visibility = View.VISIBLE
                    
                    // Show completion time for completed tasks
                    binding.timeElapsedTextView.text = "Completed: ${formatTimeElapsed(task.completedTimestamp)}"
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
                    
                    // Show complete task button for ongoing tasks
                    binding.logTimeButton.visibility = View.VISIBLE
                    
                    // Hide undo button for ongoing tasks
                    binding.undoButton.visibility = View.GONE
                    
                    // Show last logged time or creation time
                    if (task.lastLoggedTimestamp > 0) {
                        binding.timeElapsedTextView.text = "Last logged: ${formatTimeElapsed(task.lastLoggedTimestamp)}"
                    } else {
                        binding.timeElapsedTextView.text = "Created: ${formatTimeElapsed(task.creationTimestamp)}"
                    }
                    binding.timeElapsedTextView.visibility = View.VISIBLE
                    
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
                
                // Only set saveNotesButton visibility if not in selection mode and task is not completed
                if (!isSelectionMode && !task.isCompleted) {
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
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction {
                        binding.undoButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
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
                
                // Keep focus on the EditText to maintain keyboard visibility
                binding.notesEditText.requestFocus()
                
                // Show a subtle visual feedback instead of closing keyboard
                binding.saveNotesButton.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        binding.saveNotesButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
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
                expandedTasks.remove(task.id)
            } else {
                expandedTasks.add(task.id)
            }
            
            // Use smooth animation for expansion/collapse
            if (wasExpanded) {
                // Collapsing - animate out
                binding.notesSectionLayout.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(200)
                    .withEndAction {
                        binding.notesSectionLayout.visibility = View.GONE
                    }
                    .start()
            } else {
                // Expanding - animate in
                binding.notesSectionLayout.visibility = View.VISIBLE
                binding.notesSectionLayout.alpha = 0f
                binding.notesSectionLayout.translationY = -20f
                binding.notesSectionLayout.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(150) // Faster animation for high refresh rate
                    .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
                    .start()
            }
            
            // Update save button visibility with animation
            if (!isSelectionMode && !task.isCompleted) {
                if (wasExpanded) {
                    binding.saveNotesButton.animate()
                        .alpha(0f)
                        .setDuration(100) // Faster animation for high refresh rate
                        .setInterpolator(android.view.animation.AccelerateInterpolator())
                        .withEndAction {
                            binding.saveNotesButton.visibility = View.GONE
                        }
                        .start()
                } else {
                    binding.saveNotesButton.visibility = View.VISIBLE
                    binding.saveNotesButton.alpha = 0f
                    binding.saveNotesButton.animate()
                        .alpha(1f)
                        .setDuration(100) // Faster animation for high refresh rate
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }
            }
        }
        
        private fun scrollToNotesInput() {
            // Simple approach: scroll the current view into view
            binding.root.post {
                try {
                    // Get the task card (MaterialCardView)
                    val taskCard = binding.root as? com.google.android.material.card.MaterialCardView
                    if (taskCard != null) {
                        // Find the NestedScrollView by traversing up the hierarchy
                        var parent = taskCard.parent as? android.view.View
                        while (parent != null && parent !is androidx.core.widget.NestedScrollView) {
                            parent = parent.parent as? android.view.View
                        }
                        
                        if (parent is androidx.core.widget.NestedScrollView) {
                            // Scroll to bring the task card into view
                            val scrollY = taskCard.top - 200 // 200dp offset from top
                            if (scrollY > 0) {
                                parent.smoothScrollBy(0, scrollY)
                                android.util.Log.d("TaskAdapter", "Scrolled to notes input: $scrollY")
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TaskAdapter", "Error scrolling to notes input: ${e.message}")
                }
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
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    }
}
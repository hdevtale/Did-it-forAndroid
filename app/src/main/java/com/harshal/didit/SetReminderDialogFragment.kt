package com.harshal.didit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class SetReminderDialogFragment : DialogFragment() {
    
    interface ReminderDialogListener {
        fun onReminderSet(task: Task, reminderTime: Long)
        fun onReminderRemoved(task: Task)
    }
    
    private var listener: ReminderDialogListener? = null
    private var task: Task? = null
    
    // Date selection
    private lateinit var selectedDateText: TextView
    private lateinit var datePickerButton: ImageButton
    private var selectedDate: Calendar = Calendar.getInstance()
    
    // Time selection spinners
    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner
    private lateinit var ampmSpinner: Spinner
    
    // Buttons
    private lateinit var saveButton: MaterialButton
    private lateinit var removeButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
    companion object {
        fun newInstance(task: Task, listener: ReminderDialogListener): SetReminderDialogFragment {
            return SetReminderDialogFragment().apply {
                this.task = task
                this.listener = listener
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DarkDialogTheme)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_set_reminder, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("SetReminderDialog", "Starting view setup...")
            initializeViews(view)
            android.util.Log.d("SetReminderDialog", "Views initialized successfully")
            
            setupDatePicker()
            android.util.Log.d("SetReminderDialog", "Date picker setup successfully")
            
            setupTimeSpinners()
            android.util.Log.d("SetReminderDialog", "Time spinners setup successfully")
            
            setupButtons()
            android.util.Log.d("SetReminderDialog", "Buttons setup successfully")
            
            populateExistingData()
            android.util.Log.d("SetReminderDialog", "Data populated successfully")
            
            android.util.Log.d("SetReminderDialog", "Dialog setup complete!")
        } catch (e: Exception) {
            android.util.Log.e("SetReminderDialog", "Error setting up reminder dialog", e)
            dismiss()
        }
    }
    
    private fun initializeViews(view: View) {
        // Date selection
        selectedDateText = view.findViewById(R.id.selectedDateText)
        datePickerButton = view.findViewById(R.id.datePickerButton)
        
        // Time spinners
        hourSpinner = view.findViewById(R.id.hourSpinner)
        minuteSpinner = view.findViewById(R.id.minuteSpinner)
        ampmSpinner = view.findViewById(R.id.ampmSpinner)
        
        // Buttons
        saveButton = view.findViewById(R.id.saveButton)
        removeButton = view.findViewById(R.id.removeButton)
        cancelButton = view.findViewById(R.id.cancelButton)
    }
    
    private fun setupDatePicker() {
        // Set initial date text
        updateDateText()
        
        // Set up date picker button click
        datePickerButton.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DarkDialogTheme,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        
        // Apply dark theme to the dialog
        datePickerDialog.setOnShowListener {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.white, null))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.white, null))
        }
        
        datePickerDialog.show()
    }
    
    private fun updateDateText() {
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        
        val dateText = when {
            selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            selectedDate.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> "Tomorrow"
            else -> {
                val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                formatter.format(selectedDate.time)
            }
        }
        
        selectedDateText.text = dateText
    }
    
    private fun setupTimeSpinners() {
        // Hour spinner (1-12)
        val hours = (1..12).map { it.toString() }.toTypedArray()
        val hourAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hours)
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hourSpinner.adapter = hourAdapter
        
        // Minute spinner (00-59)
        val minutes = (0..59).map { String.format("%02d", it) }.toTypedArray()
        val minuteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, minutes)
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        minuteSpinner.adapter = minuteAdapter
        
        // AM/PM spinner
        val ampm = arrayOf("AM", "PM")
        val ampmAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ampm)
        ampmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ampmSpinner.adapter = ampmAdapter
        
        // Set current time as default
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentAmPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) 0 else 1
        
        hourSpinner.setSelection(if (currentHour == 0) 11 else currentHour - 1)
        minuteSpinner.setSelection(currentMinute)
        ampmSpinner.setSelection(currentAmPm)
    }
    
    private fun setupButtons() {
        saveButton.setOnClickListener {
            saveReminder()
        }
        
        removeButton.setOnClickListener {
            removeReminder()
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun populateExistingData() {
        task?.let { currentTask ->
            // Set existing reminder time if available
            if (currentTask.reminderTime != null) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = currentTask.reminderTime
                
                // Set the selected date
                selectedDate.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                selectedDate.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                selectedDate.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                updateDateText()
                
                val hour = calendar.get(Calendar.HOUR)
                val minute = calendar.get(Calendar.MINUTE)
                val amPm = calendar.get(Calendar.AM_PM)
                
                hourSpinner.setSelection(if (hour == 0) 11 else hour - 1)
                minuteSpinner.setSelection(minute)
                ampmSpinner.setSelection(amPm)
            }
        }
    }
    
    private fun saveReminder() {
        try {
            val task = this.task ?: return
            
            // Get time from spinners
            val hour = hourSpinner.selectedItemPosition + 1
            val minute = minuteSpinner.selectedItemPosition
            val amPm = ampmSpinner.selectedItemPosition
            
            // Convert to 24-hour format
            val hour24 = when {
                amPm == 0 && hour == 12 -> 0  // 12 AM = 0
                amPm == 1 && hour != 12 -> hour + 12  // PM hours (except 12 PM)
                else -> hour
            }
            
            android.util.Log.d("SetReminderDialog", "Time selected: $hour24:$minute")
            
            // Create calendar with selected date and time
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, hour24)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val reminderTime = calendar.timeInMillis
            
            // If the selected date/time has already passed, set it for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val adjustedReminderTime = calendar.timeInMillis
                android.util.Log.d("SetReminderDialog", "Adjusted reminder time: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(adjustedReminderTime)}")
                
                android.util.Log.d("SetReminderDialog", "Saving reminder successfully")
                try {
                    listener?.onReminderSet(task, adjustedReminderTime)
                    dismiss()
                } catch (e: Exception) {
                    android.util.Log.e("SetReminderDialog", "Error in listener callback", e)
                }
            } else {
                android.util.Log.d("SetReminderDialog", "Saving reminder successfully")
                try {
                    listener?.onReminderSet(task, reminderTime)
                    dismiss()
                } catch (e: Exception) {
                    android.util.Log.e("SetReminderDialog", "Error in listener callback", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SetReminderDialog", "Error saving reminder", e)
        }
    }
    
    private fun removeReminder() {
        val task = this.task ?: return
        listener?.onReminderRemoved(task)
        dismiss()
    }
}

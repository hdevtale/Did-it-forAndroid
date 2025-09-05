package com.harshal.didit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.harshal.didit.databinding.DialogAddTaskBinding

class AddTaskDialogFragment : DialogFragment() {
    
    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!
    
    private var onTaskAdded: ((String, String) -> Unit)? = null
    
    companion object {
        fun newInstance(onTaskAdded: (String, String) -> Unit): AddTaskDialogFragment {
            return AddTaskDialogFragment().apply {
                this.onTaskAdded = onTaskAdded
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up the dialog
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // For now, just show a simple dialog since layout references are missing
        // TODO: Implement proper dialog layout
        onTaskAdded?.invoke("New Task", "")
        dismiss()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
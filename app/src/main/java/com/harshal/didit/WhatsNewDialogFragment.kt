package com.harshal.didit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton

class WhatsNewDialogFragment : DialogFragment() {
    
    interface WhatsNewDialogListener {
        fun onWhatsNewDismissed()
    }
    
    private var listener: WhatsNewDialogListener? = null
    
    companion object {
        fun newInstance(listener: WhatsNewDialogListener? = null): WhatsNewDialogFragment {
            val fragment = WhatsNewDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_whats_new, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up close button
        view.findViewById<View>(R.id.closeButton).setOnClickListener {
            dismissDialog()
        }
        
        // Set up continue button
        view.findViewById<MaterialButton>(R.id.continueButton).setOnClickListener {
            dismissDialog()
        }
        
        // Make dialog non-cancelable by touching outside
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
    }
    
    private fun dismissDialog() {
        listener?.onWhatsNewDismissed()
        dismiss()
    }
    
    override fun onStart() {
        super.onStart()
        // Make dialog full width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

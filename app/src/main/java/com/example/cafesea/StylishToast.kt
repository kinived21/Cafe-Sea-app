package com.example.cafesea

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

object StylishToast {
    // Change parameter type to Activity
    fun show(activity: Activity, message: String, isSuccess: Boolean = true) {
        // We use the activity to get the layoutInflater
        val layout = activity.layoutInflater.inflate(R.layout.custom_toast, null)

        val text: TextView = layout.findViewById(R.id.toastText)
        text.text = message

        val icon: ImageView = layout.findViewById(R.id.toastIcon)
        if (isSuccess) {
            icon.setImageResource(R.drawable.cafelogo)
        } else {
            icon.setImageResource(R.drawable.baseline_error_24)
        }

        val toast = Toast(activity.applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
}
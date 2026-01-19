package com.example.cafesea

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cafesea.databinding.ActivitySettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    // Permission launcher to handle the "Allow/Deny" result
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            saveNotificationPreference(true)
            StylishToast.show(this, "Notifications Enabled", true)
        } else {
            // If denied, turn the switch back off
            binding.switchNotifications.isChecked = false
            saveNotificationPreference(false)
            StylishToast.show(this, "Notifications Disabled", true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("CafeSeaSettings", Context.MODE_PRIVATE)

        // 1. Load Initial State (Default to false for safety)
        binding.switchNotifications.isChecked = sharedPref.getBoolean("notifications", false)

        binding.btnBackSettings.setOnClickListener { finish() }

        // 2. Notification Switch Logic
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleNotificationEnable()
            } else {
                saveNotificationPreference(false)
                StylishToast.show(this,"Notifications Disabled", true)
            }
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun handleNotificationEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    saveNotificationPreference(true)
                }
                else -> {
                    // Trigger the system permission popup
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 and below: permission is granted automatically
            saveNotificationPreference(true)
        }
    }

    private fun saveNotificationPreference(enabled: Boolean) {
        val sharedPref = getSharedPreferences("CafeSeaSettings", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("notifications", enabled).apply()
    }

    private fun showChangePasswordDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        dialog.setContentView(view)

        val btnUpdate = view.findViewById<android.widget.Button>(R.id.btnUpdatePassword)
        val etCurrent = view.findViewById<android.widget.EditText>(R.id.etCurrentPassword)
        val etNew = view.findViewById<android.widget.EditText>(R.id.etNewPassword)

        btnUpdate.setOnClickListener {
            val currentPwd = etCurrent.text.toString()
            val newPwd = etNew.text.toString()

            if (currentPwd.isNotEmpty() && newPwd.length >= 6) {
                updatePasswordInFirebase(newPwd, dialog)
            } else {
                StylishToast.show(this, "Password Must be at least 6 characters", true)
            }
        }
        dialog.show()
    }

    private fun updatePasswordInFirebase(newPwd: String, dialog: BottomSheetDialog) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPwd)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                StylishToast.show(this, "Password Updated Successfully", true)
                dialog.dismiss()
            } else {
                StylishToast.show(this, "Error : ${task.exception?.message}", true)
            }
        }
    }
}
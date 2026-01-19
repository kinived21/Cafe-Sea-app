package com.example.cafesea

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafesea.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("Users/User_01/Profile")

    private var selectedAvatarName: String = "person"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGenderSpinner()
        loadCurrentData()

        binding.btnBack.setOnClickListener { finish() }

        // 1. Avatar Picker
        binding.btnChooseAvatar.setOnClickListener { showAvatarPickerDialog() }

        // 2. Date Picker
        binding.etEditDOB.setOnClickListener { showDatePicker() }

        // 3. Change Email Logic
        binding.tvChangeEmail.setOnClickListener {
            showUpdateDialog("Email") { newValue -> updateEmail(newValue) }
        }

        // 4. Change Phone Logic
        binding.tvChangePhone.setOnClickListener {
            showUpdateDialog("Phone") { newValue -> binding.etEditPhone.setText(newValue) }
        }

        // 5. Final Save to Firebase
        binding.btnUpdateProfile.setOnClickListener { updateProfile() }
    }

    private fun loadCurrentData() {
        // First, load from Firebase Auth (Login Credentials)
        val user = auth.currentUser
        binding.etEditEmail.setText(user?.email ?: "No Email")
        binding.etEditPhone.setText(user?.phoneNumber ?: "No Phone")

        // Second, load from Realtime Database (Profile Details)
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                binding.etEditName.setText(snapshot.child("name").value.toString())
                binding.etEditDOB.setText(snapshot.child("dob").value.toString())

                val gender = snapshot.child("gender").value.toString()
                val genderPos = (binding.spinnerGender.adapter as ArrayAdapter<String>).getPosition(gender)
                binding.spinnerGender.setSelection(genderPos)

                selectedAvatarName = snapshot.child("profileImage").value.toString()
                val resId = resources.getIdentifier(selectedAvatarName, "drawable", packageName)
                binding.ivEditProfileImage.setImageResource(if (resId != 0) resId else R.drawable.person)
            }
        }
    }

    private fun showUpdateDialog(type: String, onConfirm: (String) -> Unit) {
        // Inflate the layout we just created
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_field, null)
        val etInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDialogInput)

        // Set hint based on whether it's Email or Phone
        etInput.hint = "New $type"
        if (type == "Phone") etInput.inputType = android.text.InputType.TYPE_CLASS_PHONE

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newValue = etInput.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onConfirm(newValue)
                } else {
                    StylishToast.show(this, "Please enter a new $type", true)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEmail(newEmail: String) {
        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                StylishToast.show(this, "Verification Email Sent to $newEmail", true)
                binding.etEditEmail.setText(newEmail)
            } else {
                StylishToast.show(this, "Error : ${task.exception?.message}", true)
            }
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            binding.etEditDOB.setText("$d/${m + 1}/$y")
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showAvatarPickerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_avatar_picker, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val rvAvatars = dialogView.findViewById<RecyclerView>(R.id.rvAvatarOptions)
        val avatarList = listOf(R.drawable.pro1, R.drawable.pro2, R.drawable.pro3, R.drawable.pro4, R.drawable.pro5, R.drawable.pro6)

        rvAvatars.layoutManager = GridLayoutManager(this, 3)
        rvAvatars.adapter = AvatarAdapter(avatarList) { selectedResId ->
            selectedAvatarName = resources.getResourceEntryName(selectedResId)
            binding.ivEditProfileImage.setImageResource(selectedResId)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = adapter
    }

    private fun updateProfile() {
        val updatedData = mapOf(
            "name" to binding.etEditName.text.toString(),
            "phone" to binding.etEditPhone.text.toString(),
            "email" to binding.etEditEmail.text.toString(),
            "dob" to binding.etEditDOB.text.toString(),
            "gender" to binding.spinnerGender.selectedItem.toString(),
            "profileImage" to selectedAvatarName
        )

        database.updateChildren(updatedData).addOnSuccessListener {
            StylishToast.show(this, "Profile Updated Successfully", true)
            finish()
        }
    }
}
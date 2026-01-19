package com.example.cafesea

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cafesea.databinding.ActivityFeedbackBinding
import com.google.firebase.database.FirebaseDatabase

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private val database = FirebaseDatabase.getInstance().getReference("Feedback")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }


        binding.btnSubmitFeedback.setOnClickListener {
            val rating = binding.ratingBar.rating // e.g., 4.0
            val suggestions = binding.etFeedbackSuggestions.text.toString()

            if (suggestions.isEmpty()) {
                StylishToast.show(this, "Please enter your suggestions", true)
            } else {
                saveFeedbackToFirebase(rating, suggestions)
            }
        }
    }

    private fun saveFeedbackToFirebase(stars: Float, text: String) {
        val feedbackId = database.push().key ?: return
        val data = mapOf(
            "rating" to stars,
            "suggestion" to text,
            "timestamp" to System.currentTimeMillis()
        )

        database.child(feedbackId).setValue(data).addOnSuccessListener {
            StylishToast.show(this, "Thank you for your feedback!", true)
            finish() // Close activity and return to Profile
        }
    }
}
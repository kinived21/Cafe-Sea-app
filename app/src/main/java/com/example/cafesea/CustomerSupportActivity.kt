package com.example.cafesea

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cafesea.databinding.ActivityCustomerSupportBinding

class CustomerSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackSupport.setOnClickListener { finish() }

        // Logic for Email Support
        binding.cardEmailSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@cafesea.com")
                putExtra(Intent.EXTRA_SUBJECT, "Customer Support Request")
            }
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        }

        // Logic for Phone Support
        binding.cardCallSupport.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+1234567890")
            }
            startActivity(callIntent)
        }
    }
}
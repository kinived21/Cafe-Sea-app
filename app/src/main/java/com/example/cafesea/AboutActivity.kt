package com.example.cafesea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cafesea.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackAbout.setOnClickListener {
            finish() // Goes back to Profile Fragment
        }
    }
}
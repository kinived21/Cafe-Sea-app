package com.example.cafesea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.cafesea.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. THIS IS THE LINK: Show HomeFragment as soon as the app opens
        replaceFragment(HomeFragment())

        // 2. Set Clicks for your Red Bottom Bar buttons
        binding.btnHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        binding.btnFavorite.setOnClickListener {
            replaceFragment(FavoriteFragment())
        }

        binding.btnCart.setOnClickListener {
            replaceFragment(CartFragment())
        }

        binding.btnProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
        }
    }

    // 3. The Function that swaps the fragments
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // This line replaces whatever is in the FrameLayout with the new fragment
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)

        fragmentTransaction.commit()
    }
}
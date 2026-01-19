package com.example.cafesea

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.cafesea.databinding.ActivityDetailBinding
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private var quantity = 1
    private var isFavorite = false
    private val fbHelper = FirebaseHelper()

    // Reference to the specific user's favorites in Firebase
    private val favRef = FirebaseDatabase.getInstance().getReference("Users/User_01/Favorites")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the top status bar height as margin to your buttons
            val backParams = binding.btnBack.layoutParams as ConstraintLayout.LayoutParams
            backParams.topMargin = insets.top + 20 // Status bar height + your 20dp margin
            binding.btnBack.layoutParams = backParams

            val favParams = binding.btnFavDetail.layoutParams as ConstraintLayout.LayoutParams
            favParams.topMargin = insets.top + 20
            binding.btnFavDetail.layoutParams = favParams

            windowInsets
        }

        // 1. Receive data from Intent
        val name = intent.getStringExtra("name") ?: ""
        val price = intent.getStringExtra("price") ?: ""
        val image = intent.getStringExtra("image") ?: ""
        val desc = intent.getStringExtra("desc") ?: ""
        val category = intent.getStringExtra("category") ?: ""

        // 2. CHECK FIREBASE status immediately to set the Heart Icon
        checkFavoriteStatus(name)

        // 3. Bind UI data
        binding.detailName1.text = name
        binding.detailPrice1.text = "Rs. $price"
        binding.detailDescription1.text = desc
        Glide.with(this).load(image).into(binding.detailImage1)

        // 4. Quantity Logic
        binding.btnPlus.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }

        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }

        // 5. Add to Cart Logic
        binding.btnAddToCart.setOnClickListener {
            val currentQuantity = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
            val item = CafeModel(
                name = name,
                price = price,
                image = image,
                description = desc,
                category = category,
                quantity = currentQuantity,
                id = System.currentTimeMillis().toString()
            )
            fbHelper.saveToCart(item)
            StylishToast.show(this, "Added to Cart", true)
            finish()
        }

        // 6. Upgraded Favorite Toggle Logic
        binding.btnFavDetail.setOnClickListener {
            toggleFavorite(name, price, image, desc, category)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun checkFavoriteStatus(foodName: String) {
        val safeName = foodName.replace(".", "_")
        // Query Firebase to see if this item name exists in favorites
        favRef.child(safeName).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                isFavorite = true
                binding.btnFavDetail.setImageResource(R.drawable.filledfav)
            } else {
                isFavorite = false
                binding.btnFavDetail.setImageResource(R.drawable.favorite)
            }
        }.addOnFailureListener {
            isFavorite = false // Default to outline if network fails
        }
    }

    private fun toggleFavorite(name: String, price: String, image: String, desc: String, category: String) {
        // 1. ADD THIS LINE: This cleans the name so Firebase doesn't crash
        val safeName = name.replace(".", "_").replace("#", "_").replace("$", "_")

        // Heart Animation
        binding.btnFavDetail.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction {
            binding.btnFavDetail.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
        }

        if (isFavorite) {
            // 2. CHANGE THIS: Use safeName instead of name
            fbHelper.removeFromFavorite(safeName)

            binding.btnFavDetail.setImageResource(R.drawable.favorite)
            isFavorite = false
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
        } else {
            // 3. CHANGE THIS: Use safeName for the model name too
            val item = CafeModel(name = safeName, price = price, image = image, description = desc, category = category)

            fbHelper.saveToFavorite(item)
            binding.btnFavDetail.setImageResource(R.drawable.filledfav)
            isFavorite = true
            StylishToast.show(this, "Added to Favorites", true)
        }
    }
}
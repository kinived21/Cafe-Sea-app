package com.example.cafesea

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.cafesea.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private val auth = FirebaseAuth.getInstance()
    private val cartRef = FirebaseDatabase.getInstance().getReference("Users/User_01/Cart")
    private val orderRef = FirebaseDatabase.getInstance().getReference("Orders")

    private val UPI_PAYMENT_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Get the Total passed from CartFragment
        val totalAmount = intent.getStringExtra("TOTAL_PRICE") ?: "Rs. 0"
        binding.tvTotalAmount.text = totalAmount

        binding.btnBackPayment.setOnClickListener { finish() }

        // 2. Confirm Order Button
        binding.btnConfirmOrder.setOnClickListener {
            if (binding.rbCOD.isChecked) {
                // 1. Cash on Delivery logic (Your existing logic)
                binding.btnConfirmOrder.isEnabled = false
                checkCartAndProcess(totalAmount)
            } else {
                // 2. Online Payment logic (Automatic UPI intent)
                val numericAmount = totalAmount.replace("Total:", "").replace("Rs.", "").replace(" ", "").trim()
                startUpiPayment(numericAmount)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            // In a dummy project, we assume coming back means "Paid"
            // Or check if data contains "success"
            val response = data?.getStringExtra("response") ?: ""

            if (response.lowercase().contains("success") || response.isEmpty()) {
                // AUTOMATICALLY place the order without clicking Confirm again
                val totalAmount = binding.tvTotalAmount.text.toString()
                checkCartAndProcess(totalAmount)
            } else {
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startUpiPayment(amount: String) {
        // 1. CLEAN THE AMOUNT: Remove "Rs.", "Total:", and spaces
        // This turns "Total: Rs. 500.0" into "500.0"
        val cleanAmount = amount.replace(Regex("[^0-9.]"), "")

        val upiUri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", "paytmqr2810050501011l2fom20m2o7@paytm") // Use a real or valid-looking ID
            .appendQueryParameter("pn", "CafeSea")
            .appendQueryParameter("tn", "CafeSea Food Order")
            .appendQueryParameter("am", cleanAmount) // Using the sanitized amount
            .appendQueryParameter("cu", "INR")
            .build()

        val upiIntent = Intent(Intent.ACTION_VIEW)
        upiIntent.data = upiUri

        // 2. THE CHOOSER: This triggers the list of apps
        val chooser = Intent.createChooser(upiIntent, "Complete payment with:")

        try {
            startActivityForResult(chooser, UPI_PAYMENT_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "No UPI app found on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCartAndProcess(amount: String) {
        // We check the database directly to see if the cart actually has items
        cartRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && snapshot.hasChildren()) {
                val itemsList = mutableListOf<Map<String, Any>>()
                for (item in snapshot.children) {
                    itemsList.add(item.value as Map<String, Any>)
                }
                processOrder(amount, itemsList)
            } else {
                binding.btnConfirmOrder.isEnabled = true
                showEmptyCartDialog()
            }
        }.addOnFailureListener {
            binding.btnConfirmOrder.isEnabled = true
            StylishToast.show(this, "Error Checking Cart", true)
        }
    }

    private fun processOrder(amount: String, items: List<Map<String, Any>>) {
        // 1. Get the actual logged-in User ID dynamically
        val userId = auth.currentUser?.uid ?: "Guest_User"
        val profileRef = FirebaseDatabase.getInstance().getReference("Users/$userId/Profile")

        // 2. Clean the price string (Remove "Rs." and spaces)
        // If amount is "Rs. 450", numericAmount becomes "450"
        val numericAmount = amount.replace("Rs.", "").replace(" ", "").trim()

        profileRef.get().addOnSuccessListener { snapshot ->
            val userName = snapshot.child("name").value?.toString() ?: "Customer"
            val userAddress = snapshot.child("address").value?.toString() ?: "No Address Provided"

            val orderId = "ORD${System.currentTimeMillis()}"
            val selectedMethod = if (binding.rbCOD.isChecked) "Cash on Delivery" else "Online"

            // 3. Create the Order Map with CORRECT KEYS
            val orderData = hashMapOf(
                "orderId" to orderId,
                "totalPrice" to numericAmount,  // FIXED: Changed from totalAmount to totalPrice
                "paymentMethod" to selectedMethod,
                "status" to "Pending",
                "userId" to userId,
                "userName" to userName,
                "address" to userAddress,
                "items" to items,
                "timestamp" to ServerValue.TIMESTAMP // Professional way to handle time
            )

            // 4. Save to Database
            orderRef.child(orderId).setValue(orderData)
                .addOnSuccessListener {
                    clearCartAndFinish()
                }
                .addOnFailureListener { e ->
                    binding.btnConfirmOrder.isEnabled = true
                    StylishToast.show(this, "Failed to place order: ${e.message}", false)
                }
        }.addOnFailureListener {
            binding.btnConfirmOrder.isEnabled = true
            StylishToast.show(this, "Failed to fetch profile", false)
        }
    }

    private fun clearCartAndFinish() {
        // Remove all items from the cart in Firebase after order is placed
        cartRef.removeValue().addOnSuccessListener {
            StylishToast.show(this, "Order Placed Successfully!!", true)

            // Redirect to Home and clear all previous activities
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showEmptyCartDialog() {
        AlertDialog.Builder(this)
            .setTitle("Empty Cart")
            .setMessage("Your cart is empty! Please add items before checking out.")
            .setCancelable(false)
            .setPositiveButton("Go to Menu") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .show()
    }
}
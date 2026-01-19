package com.example.cafesea

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cafesea.databinding.ActivityMyOrdersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyOrdersBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("Orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Setup UI
        binding.rvMyOrders.layoutManager = LinearLayoutManager(this)

        fetchUserOrders()
    }

    private fun fetchUserOrders() {
        val currentUserId = auth.currentUser?.uid ?: "User_01"

        // Querying Firebase for orders where userId matches
        database.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ordersList = mutableListOf<Map<String, Any>>()

                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val order = data.value as Map<String, Any>
                            ordersList.add(order)
                        }

                        // Show list and hide "No Orders" text if you have one
                        binding.rvMyOrders.adapter = OrderAdapter(ordersList.reversed())
                    } else {
                        StylishToast.show(this@MyOrdersActivity, "No Orders Found", true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    StylishToast.show(this@MyOrdersActivity, "Database Error: ${error.message}", true)
                }
            })
    }
}
package com.example.cafesea

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cafesea.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: List<Map<String, Any>>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Map<String, Any>) {
            val context = binding.root.context

            // 1. Set Order ID (Safely handle nulls)
            val fullOrderId = order["orderId"]?.toString() ?: "000000"
            binding.tvOrderId.text = "Order #${fullOrderId.takeLast(6)}"

            // 2. FIXED PRICE LOGIC: Checking both possible keys
            // In your PaymentActivity you saved it as 'totalAmount'
            val priceValue = order["totalPrice"] ?: order["totalAmount"] ?: "0.00"

            val formattedPrice = when (priceValue) {
                is Number -> String.format("%.2f", priceValue.toDouble())
                is String -> {
                    // Removes "Rs." and any spaces if they were accidentally saved
                    priceValue.replace("Rs.", "").trim()
                }
                else -> "0.00"
            }
            binding.tvOrderPrice.text = "Rs. $formattedPrice"

            // 3. Set Address (Using Theme Attribute for variant text)
            binding.tvOrderAddress.text = order["address"]?.toString() ?: "No Address Provided"

            // 4. Set Status & Modern Theme Colors
            val status = order["status"]?.toString() ?: "Pending"
            binding.tvOrderStatus.text = status

            // Using ContextCompat and dynamic status colors
            val statusColor = when (status.lowercase(Locale.ROOT)) {
                "delivered" -> android.R.color.holo_green_dark
                "cancelled", "failed" -> R.color.red_dark
                "placed", "processing" -> android.R.color.holo_blue_dark
                else -> R.color.red_dark // Default for Pending
            }
            binding.tvOrderStatus.setTextColor(ContextCompat.getColor(context, statusColor))

            // 5. Date Logic (Handling multiple types safely)
            val timestamp = order["timestamp"]
            if (timestamp != null) {
                val timeInMillis = when (timestamp) {
                    is Long -> timestamp
                    is Double -> timestamp.toLong()
                    is String -> timestamp.toLongOrNull() ?: 0L
                    else -> 0L
                }

                if (timeInMillis != 0L) {
                    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    binding.tvOrderDate.text = sdf.format(Date(timeInMillis))
                }
            } else {
                binding.tvOrderDate.text = "Date unavailable"
            }
        }
    }
}
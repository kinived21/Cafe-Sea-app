package com.example.cafesea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafesea.databinding.ItemFoodBinding

class CafeAdapter(
    var foodList: List<CafeModel>,
    private val showQuantityControls: Boolean, // NEW FLAG
    private val onItemClick: (CafeModel) -> Unit,
    private val onIncrease: (CafeModel) -> Unit = {}, // Optional default
    private val onDecrease: (CafeModel) -> Unit = {}  // Optional default
) : RecyclerView.Adapter<CafeAdapter.FoodViewHolder>() {

    class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.binding.foodName1.text = food.name
        holder.binding.foodPrice1.text = "Rs. ${food.price}"

        // --- THE MAGIC LOGIC ---
        if (showQuantityControls) {
            holder.binding.quantityLayout.visibility = View.VISIBLE // You need to ID your layout in XML
            holder.binding.tvItemQuantity.text = food.quantity.toString()
        } else {
            holder.binding.quantityLayout.visibility = View.GONE
        }

        Glide.with(holder.itemView.context).load(food.image).into(holder.binding.foodImage1)

        holder.itemView.setOnClickListener { onItemClick(food) }
        holder.binding.btnPlus.setOnClickListener { onIncrease(food) }
        holder.binding.btnMinus.setOnClickListener { onDecrease(food) }
    }

    override fun getItemCount() = foodList.size

    fun updateList(newList: List<CafeModel>) {
        this.foodList = newList
        notifyDataSetChanged()
    }
}
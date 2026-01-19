package com.example.cafesea

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafesea.databinding.FragmentCartBinding
import com.google.firebase.database.*

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CafeAdapter
    private val cartList = mutableListOf<CafeModel>()

    // Use the Helper we upgraded
    private val fbHelper = FirebaseHelper()
    private var cartListener: ValueEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeCartData()

        binding.btnPlaceOrder.setOnClickListener {
            // USE the local cartList that observeCartData() is filling
            if (cartList.isEmpty()) {
                showEmptyCartDialog()
            } else {
                // Proceed to Payment only
                val intent = Intent(requireContext(), PaymentActivity::class.java).apply {
                    // Pass the text currently shown in your Total TextView
                    putExtra("TOTAL_PRICE", binding.tvTotalAmount.text.toString())
                }
                startActivity(intent)
            }
        }
    }

    private fun showEmptyCartDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Oops! Empty Cart")
            .setMessage("You haven't added anything to your cart yet.")
            .setPositiveButton("Show Menu") { _, _ ->
                // This moves the user back to the main activity
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            .setNegativeButton("Not now", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = CafeAdapter(
            foodList = cartList,
            showQuantityControls = true,
            onItemClick = { food -> /* detail navigation */ },
            onIncrease = { food ->
                // Calls the helper function you just added
                fbHelper.updateCartQuantity(food.name, food.quantity + 1)
            },
            onDecrease = { food ->
                if (food.quantity > 1) {
                    fbHelper.updateCartQuantity(food.name, food.quantity - 1)
                } else {
                    // If quantity is 1 and they click minus, remove it entirely
                    fbHelper.removeFromCart(food.name)
                }
            }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter
    }

    private fun observeCartData() {
        val cartRef = fbHelper.getCartReference()

        cartListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentBinding = _binding ?: return

                cartList.clear()
                var totalPrice = 0.0

                for (data in snapshot.children) {
                    val item = data.getValue(CafeModel::class.java)
                    item?.let {
                        cartList.add(it)

                        val itemPrice = it.price.toDoubleOrNull() ?: 0.0
                        val itemQuantity = it.quantity // Accessing the new field in CafeModel
                        totalPrice += (itemPrice * itemQuantity)
                    }
                }

                adapter.updateList(cartList)
                currentBinding.tvTotalAmount.text = "Total: Rs. $totalPrice"
                currentBinding.tvNoDataCart.visibility = if (cartList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        cartRef.addValueEventListener(cartListener!!)
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToDelete = cartList[position]

                // UPGRADE 2: Sanitize Name to prevent "." crash
                val safeName = itemToDelete.name.replace(".", "_")

                fbHelper.removeFromCart(safeName)
                StylishToast.show(requireActivity(), "Item removed from cart", true)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvCart)
    }


    override fun onDestroyView() {
        // Clean up listener using the helper reference
        fbHelper.getCartReference().removeEventListener(cartListener!!)
        super.onDestroyView()
        _binding = null
    }
}
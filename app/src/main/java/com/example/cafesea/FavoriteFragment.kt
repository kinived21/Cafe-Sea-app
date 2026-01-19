package com.example.cafesea

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cafesea.databinding.FragmentFavoriteBinding
import com.google.firebase.database.*

class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val fbHelper = FirebaseHelper()
    private var favListener: ValueEventListener? = null
    private val favList = mutableListOf<CafeModel>()
    private lateinit var adapter: CafeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        // UPGRADE: Added the Increase and Decrease click listeners to fix the RED error
        adapter = CafeAdapter(
            foodList = favList,
            showQuantityControls = false,
            onItemClick = { food ->
                if (isAdded) {
                    val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                        putExtra("name", food.name)
                        putExtra("price", food.price)
                        putExtra("image", food.image)
                        putExtra("desc", food.description)
                        putExtra("category", food.category)
                    }
                    startActivity(intent)
                }
            }
        )

        binding.rvFavorite.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvFavorite.adapter = adapter
    }

    private fun observeFavorites() {
        val favDatabase = fbHelper.getFavoriteReference()

        favListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentBinding = _binding ?: return

                favList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(CafeModel::class.java)
                    item?.let { favList.add(it) }
                }

                adapter.updateList(favList)
                currentBinding.tvNoDataFav.visibility = if (favList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }
        favDatabase.addValueEventListener(favListener!!)
    }

    override fun onDestroyView() {
        favListener?.let {
            fbHelper.getFavoriteReference().removeEventListener(it)
        }
        super.onDestroyView()
        _binding = null
    }
}
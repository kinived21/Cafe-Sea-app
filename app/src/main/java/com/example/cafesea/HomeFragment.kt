package com.example.cafesea

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafesea.databinding.FragmentHomeBinding
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var fullFoodList = listOf<CafeModel>()
    private var adapter: CafeAdapter? = null

    // Reference to the User Profile in Firebase
    private val profileDatabase = FirebaseDatabase.getInstance().getReference("Users/User_01/Profile")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // START SHIMMER IMMEDIATELY
        binding.shimmerHome.startShimmer()
        binding.shimmerHome.visibility = View.VISIBLE
        binding.swipeRefresh.visibility = View.GONE

        initRecyclerView()
        setupSearch()
        loadUserProfile()
        setupCategoryButtons()

        // Match swipe refresh color to your theme
        binding.swipeRefresh.setColorSchemeResources(R.color.red_dark)
        binding.swipeRefresh.setOnRefreshListener { fetchFoodData() }

        binding.profileCard.setOnClickListener { showCenteredAvatarDialog() }
        binding.filterCard.setOnClickListener { showFilterMenu(it) }

        fetchFoodData()
    }

    /**
     * Loads the user's name and selected avatar from Firebase
     */
    private fun loadUserProfile() {
        profileDatabase.get().addOnSuccessListener { snapshot ->
            // Safety check: Ensure binding exists and fragment is attached
            val currentBinding = _binding ?: return@addOnSuccessListener
            if (!isAdded) return@addOnSuccessListener

            //Load the Profile Image
            val imageName = snapshot.child("profileImage").value?.toString() ?: "person"

            // Get the resource ID dynamically from drawable folder
            val resourceId = try {
                requireContext().resources.getIdentifier(imageName, "drawable", requireContext().packageName)
            } catch (e: Exception) {
                0
            }

            if (resourceId != 0) {
                currentBinding.ivProfileImage.setImageResource(resourceId)
            } else {
                // Fallback image if database name doesn't match a file
                currentBinding.ivProfileImage.setImageResource(R.drawable.person)
            }

        }.addOnFailureListener {
            // Fallback for network errors
            _binding?.ivProfileImage?.setImageResource(R.drawable.person)
        }
    }

    private fun initRecyclerView() {
        // PASS ALL 4 PARAMETERS TO FIX THE RED ERROR
        adapter = CafeAdapter(
            foodList = emptyList(),
            showQuantityControls = false,// This represents the starting data
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

        binding.rvPopular.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = this@HomeFragment.adapter
        }
    }

    private fun setupCategoryButtons() {
        val buttons = listOf(binding.btnAll, binding.btnBurger, binding.btnPizza, binding.btnDrink)

        // Default: Set 'All' to red when the fragment first loads
        updateButtonUI(binding.btnAll, buttons)

        buttons.forEach { selectedButton ->
            selectedButton.setOnClickListener {
                // When any button is clicked, update the UI colors
                updateButtonUI(selectedButton, buttons)

                // Then filter the list
                filterByCategory(selectedButton.text.toString().lowercase())
            }
        }
    }

    private fun updateButtonUI(selected: View, allButtons: List<android.widget.Button>) {
        allButtons.forEach { btn ->
            // Reset all buttons to Gray and smaller size
            btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            btn.scaleX = 0.9f
            btn.scaleY = 0.9f

            // Optional: If you want to change the background tint instead of just text
            btn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.transparent)
        }

        // Set the CLICKED button to Red and larger size
        val activeBtn = selected as android.widget.Button
        activeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_dark))
        activeBtn.scaleX = 1.1f
        activeBtn.scaleY = 1.1f

        // Optional: If you want a background highlight for the red button
        activeBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_light)
    }

    private fun filterByCategory(category: String) {
        val filtered = if (category.contains("all")) {
            fullFoodList
        } else {
            fullFoodList.filter { it.category.lowercase().contains(category.removeSuffix("s")) }
        }

        adapter?.updateList(filtered)
        binding.tvNoData.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun fetchFoodData() {
        // If not refreshing via swipe, show the shimmer
        if (!binding.swipeRefresh.isRefreshing) {
            binding.shimmerHome.startShimmer()
            binding.shimmerHome.visibility = View.VISIBLE
            binding.rvPopular.visibility = View.GONE
        }

        val api = Retrofit.Builder()
            .baseUrl("https://6954ceb21cd5294d2c7d8a11.mockapi.io/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService::class.java)

        api.getFoodItems().enqueue(object : retrofit2.Callback<List<CafeModel>> {
            override fun onResponse(call: Call<List<CafeModel>>, response: Response<List<CafeModel>>) {
                // Safety: ensure fragment is still attached
                if (_binding == null || !isAdded) return

                binding.swipeRefresh.isRefreshing = false

                // STOP SHIMMER AND SHOW RECYCLER
                binding.shimmerHome.stopShimmer()
                binding.shimmerHome.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
                binding.rvPopular.visibility = View.VISIBLE

                if (response.isSuccessful) {
                    fullFoodList = response.body() ?: emptyList()
                    adapter?.updateList(fullFoodList)
                }
            }

            override fun onFailure(call: Call<List<CafeModel>>, t: Throwable) {
                if (_binding == null) return
                binding.swipeRefresh.isRefreshing = false
                binding.shimmerHome.stopShimmer()
                binding.shimmerHome.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE

                Toast.makeText(requireContext(), "Check your internet connection", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filteredList = fullFoodList.filter { it.name.lowercase().contains(query) }
                // Make the Recycler disappear if no results, otherwise show it
                binding.rvPopular.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
                binding.tvNoData.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE

                // If the list is empty, show the "No Data" text
                if (filteredList.isEmpty()) {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.rvPopular.visibility = View.GONE
                } else {
                    binding.tvNoData.visibility = View.GONE
                    binding.rvPopular.visibility = View.VISIBLE
                }

                adapter?.updateList(filteredList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showCenteredAvatarDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_avatar_picker, null)
        val alertDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val rvAvatars = dialogView.findViewById<RecyclerView>(R.id.rvAvatarOptions)
        val avatarList = listOf(R.drawable.pro1, R.drawable.pro2, R.drawable.pro3, R.drawable.pro4, R.drawable.pro5, R.drawable.pro6)

        rvAvatars.layoutManager = GridLayoutManager(requireContext(), 3)
        rvAvatars.adapter = AvatarAdapter(avatarList) { selectedResId ->
            val drawableName = resources.getResourceEntryName(selectedResId)
            saveAvatarToFirebase(drawableName)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun saveAvatarToFirebase(imageName: String) {
        profileDatabase.child("profileImage").setValue(imageName).addOnSuccessListener {
            if (isAdded) {
                StylishToast.show(requireActivity(), "Profile Image Updated",true)
                loadUserProfile()
            }
        }
    }

    private fun showFilterMenu(view: View) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
        popup.menu.add("Price: Low to High")
        popup.menu.add("Price: High to Low")
        popup.menu.add("Name: A to Z")
        popup.menu.add("Reset")

        popup.setOnMenuItemClickListener { item ->
            val currentList = adapter?.foodList ?: fullFoodList
            val sortedList = when (item.title) {
                "Price: Low to High" -> currentList.sortedBy { it.price.toDoubleOrNull() ?: 0.0 }
                "Price: High to Low" -> currentList.sortedByDescending { it.price.toDoubleOrNull() ?: 0.0 }
                "Name: A to Z" -> currentList.sortedBy { it.name.lowercase() }
                else -> fullFoodList
            }
            adapter?.updateList(sortedList)
            true
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
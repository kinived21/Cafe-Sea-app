package com.example.cafesea

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cafesea.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileDatabase = FirebaseDatabase.getInstance().getReference("Users/User_01/Profile")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEditProfileLink.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.cardAddress.setOnClickListener {
            startActivity(Intent(requireContext(), AddressActivity::class.java))
        }
        loadProfileData()
        setupMenuRecyclerView()
    }

    private fun loadProfileData() {

        profileDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentBinding = _binding ?: return

                val address = snapshot.child("address").value?.toString() ?: "Add Address Here"
                binding.tvAddressText.text = address
                // Set User Name
                val name = snapshot.child("name").value?.toString() ?: "User"
                currentBinding.userName.text = name

                // Set Profile Image
                val imageName = snapshot.child("profileImage").value?.toString() ?: "person"
                val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
                currentBinding.profileImage.setImageResource(if (resId != 0) resId else R.drawable.person)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupMenuRecyclerView() {
        // Define specific menu items
        val menuItems = listOf(
            MenuModel("My Orders", R.drawable.baseline_shopping_bag_24),
            MenuModel("Your feedback", R.drawable.baseline_thumb_up_24),
            MenuModel("About", R.drawable.baseline_info_24),
            MenuModel("Customer support", R.drawable.outline_support_agent_24),
            MenuModel("Settings", R.drawable.baseline_settings_24),
            MenuModel("Logout", R.drawable.baseline_logout_24)
        )

        // Initialize Adapter with click logic
        val menuAdapter = ProfileMenuAdapter(menuItems) { title ->
            when (title) {
                "My Orders" -> {
                    startActivity(Intent(requireContext(), MyOrdersActivity::class.java))
                }
                "Your feedback" -> {
                    startActivity(Intent(requireContext(), FeedbackActivity::class.java))
                }
                "About" -> {
                    startActivity(Intent(requireContext(), AboutActivity::class.java))
                }
                "Customer support" -> {
                    startActivity(Intent(requireContext(), CustomerSupportActivity::class.java))
                }
                "Settings" -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }
                "Logout" -> showLogoutDialog()
                "Customer support" -> contactSupport()
                else -> StylishToast.show(requireActivity(), "$title clicked", true)
            }
        }

        binding.rvProfileMenu.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
            isNestedScrollingEnabled = false // Important for ScrollView performance
        }
    }

    private fun showLogoutDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Logout") { _, _ -> performLogoutLogic() }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()

// Set the positive button color to Red
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red_dark)
        )
    }

    private fun performLogoutLogic() {
        // 1. Sign out from Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

        // 2. Clear any local SharedPreferences (Optional)
        val sharedPref = requireActivity().getSharedPreferences("CafeSeaSettings", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // 3. Move to Login Activity and clear the task
        val intent = Intent(requireContext(), LoginActivity::class.java)

        // These flags ensure the user cannot go back to the Profile screen after logging out
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish() // Close current activity

        StylishToast.show(requireActivity(), "Logged Out Successfully!!", true)
    }

    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@cafesea.com")
            putExtra(Intent.EXTRA_SUBJECT, "Customer Support")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
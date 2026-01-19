package com.example.cafesea

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cafesea.databinding.ActivityAddressBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding
    private val database = FirebaseDatabase.getInstance().getReference("Users/User_01/Profile")

    // Register the permission requester
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndOpenMap()
        } else {
            StylishToast.show(this, "Location permission denied, Search manually", true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackAddress.setOnClickListener { finish() }

        // 1. Manual Save Button
        binding.btnSaveAddress.setOnClickListener {
            val address = binding.etManualAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                saveToFirebase(address)
            } else {
                StylishToast.show(this,  "Please enter an address", true)
            }
        }

        // 2. Upgraded Google Maps Button
        binding.btnPickOnMap.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndOpenMap()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchLocationAndOpenMap() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude

                    // Auto-fill the address field using Geocoder (Reverse Geocoding)
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addressLine = addresses[0].getAddressLine(0)
                            binding.etManualAddress.setText(addressLine)
                            StylishToast.show(this, "Address Saved Successfully!", true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Open Google Maps at this location
                    val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(My+Location)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    if (mapIntent.resolveActivity(packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        StylishToast.show(this, "No map app found", true)
                    }
                } else {
                    StylishToast.show(this, "Location not available,Turn on location", true)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun saveToFirebase(address: String) {
        database.child("address").setValue(address).addOnSuccessListener {
            StylishToast.show(this, "Address Saved Successfully!", true)
            finish()
        }.addOnFailureListener {
            StylishToast.show(this, "Failed to save address", true)
        }
    }
}
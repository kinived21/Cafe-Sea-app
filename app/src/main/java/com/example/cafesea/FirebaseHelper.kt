package com.example.cafesea

import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {
    private val database = FirebaseDatabase.getInstance().getReference("Users")
    private val userId = "User_01"

    // --- CART METHODS ---

    fun updateCartQuantity(itemName: String, newQuantity: Int) {
        // 1. Sanitize the name just like we did for favorites/cart
        val safeKey = itemName.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")

        // 2. Update ONLY the quantity field in the database
        // This will trigger the listener in CartFragment to update the total price
        database.child(userId).child("Cart").child(safeKey).child("quantity").setValue(newQuantity)
    }

    fun saveToCart(item: CafeModel) {
        // Sanitize the key before saving
        val safeKey = item.name.replace(".", "_")

        database.child(userId).child("Cart").child(safeKey).setValue(item)
    }

    // ADD THIS: Remove a single item from the cart
    fun removeFromCart(itemName: String) {
        // 1. Sanitize the name so it matches how it was saved
        val safeKey = itemName.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")

        // 2. Use the safeKey to remove the value
        database.child(userId).child("Cart").child(safeKey).removeValue()
    }

    // ADD THIS: Clear the entire cart after a successful order
    fun clearCart() {
        database.child(userId).child("Cart").removeValue()
    }

    // --- FAVORITE METHODS ---

    // Inside FirebaseHelper.kt

    fun saveToFavorite(item: CafeModel) {
        // This replaces forbidden characters with underscores
        val safeKey = item.name.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")

        database.child(userId).child("Favorites").child(safeKey).setValue(item)
    }

    fun removeFromFavorite(itemName: String) {
        val safeKey = itemName.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")

        database.child(userId).child("Favorites").child(safeKey).removeValue()
    }

    // ADD THIS: A helper to get the reference directly for listeners
    fun getCartReference() = database.child(userId).child("Cart")

    fun getFavoriteReference() = database.child(userId).child("Favorites")
}
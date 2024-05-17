package com.dsa.pocketmart

import com.dsa.pocketmart.models.CartItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CartRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun addItemToKart(userId: String, cartItem: CartItem) {
        val userCartRef = database.child("cart").child(userId)
        val newItemRef = userCartRef.push()
        newItemRef.setValue(cartItem)
    }
}
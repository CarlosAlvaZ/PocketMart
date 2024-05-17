package com.dsa.pocketmart

import android.util.Log
import com.dsa.pocketmart.models.CartItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CartRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun addItemToKart(userId: String, cartItem: CartItem) {
        val userCartRef = database.child("cart").child(userId).child(cartItem.id!!)
        userCartRef.setValue(cartItem)
    }

    fun deleteItemFromKart(userId: String, cartItemId: String, sucessCb: () -> Unit) {
        val userCartRef = database.child("cart").child(userId).child(cartItemId)
        userCartRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                sucessCb()
            } else {
                Log.e(
                    "Firebase Realtime database Cart",
                    "${task.exception?.message}, ${task.exception?.cause}"
                )
            }
        }
    }
}
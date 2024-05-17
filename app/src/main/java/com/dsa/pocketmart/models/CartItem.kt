package com.dsa.pocketmart.models

data class CartItem(
    val id: String? = "",
    val productId: String? = "",
    val productName: String? = "",
    val quantity: Int? = 0,
    val subTotal: Double? = 0.0
)

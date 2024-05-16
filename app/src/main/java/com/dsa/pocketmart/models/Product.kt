package com.dsa.pocketmart.models

data class Product(val id: String, val name: String, val description: String, val price: Double, val category: Category, val images: List<String>)

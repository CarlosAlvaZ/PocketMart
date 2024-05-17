package com.dsa.pocketmart.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.dsa.pocketmart.CartRepository
import com.dsa.pocketmart.R
import com.dsa.pocketmart.adapters.OrderItemAdapter
import com.dsa.pocketmart.adapters.ProductsAdapter
import com.dsa.pocketmart.databinding.FragmentOrderBinding
import com.dsa.pocketmart.models.CartItem
import com.dsa.pocketmart.models.Category
import com.dsa.pocketmart.models.Product
import com.dsa.pocketmart.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private val cartItems = arrayListOf<CartItem>()

    private val cartItemsAdapter =
        OrderItemAdapter(cartItems, { deleteCartItem(it) }, { goToItem(it) })

    private val productsArrayList = arrayListOf<Product>()

    private val productsAdapter = ProductsAdapter(productsArrayList, true) { goToItem(it) }

    private lateinit var db: FirebaseFirestore

    private lateinit var rtdb: FirebaseDatabase

    private lateinit var auth: FirebaseAuth

    private val cartRepository = CartRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        rtdb = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCartItemsRecycler()
        initProductRecyclerView()
        fetchCartItems()
        fetchProducts()
        getDirection()
    }

    private fun initCartItemsRecycler() {
        binding.cartEmpty.visibility = View.GONE
        val layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyler.layoutManager = layoutManager
        binding.cartRecyler.adapter = cartItemsAdapter
        binding.cartRecyler.addItemDecoration(
            DividerItemDecoration(
                requireContext(), LinearLayoutManager.VERTICAL
            )
        )
    }

    private fun isCartEmpty() {
        if (cartItems.isEmpty()) {
            binding.cartEmpty.visibility = View.VISIBLE
        } else {
            binding.cartEmpty.visibility = View.GONE
        }
    }

    private fun goToItem(productId: String) {
        findNavController().navigate(
            OrderFragmentDirections.actionOrderFragmentToProductFragment(
                productId
            )
        )
    }

    private fun calcTotal() {
        if (cartItems.isNotEmpty()) {
            val total = cartItems.sumOf { it.subTotal!! }
            binding.totalPrice.text = "$ $total"
        } else {
            binding.totalPrice.text = "$ 0.00"
        }

    }

    private fun fetchCartItems() {
        if (auth.currentUser != null) {
            binding.cartRecyler.visibility = View.GONE
            binding.progressBarCart.visibility = View.VISIBLE
            rtdb.reference.child("cart").child(auth.currentUser!!.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.cartRecyler.visibility = View.GONE
                        binding.progressBarCart.visibility = View.VISIBLE
                        cartItems.clear()
                        for (productSnapshot in snapshot.children) {
                            val item = productSnapshot.getValue(CartItem::class.java)
                            item?.let { cartItems.add(it) }
                        }
                        binding.cartRecyler.visibility = View.VISIBLE
                        binding.progressBarCart.visibility = View.GONE
                        isCartEmpty()
                        calcTotal()
                        cartItemsAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Ocurrió un error al obtener los productos",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "Cart Items Database Error",
                            "${error.code}. ${error.message}, ${error.details}"
                        )
                    }

                })
        } else {
            Log.e("Authentication error", "No user authenticated for cart items")
        }
    }


    private fun initProductRecyclerView() {
        val gridLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.productsRecycler.layoutManager = gridLayoutManager
        binding.productsRecycler.adapter = productsAdapter
        binding.productsRecycler.addItemDecoration(ProductsAdapter.HorizontalSpaceItemDecoration(20))
    }

    private fun fetchProducts() {
        binding.productsRecycler.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        productsArrayList.clear()

        db.collection("products").get().addOnSuccessListener { result ->
            processResult(result)
        }.addOnFailureListener {
            onFailureProduct(it)
        }
        productsAdapter.notifyDataSetChanged()
    }

    private fun processResult(result: QuerySnapshot) {
        for (document in result) {
            Log.d("Success on firestore", "${document.data}")
            val id = document.id
            val name = document.getString("name") ?: ""
            val description = document.getString("description") ?: ""
            val price = document.getDouble("price") ?: 0.0
            val categoryReference = document.getDocumentReference("category")
            var categoryName = ""
            var categoryId = ""
            var categoryColor = ""
            categoryReference?.get()?.addOnSuccessListener { category ->
                if (category != null && category.exists()) {
                    categoryId = category.id
                    categoryName = category.getString("name") ?: ""
                    categoryColor = category.getString("color") ?: ""
                }
            }
            val imageReferences = document.get("images") as List<DocumentReference>
            val images = arrayListOf<String>()
            for (imageReference in imageReferences) {
                val firebaseStorage = FirebaseStorage.getInstance()
                firebaseStorage.getReference(imageReference.path).downloadUrl.addOnSuccessListener { uri ->
                    Log.d("Image", uri.toString())
                    images.add(uri.toString())
                    val category = Category(categoryId, categoryName, categoryColor)
                    val product = Product(id, name, description, price, category, images)
                    productsArrayList.add(product)
                    productsAdapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    Log.e("IMAGE FAILURE", "ERROR ${it.message}, ${it.cause}")
                }
            }
        }
        binding.progressBar.visibility = View.GONE
        binding.productsRecycler.visibility = View.VISIBLE
    }

    private fun onFailureProduct(exception: Exception) {
        Toast.makeText(
            requireContext(), "Error Firestore: ${exception.message}", Toast.LENGTH_SHORT
        ).show()
        binding.progressBar.visibility = View.GONE
        binding.productsRecycler.visibility = View.VISIBLE
        Log.e("Error firestore", "${exception.message}, ${exception.cause}")
    }

    private fun deleteCartItem(productId: String) {
        auth.currentUser?.let {
            cartRepository.deleteItemFromKart(it.uid, productId) {
                Toast.makeText(
                    requireContext(), "Producto eliminado exitosamente", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getDirection() {
        auth.currentUser?.let {
            val dbref = db.collection("users").document(it.uid)
            dbref.get().addOnSuccessListener {
                val zipCode = it.getString("zipCode") ?: ""
                val location = it.getString("location") ?: ""
                val city = it.getString("city") ?: ""
                val state = it.getString("state") ?: ""
                binding.cardLocation.text = "$location, $city, $state, $zipCode"
            }
        }
    }
}
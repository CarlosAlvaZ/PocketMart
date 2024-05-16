package com.dsa.pocketmart.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsa.pocketmart.R
import com.dsa.pocketmart.adapters.CategoriesAdapter
import com.dsa.pocketmart.adapters.ProductsAdapter
import com.dsa.pocketmart.databinding.FragmentHomeBinding
import com.dsa.pocketmart.models.Category
import com.dsa.pocketmart.models.Product
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val productsArrayList: ArrayList<Product> = arrayListOf()

    private val productsAdapter = ProductsAdapter(productsArrayList) { onClickProduct(it) }

    private val categoryArrayList: ArrayList<Category> = arrayListOf()

    private val categoryAdapter = CategoriesAdapter(categoryArrayList) { categoryOnClick(it) }

    private lateinit var db: FirebaseFirestore

    private lateinit var storageRef: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        val firebaseStorage = FirebaseStorage.getInstance()
        storageRef = firebaseStorage.reference
    }

    private fun fetchProducts(queryCategory: Category? = null, query: String? = null) {
        binding.productsRecycler.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        productsArrayList.clear()
        if (queryCategory != null) {
            db.collection("products")
                .whereEqualTo("category", db.document("/categories/" + queryCategory.id)).get()
                .addOnSuccessListener { result ->
                    processResult(result)
                }.addOnFailureListener {
                    onFailureProduct(it)
                }
            productsAdapter.notifyDataSetChanged()
            return
        }

        if (query != null) {
            db.collection("products").whereIn(
                "name", arrayListOf(
                    query.lowercase(),
                    query.uppercase(),
                    query.replaceFirstChar { it.uppercase() },
                    query
                )
            ).get().addOnSuccessListener { result ->
                processResult(result)
            }.addOnFailureListener {
                onFailureProduct(it)
            }
            productsAdapter.notifyDataSetChanged()
            return
        }

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

    private fun fetchCategories() {
        categoryArrayList.clear()
        db.collection("categories").get().addOnSuccessListener { result ->
            for (cat in result) {
                val id = cat.id
                val name = cat.getString("name") ?: ""
                val color = cat.getString("color") ?: ""
                val newCat = Category(id, name, color)
                categoryArrayList.add(newCat)
            }
            categoryAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(), "Error Firestore en categorias: ${it.message}", Toast.LENGTH_SHORT
            ).show()
            Log.e("Error firestore en categorias", "${it.message}, ${it.cause}")
        }
    }

    private fun initCategoryRecyclerView() {
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecycler.layoutManager = layoutManager
        binding.categoriesRecycler.adapter = categoryAdapter
        binding.categoriesRecycler.addItemDecoration(
            CategoriesAdapter.HorizontalSpaceItemDecoration(
                32
            )
        )
    }

    private fun initProductRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.productsRecycler.layoutManager = gridLayoutManager
        binding.productsRecycler.adapter = productsAdapter
        binding.productsRecycler.addItemDecoration(
            ProductsAdapter.GridSpacingItemDecoration(
                40, gridLayoutManager.spanCount
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCategoryRecyclerView()
        initProductRecyclerView()
        initSearchView()
        fetchProducts()
        fetchCategories()
    }

    private fun initSearchView() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fetchProducts(query = query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun categoryOnClick(category: Category) {
        fetchProducts(category)
    }

    private fun onClickProduct(productId: String) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToProductFragment(
                productId
            )
        )
    }
}
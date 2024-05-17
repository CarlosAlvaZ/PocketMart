package com.dsa.pocketmart.Fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.dsa.pocketmart.CartRepository
import com.dsa.pocketmart.R
import com.dsa.pocketmart.databinding.FragmentProductBinding
import com.dsa.pocketmart.models.CartItem
import com.dsa.pocketmart.models.Category
import com.dsa.pocketmart.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import java.time.Instant

class ProductFragment : Fragment() {

    val args: ProductFragmentArgs by navArgs()

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore

    private val cartRepository = CartRepository()

    private lateinit var storageRef: StorageReference

    private lateinit var auth: FirebaseAuth

    private lateinit var dialog: Dialog

    private var quantity = 1

    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        val firebaseStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = firebaseStorage.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchProduct(args.productId)
    }

    private fun setDialog() {
        dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.add_item_to_kart_modal)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.modal_background))
        dialog.setCancelable(false)

        val cancelButton = dialog.findViewById<AppCompatImageButton>(R.id.cancel)
        val addButton = dialog.findViewById<AppCompatImageButton>(R.id.more)
        val removeButton = dialog.findViewById<AppCompatImageButton>(R.id.less)
        val addToKart = dialog.findViewById<Button>(R.id.add_to_kart)

        binding.addToKart.setOnClickListener {
            dialog.show()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        addButton.setOnClickListener {
            quantity++
            updateQuantity()
        }

        removeButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantity()
            }
        }

        addToKart.setOnClickListener {
            if (auth.currentUser != null && product != null) {
                val cartItem =
                    CartItem(
                        System.currentTimeMillis().toString(),
                        product!!.id,
                        product!!.name,
                        quantity,
                        quantity * product!!.price
                    )
                cartRepository.addItemToKart(auth.currentUser!!.uid, cartItem)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Item añadido al carrito", Toast.LENGTH_SHORT)
                    .show()
            } else {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Ha ocurrido un problema", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        updateQuantity()
        updateTotal()
    }

    private fun updateQuantity() {
        val quantityElement = dialog.findViewById<TextView>(R.id.quantity)
        quantityElement.text = quantity.toString()
        updateTotal()
    }

    private fun updateTotal() {
        val totalElement = dialog.findViewById<TextView>(R.id.total)
        if (product != null) {
            totalElement.text = "$ ${quantity * product!!.price}"
        }
    }

    private fun fetchProduct(productId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.image.visibility = View.GONE
        db.collection("products").document(productId).get().addOnSuccessListener { result ->
            onSuccessProduct(result)
        }.addOnFailureListener {
            onFailureProduct(it)
        }
    }

    private fun onSuccessProduct(result: DocumentSnapshot) {
        val document = result
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
                product = Product(id, name, description, price, category, images)
                printProduct(product!!)
                setDialog()
            }.addOnFailureListener {
                Log.e("IMAGE FAILURE", "ERROR ${it.message}, ${it.cause}")
            }
        }
        binding.progressBar.visibility = View.GONE
        binding.image.visibility = View.VISIBLE
    }

    private fun onFailureProduct(exception: Exception) {
        Toast.makeText(
            requireContext(), "Error Firestore: ${exception.message}", Toast.LENGTH_SHORT
        ).show()
        binding.progressBar.visibility = View.GONE
        binding.name.text = getString(R.string.error_al_buscar_producto)
        Log.e("Error firestore", "${exception.message}, ${exception.cause}")
    }

    private fun printProduct(product: Product) {
        binding.name.text = product.name
        binding.price.text = "$ ${product.price}"
        binding.description.text = product.description
        if (product.images.size > 0) {
            Glide.with(binding.image.context).load(product.images.get(0)).into(binding.image)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
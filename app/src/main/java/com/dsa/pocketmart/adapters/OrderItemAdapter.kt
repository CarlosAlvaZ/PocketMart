package com.dsa.pocketmart.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsa.pocketmart.R
import com.dsa.pocketmart.databinding.OrderItemBinding
import com.dsa.pocketmart.models.CartItem
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class OrderItemAdapter(
    private val cartItems: List<CartItem>,
    private val onClickListenerDelete: (String) -> Unit,
    private val onClickListenerItem: (String) -> Unit
) : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val binding = OrderItemBinding.bind(view)

        private val db = FirebaseFirestore.getInstance()
        fun render(
            cartItem: CartItem,
            onClickListenerDelete: (String) -> Unit,
            onClickListenerItem: (String) -> Unit
        ) {
            db.collection("products").document(cartItem.productId!!).get()
                .addOnSuccessListener { result ->
                    val document = result
                    Log.d("Success on firestore", "${document.data}")
                    val name = document.getString("name") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    binding.name.text = name
                    binding.price.text = "$ ${price}"
                    binding.quantity.text = cartItem.quantity.toString()
                    val imageReferences = document.get("images") as List<DocumentReference>
                    val images = arrayListOf<String>()
                    for (imageReference in imageReferences) {
                        val firebaseStorage = FirebaseStorage.getInstance()
                        firebaseStorage.getReference(imageReference.path).downloadUrl.addOnSuccessListener { uri ->
                            Log.d("Image", uri.toString())
                            images.add(uri.toString())
                            if (images.size > 0) {
                                Glide.with(binding.image.context).load(images.get(0))
                                    .into(binding.image)
                            }
                        }.addOnFailureListener {
                            Log.e("IMAGE FAILURE", "ERROR ${it.message}, ${it.cause}")
                        }
                    }
                }

            itemView.setOnClickListener { onClickListenerItem(cartItem.productId) }
            binding.delete.setOnClickListener {
                onClickListenerDelete(cartItem.id!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.order_item, parent, false))
    }

    override fun getItemCount(): Int = cartItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartItems[position]
        holder.render(item, onClickListenerDelete, onClickListenerItem)
    }
}
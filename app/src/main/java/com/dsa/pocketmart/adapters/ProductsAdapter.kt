package com.dsa.pocketmart.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsa.pocketmart.R
import com.dsa.pocketmart.databinding.ProductCardBinding
import com.dsa.pocketmart.models.Product

class ProductsAdapter(
    private val productsList: List<Product>, private val onClickListener: (String) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        val binding = ProductCardBinding.bind(view)
        fun render(product: Product, onClickListener: (String) -> Unit) {
            binding.name.text = product.name
            binding.price.text = "$ ${product.price}"
            if (product.images.size > 0) {
                Glide.with(binding.image.context).load(product.images.get(0)).into(binding.image)
            }
            itemView.setOnClickListener {
                onClickListener(product.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.product_card, parent, false))
    }

    override fun getItemCount(): Int = productsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = productsList[position]
        holder.render(item, onClickListener)
    }

    class GridSpacingItemDecoration(private val spacing: Int, private val spanCount: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        }
    }
}
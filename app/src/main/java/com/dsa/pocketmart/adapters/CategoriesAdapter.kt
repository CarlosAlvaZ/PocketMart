package com.dsa.pocketmart.adapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dsa.pocketmart.R
import com.dsa.pocketmart.databinding.CategoryItemBinding
import com.dsa.pocketmart.models.Category

class CategoriesAdapter(
    private val categoryList: List<Category>, private val onClickListener: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CategoriesAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.category_item, parent, false))
    }

    override fun onBindViewHolder(holder: CategoriesAdapter.ViewHolder, position: Int) {
        val item = categoryList[position]
        holder.render(item, onClickListener)
    }

    override fun getItemCount(): Int = categoryList.size

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val bindign = CategoryItemBinding.bind(view)

        public fun render(category: Category, onClickListener: (Category) -> Unit) {
            bindign.categoryName.text = category.name
            val hexColor = Color.parseColor(category.color)
            val colorFilter = PorterDuffColorFilter(hexColor, PorterDuff.Mode.SRC_ATOP)
            bindign.card.background.colorFilter = colorFilter
            itemView.setOnClickListener {
                onClickListener(category)
            }
        }
    }

    class HorizontalSpaceItemDecoration(private val horizontalSpaceWidth: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.right = horizontalSpaceWidth // Set right margin (spacing) for each item
            outRect.left = horizontalSpaceWidth
        }
    }
}
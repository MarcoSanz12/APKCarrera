package com.gf.common.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cotesa.common.extensions.notNull
import com.gf.common.R
import com.gf.common.extensions.loadFromUrl
import com.gf.common.utils.ImageZoomView


class MultimediaGalleryDialogAdapter(
    var items: List<Bitmap>? = null,
    var urls : List<String>? = null,
    var onEndScrolling : (Boolean) -> Unit
) :
    RecyclerView.Adapter<MultimediaGalleryDialogAdapter.ViewHolder>() {

    companion object{
        private const val TAG = "MultimediaGalleryDialog"
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.gallery_item, viewGroup, false)

        return ViewHolder(view)
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bind(i)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.ivItem.fitToScreen()
    }

    override fun getItemCount(): Int = items?.size ?: urls?.size ?: 0

    fun loadItems(items: List<Bitmap>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var ivItem: ImageZoomView = itemView.findViewById(R.id.iv_imagen)
        var mPosition = 0
        //var root : View = itemView.findViewById(R.id.ly_root)

        fun bind(position: Int) {
            mPosition = position

            with(ivItem){
                if (!items.isNullOrEmpty())
                    setImageBitmap(items!![position])
                else if (!urls.isNullOrEmpty())
                    loadFromUrl(urls!![position])

                canScroll = this@MultimediaGalleryDialogAdapter.onEndScrolling
            }
        }
    }

}
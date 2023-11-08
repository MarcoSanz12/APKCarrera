package com.gf.apkcarrera.features.f3_running.adapter

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageButton
import androidx.core.view.updateMargins
import com.cotesa.appcore.platform.BaseAdapter
import com.gf.apkcarrera.R
import com.gf.common.entity.activity.ActivityImage
import com.google.android.material.imageview.ShapeableImageView

class RunningImagesAdapter(
    imageList : List<ActivityImage>,
    val onImageClick : (images:List<Bitmap>, position : Int) -> Unit,
    val onImageRemoved : () -> Unit
) : BaseAdapter<ActivityImage>(imageList,R.layout.item_image_deletable) {

    override fun renderOnViewHolder(resource: ActivityImage, view: View) {

        val margin = view.context.resources.getDimension(com.gf.common.R.dimen.dim_10dp).toInt()
        (view.layoutParams as MarginLayoutParams).updateMargins(right = margin)

        val imageViewHolder = view.findViewById<ShapeableImageView>(R.id.iv_imageView)
        val closeButton = view.findViewById<ImageButton>(R.id.bt_remove)

        imageViewHolder.setImageBitmap(resource.image)

        // Borrar foto
        closeButton.setOnClickListener {
            removeImage(resource)
        }

        // Mostrar foto en grande
        imageViewHolder.setOnClickListener {
            onImageClick(resourceListFiltered.map { it.image }, resourceListFiltered.indexOf(resource))
        }

    }

    fun addImage(bitmap: Bitmap){
        val newList = resourceListFiltered.map { it.copy() } + listOf<ActivityImage>(ActivityImage(resourceListFiltered.size,bitmap))
        actualizarLista(newList)
    }

    private fun removeImage(activityImage: ActivityImage){
        val newList = resourceListFiltered.filter { it != activityImage }
        actualizarLista(newList)
        onImageRemoved()
    }

}
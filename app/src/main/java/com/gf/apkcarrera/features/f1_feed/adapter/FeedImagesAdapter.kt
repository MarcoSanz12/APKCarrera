package com.gf.apkcarrera.features.f1_feed.adapter

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.updateMargins
import com.cotesa.appcore.platform.BaseAdapter
import com.gf.apkcarrera.R
import com.gf.common.entity.activity.ActivityImage
import com.google.android.material.imageview.ShapeableImageView

class FeedImagesAdapter (imageList : List<ActivityImage>,
                         val onImageClick : (images:List<Bitmap>, position : Int) -> Unit,)
    : BaseAdapter<ActivityImage>(imageList,R.layout.item_image) {

    override fun renderOnViewHolder(resource: ActivityImage, view: View) {

        val margin = view.context.resources.getDimension(com.gf.common.R.dimen.dim_10dp).toInt()
        (view.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(right = margin)

        val imageViewHolder = view as ShapeableImageView

        imageViewHolder.setImageBitmap(resource.image)


        // Mostrar foto en grande
        imageViewHolder.setOnClickListener {
            onImageClick(resourceListFiltered.map { it.image }, resourceListFiltered.indexOf(resource))
        }

    }
}
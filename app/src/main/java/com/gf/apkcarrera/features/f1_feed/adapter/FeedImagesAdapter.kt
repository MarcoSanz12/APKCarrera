package com.gf.apkcarrera.features.f1_feed.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateMargins
import com.cotesa.appcore.platform.BaseAdapter
import com.gf.apkcarrera.R
import com.gf.common.entity.feed.FeedImage
import com.gf.common.extensions.loadFromUrl
import com.google.android.material.imageview.ShapeableImageView

class FeedImagesAdapter (imageList : List<FeedImage>,
                         val onImageClick : (images:List<FeedImage>, position : Int) -> Unit,)
    : BaseAdapter<FeedImage>(imageList,R.layout.item_image) {

    override fun renderOnViewHolder(resource: FeedImage, view: View) {

        val margin = view.context.resources.getDimension(com.gf.common.R.dimen.dim_10dp).toInt()
        (view.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(right = margin)

        val imageViewHolder = view as ShapeableImageView

        imageViewHolder.loadFromUrl(resource.url)


        // Mostrar foto en grande
        imageViewHolder.setOnClickListener {
            onImageClick(resourceListFiltered, resourceListFiltered.indexOf(resource))
        }

    }
}
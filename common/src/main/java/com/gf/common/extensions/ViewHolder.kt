package com.gf.common.extensions

import android.util.Log
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.gf.common.entity.Resource

fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition, itemViewType)
    }
    return this
}

fun RecyclerView.ViewHolder.animateItem(idsToAnimate: MutableList<Int>, item : Resource, animationId : Int) {
    if (idsToAnimate.contains(item.getID())){
        Log.d("AnimateItem", "${item.getID()} -> ${item.getName()}")
        itemView.startAnimation(AnimationUtils.loadAnimation(itemView.context,animationId))
        idsToAnimate.remove(item.getID())
    }
}
package com.gf.apkcarrera.features.f1_feed.adapter

import androidx.recyclerview.widget.DiffUtil
import com.gf.common.entity.activity.ActivityModel
import com.google.firebase.firestore.auth.User


object ActivityModelComparator : DiffUtil.ItemCallback<ActivityModel>() {
    override fun areItemsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
        // Id is unique.
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean = (oldItem == newItem)

}
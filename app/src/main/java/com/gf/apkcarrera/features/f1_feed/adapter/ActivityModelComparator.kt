package com.gf.apkcarrera.features.f1_feed.adapter

import androidx.recyclerview.widget.DiffUtil
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.google.firebase.firestore.auth.User


object ActivityModelComparator : DiffUtil.ItemCallback<Pair<ActivityModel, UserModel>>() {
    override fun areItemsTheSame(oldItem: Pair<ActivityModel,UserModel>, newItem: Pair<ActivityModel,UserModel>): Boolean {
        // Id is unique.
        return oldItem.first.uid == newItem.first.uid
    }

    override fun areContentsTheSame(oldItem: Pair<ActivityModel,UserModel>, newItem: Pair<ActivityModel,UserModel>): Boolean = (oldItem.first == newItem.first)

}
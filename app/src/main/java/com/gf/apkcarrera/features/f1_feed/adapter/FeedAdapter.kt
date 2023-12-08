package com.gf.apkcarrera.features.f1_feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gf.apkcarrera.R
import com.gf.common.entity.activity.ActivityModel
import com.google.firebase.firestore.auth.User
import javax.inject.Inject
import kotlin.math.atan

class FeedAdapter () :
    PagingDataAdapter<ActivityModel, ActivityViewHolder>(ActivityModelComparator) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity,parent,false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = getItem(position)
        // Note that item can be null. ViewHolder must support binding a
        // null item as a placeholder.
        holder.bind(item)
    }

}

class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view){
    fun bind(activity:ActivityModel?){
        val tvDescription = this.itemView.findViewById<TextView>(R.id.tv_message)

        tvDescription.text = activity?.title
    }

}

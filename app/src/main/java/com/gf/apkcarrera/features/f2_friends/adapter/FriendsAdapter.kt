package com.gf.apkcarrera.features.f2_friends.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cotesa.appcore.platform.BaseAdapter
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.common.entity.friend.FriendModel
import com.gf.common.extensions.invisible
import com.gf.common.extensions.visible
import java.time.Duration
import java.util.Date

class FriendsAdapter(friendList : List<FriendModel>,
                     val onFriendClick : (friend: FriendModel) -> Unit,
                     val onRemoveClick : (friend: FriendModel) -> Unit
) : BaseAdapter<FriendModel>(friendList,R.layout.item_friend) {
    override fun renderOnViewHolder(resource: FriendModel, view: View) {
        val image = view.findViewById<ImageView>(R.id.iv_profile_pic)
        val name = view.findViewById<TextView>(R.id.tv_name)
        val lastActivity = view.findViewById<TextView>(R.id.tv_last_activity)
        val removeButton = view.findViewById<ImageView>(R.id.bt_remove_friend)

        // 1. Foto de perfil
        if (resource.image.isNotEmpty())
            image.setImageBitmap(resource.image.toBitmap()!!)

        // 2. Nombre
        name.text = resource.uname

        // 3. Botones
        removeButton.setOnClickListener{onRemoveClick(resource)}

        // 4. Última actividad
        lastActivity.setLastActivity(resource)

        // Clickar en el elemento
        view.setOnClickListener { onFriendClick(resource) }
    }

    private fun TextView.setLastActivity(friend:FriendModel){
        if (friend.lastActivity != null && friend.lastActivity!! > 1000204201L){
            val activityDate = Date().apply { time = friend.lastActivity!! * 1000 }.toInstant()
            val currentDate = Date().apply { time = System.currentTimeMillis() }.toInstant()

            val hoursBetween = Duration.between(activityDate,currentDate).toHours()

            text = if (hoursBetween < 3){
                val minutes = Duration.between(activityDate,currentDate).toMinutes()
                context.getString(com.gf.common.R.string.last_activity_minutes,minutes)
            }
            // Horas
            else if (hoursBetween < 25)
                context.getString(com.gf.common.R.string.last_activity_hours,hoursBetween)
            // Días
            else if (hoursBetween < 169)
                context.getString(com.gf.common.R.string.last_activity_days,(hoursBetween/24))
            // Semanas
            else if (hoursBetween < 673)
                context.getString(com.gf.common.R.string.last_activity_weeks,(hoursBetween/168))
            // Meses
            else if (hoursBetween < 8065)
                context.getString(com.gf.common.R.string.last_activity_months,(hoursBetween/672))
            // Años
            else
                context.getString(com.gf.common.R.string.last_activity_years,(hoursBetween/8064))

            visible()
        }
        else{
            invisible()
        }
    }

    fun friendRemoved(friendId : String) : Boolean {
        val updatedList = resourceListFiltered.filter { it.uid != friendId  }
        actualizarLista(updatedList)

        return updatedList.isEmpty()
    }
}



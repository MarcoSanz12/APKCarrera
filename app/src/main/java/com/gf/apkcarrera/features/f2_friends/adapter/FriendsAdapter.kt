package com.gf.apkcarrera.features.f2_friends.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cotesa.appcore.platform.BaseAdapter
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.common.entity.friend.FriendModel

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

        // Clickar en el elemento
        view.setOnClickListener { onFriendClick(resource) }
    }

    fun friendRemoved(friendId : String) : Boolean {
        val updatedList = resourceListFiltered.filter { it.uid != friendId  }
        actualizarLista(updatedList)

        return updatedList.isEmpty()
    }
}



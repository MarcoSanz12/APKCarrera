package com.gf.apkcarrera.features.f2_friends.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.cotesa.appcore.platform.BaseAdapter
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.common.entity.friend.FriendModel

class FriendsRequestsAdapter(friendList : List<FriendModel>,
                             val onAcceptClick : (friend: FriendModel) -> Unit,
                             val onCancelClick : (friend: FriendModel) -> Unit
) : BaseAdapter<FriendModel>(friendList,R.layout.item_friend_request_receive) {
    override fun renderOnViewHolder(resource: FriendModel, view: View) {
        val image = view.findViewById<ImageView>(R.id.iv_profile_pic)
        val name = view.findViewById<TextView>(R.id.tv_name)
        val addButton = view.findViewById<AppCompatImageButton>(R.id.bt_accept)
        val cancelButton = view.findViewById<AppCompatImageButton>(R.id.bt_remove)

        // 1. Foto de perfil
        if (resource.image.isNotEmpty())
            image.setImageBitmap(resource.image.toBitmap()!!)

        // 2. Nombre
        name.text = resource.uname

        // 3. Botones
        addButton.setOnClickListener{onAcceptClick(resource)}
        cancelButton.setOnClickListener { onCancelClick(resource) }
    }

    fun friendManaged(friendId : String) : Boolean {
        val updatedList = resourceListFiltered.filter { it.uid != friendId  }
        actualizarLista(updatedList)

        return updatedList.isEmpty()
    }
}



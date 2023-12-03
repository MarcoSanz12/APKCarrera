package com.gf.apkcarrera.features.f2_friends.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cotesa.appcore.platform.BaseAdapter
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.common.entity.friend.FriendModel
import com.gf.common.entity.friend.FriendStatus.ADDED_BY_ME
import com.gf.common.entity.friend.FriendStatus.ADDED_ME
import com.gf.common.entity.friend.FriendStatus.FRIEND
import com.gf.common.entity.friend.FriendStatus.UNKNOWN
import com.gf.common.extensions.invisible
import com.gf.common.extensions.visible
import com.google.android.material.button.MaterialButton

class NewFriendsAdapter(friendList : List<FriendModel>,
                        val onAddFriendClick : (friend: FriendModel) -> Unit,
                        val onCancelFriendClick : (friend: FriendModel) -> Unit
) : BaseAdapter<FriendModel>(friendList,R.layout.item_friend_request_send) {
    override fun renderOnViewHolder(resource: FriendModel, view: View) {
        val image = view.findViewById<ImageView>(R.id.iv_profile_pic)
        val name = view.findViewById<TextView>(R.id.tv_name)
        val addButton = view.findViewById<MaterialButton>(R.id.bt_add)
        val cancelButton = view.findViewById<MaterialButton>(R.id.bt_cancel)

        // 1. Foto de perfil
        if (resource.image.isNotEmpty())
            image.setImageBitmap(resource.image.toBitmap()!!)

        // 2. Nombre
        name.text = resource.uname

        // 3. Botón añadir (Si no lo habiamos añadido)
        fun setAddButton() {
            cancelButton.invisible()
            addButton.visible()
            addButton.setOnClickListener{onAddFriendClick(resource)}
        }

        // 4. Botón cancelar añadido (Si le habiamos añadido ya)
        fun setCancelButton(){
            addButton.invisible()
            cancelButton.visible()
            cancelButton.setOnClickListener { onCancelFriendClick(resource) }
        }

        when (resource.friendStatus){
            FRIEND -> {/* No deberia suceder*/ }
            ADDED_BY_ME -> setCancelButton()
            ADDED_ME -> setAddButton()
            UNKNOWN -> setAddButton()
        }
    }

    fun friendRequestSent(friendId : String) : Boolean{
        val updatedList = resourceListFiltered.map {
            if (it.uid == friendId)
                FriendModel(it,ADDED_BY_ME)
            else
                FriendModel(it)
        }
        actualizarLista(updatedList)
        return updatedList.isEmpty()
    }

    fun friendRequestCanceled(friendId : String) : Boolean{
        val updatedList = resourceListFiltered.map {
            if (it.uid == friendId)
                FriendModel(it,UNKNOWN)
            else
                FriendModel(it)
        }
        actualizarLista(updatedList)
        return updatedList.isEmpty()
    }

}


package com.gf.common.entity.user

import com.gf.common.entity.Model
import com.google.firebase.firestore.DocumentSnapshot

class LoginRequest : Model() {

    override var uid: String = ""

    var email: String = ""

    var password: String = ""

    var username: String = ""

    var picture: String = ""

    var searchname : String = ""

    var name: String = ""

    var friendList: List<String> = listOf()

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        username = doc.get("username") as String
        picture = doc.get("picture") as String
        searchname = doc.get("searchname") as String
        name = doc.get("name") as String
        friendList = doc.get("friendList") as List<String>
    }

    override fun setModelToMap()= hashMapOf(
        "username" to username,
        "name" to name,
        "searchname" to searchname,
        "picture" to picture,
        "friendList" to friendList
    )
}
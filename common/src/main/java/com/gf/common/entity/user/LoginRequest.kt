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

    var lastActivity : Long = 0L

    var friendList: List<String> = listOf()

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        username = doc.get("username") as String
        picture = doc.get("picture") as String
        searchname = doc.get("searchname") as String
        name = doc.get("name") as String
        lastActivity = doc.get("lastActivity") as Long
        friendList = doc.get("friendList") as List<String>
    }

    override fun setModelToMap()= hashMapOf(
        "username" to username,
        "name" to name,
        "searchname" to searchname,
        "lastActivity" to lastActivity,
        "picture" to picture,
        "friendList" to friendList
    )
}
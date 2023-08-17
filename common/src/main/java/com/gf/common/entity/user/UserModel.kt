package com.gf.common.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gf.common.entity.Model
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "users")
class UserModel() : Model() {

    constructor(request: LoginRequest) : this() {
        uid = request.uid
        username = request.username
        name = request.name
        picture = request.picture
        friendList = request.friendList
    }

    @PrimaryKey
    @ColumnInfo("uid")
    override var uid: String = ""

    @ColumnInfo("username")
    var username: String = ""

    @ColumnInfo("name")
    var name: String = ""

    @ColumnInfo("picture")
    var picture: String = ""

    @ColumnInfo("friendList")
    var friendList : List<String> = listOf()

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        username = doc.get("username") as String
        picture = doc.get("picture") as String
        name = doc.get("name") as String
        friendList = doc.get("friendList") as List<String>
    }

    override fun setModelToMap() = hashMapOf(
        "username" to username,
        "name" to name,
        "picture" to picture,
        "friendList" to friendList
    )


}
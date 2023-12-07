package com.gf.common.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gf.common.entity.Model
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
class UserModel() : Model() {

    constructor(request: LoginRequest) : this() {
        uid = request.uid
        username = request.username
        searchname = request.searchname
        name = request.name
        picture = request.picture
        friendList = request.friendList
    }

    constructor(doc: DocumentSnapshot) : this(){
        uid = doc.id
        username = doc.get("username") as String
        picture = doc.get("picture") as String
        name = doc.get("name") as String
        friendList = doc.get("friendList") as List<String>
        lastActivity = doc.get("lastActivity") as Long
        searchname = doc.get("searchname") as String
    }
    @PrimaryKey
    @SerializedName("uid")
    override var uid: String = ""

    @SerializedName("username")
    var username: String = ""

    @SerializedName("searchname")
    var searchname : String = ""

    @SerializedName("name")
    var name: String = ""

    @SerializedName("picture")
    var picture: String = ""

    @SerializedName("lastActivity")
    var lastActivity : Long = 0L

    @SerializedName("friendList")
    var friendList : List<String> = listOf()

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        uid = doc.id
        username = doc.get("username") as String
        picture = doc.get("picture") as String
        name = doc.get("name") as String
        friendList = doc.get("friendList") as List<String>
        lastActivity = doc.get("lastActivity") as Long
        searchname = doc.get("searchname") as String
    }

    override fun setModelToMap() = hashMapOf(
        "username" to username,
        "name" to name,
        "picture" to picture,
        "lastActivity" to lastActivity,
        "friendList" to friendList,
        "searchname" to searchname
    )


}
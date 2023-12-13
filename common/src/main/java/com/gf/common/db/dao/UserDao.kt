package com.gf.common.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gf.common.entity.friend.FriendModel
import com.gf.common.entity.friend.FriendStatus
import com.gf.common.entity.toFriendModel
import com.gf.common.entity.user.UserModel

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun getAllUser(): List<UserModel>

    @Transaction
    suspend fun getAllFriendsFromId(uid : String) : List<FriendModel>{
        val user = getUserByUid(uid) ?: return emptyList()
        val friends = getUserByUid(user.friendList)

        return friends.map { it.toFriendModel(user) }.filter { it.friendStatus == FriendStatus.FRIEND }
    }

    @Query("SELECT * FROM users WHERE uid IN (:uids)")
    suspend fun getUserByUid(uids: List<String>): List<UserModel>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): UserModel?

    @Transaction
    suspend fun removeFriend(userId : String, friendId : String){
        val user = getUserByUid(userId) ?: return

        // Filtrar la lista de amigos para quitar el amigo específico
        user.friendList = user.friendList.filter { it != friendId }

        // Actualizar la entidad en la base de datos
        addUser(user)
    }

    @Transaction
    suspend fun addFriend(userId : String, friendId : String){
        val user = getUserByUid(userId) ?: return

        // Filtrar la lista de amigos para quitar el amigo específico
        user.friendList = user.friendList.toMutableList().apply { add(friendId) }

        // Actualizar la entidad en la base de datos
        addUser(user)
    }

    @Query("UPDATE users SET picture = :picture, name = :name WHERE uid = :userId")
    suspend fun updateProfile(userId: String , name:String,picture : String) : Int

    @Query("SELECT friendList FROM users WHERE uid = :uid")
    suspend fun getFriendListByUid(uid: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: UserModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(users: List<UserModel>)
    @Query("DELETE FROM users")
    suspend fun clearUser()
}
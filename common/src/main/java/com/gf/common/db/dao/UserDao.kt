package com.gf.common.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gf.common.entity.user.UserModel

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun getAllUser(): UserModel?

    @Query("SELECT * FROM users WHERE uid IN (:uids)")
    suspend fun getUserByUid(uids: List<String>): List<UserModel>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): UserModel

    @Query("SELECT friendList FROM users WHERE uid = :uid")
    suspend fun getFriendListByUid(uid: String): List<String>

    @Upsert
    suspend fun addUser(user: UserModel)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}
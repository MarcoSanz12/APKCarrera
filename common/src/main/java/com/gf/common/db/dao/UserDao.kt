package com.gf.common.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gf.common.entity.user.UserModel

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllUser(): UserModel?

    @Query("SELECT * FROM users WHERE uid IN (:uids)")
    fun getUserByUid(uids: List<String>): List<UserModel>

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByUid(uid: String): UserModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(user: UserModel)

    @Query("DELETE FROM users")
    fun clearUser()
}
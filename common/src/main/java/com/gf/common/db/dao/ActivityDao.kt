package com.gf.common.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gf.common.entity.activity.ActivityModel

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities")
    suspend fun getAllActivity(): List<ActivityModel>?

    @Query("SELECT * FROM activities WHERE uid IN (:uids)")
    suspend fun getActivityById(uids: List<String>): List<ActivityModel>

    @Query ("SELECT * FROM activities WHERE userid = :userId")
    suspend fun getActivitiesByUserId(userId : String) : List<ActivityModel>

    @Query("SELECT * FROM activities WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): ActivityModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addActivity(activityModel: ActivityModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addActivity(activities: List<ActivityModel>)
    @Query("DELETE FROM activities")
    suspend fun clearActivities()
}
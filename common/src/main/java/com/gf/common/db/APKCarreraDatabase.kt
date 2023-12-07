package com.gf.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gf.common.db.converters.Converters
import com.gf.common.db.dao.ActivityDao
import com.gf.common.db.dao.UserDao
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel

@Database(
    entities = [
        UserModel::class,
        ActivityModel::class
    ], version = 1)
@TypeConverters(Converters::class)
abstract class APKCarreraDatabase : RoomDatabase() {

    //Users
    abstract fun userDao() : UserDao

    //Activities
    abstract fun activityDao() : ActivityDao


}
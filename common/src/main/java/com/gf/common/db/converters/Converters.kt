package com.gf.common.db.converters

import androidx.room.TypeConverter
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.activity.ActivityType
import com.gf.common.entity.activity.RegistryField
import com.gf.common.entity.friend.FriendModel
import com.gf.common.entity.user.UserModel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun toListString(value: String?): List<String> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListString(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toActivityType(value: String?): ActivityType {
        return gson.fromJson(value, ActivityType::class.java)
            ?: ActivityType.RUN // Cambia por el valor predeterminado correcto
    }

    @TypeConverter
    fun fromActivityType(value: ActivityType): String {
        return gson.toJson(value)
    }


    @TypeConverter
    fun toRegistryFrield(value: String?): List<RegistryField> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<RegistryField>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromRegistryField(value: List<RegistryField>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toListInteger(value: String?): List<Int> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListInteger(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toListLong(value: String?): List<Long> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListLong(value: List<Long>): String {
        return gson.toJson(value)
    }
}


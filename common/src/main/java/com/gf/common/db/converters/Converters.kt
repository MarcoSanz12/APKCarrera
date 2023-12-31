package com.gf.common.db.converters

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type

class Converters {
    @TypeConverter
    fun fromStringToInt(value: String?): Int? {
        val listType: Type = object : TypeToken<Int?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntToString(list: Int?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToArraylist(value: String?): List<String>? {
        val listType: Type = object : TypeToken<List<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayListToString(list: List<String?>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringtoInt(value: String?): List<Int> {
        val listType: Type = object : TypeToken<List<Int?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntstoString(list: List<Int?>?): String {
        return Gson().toJson(list)
    }
}
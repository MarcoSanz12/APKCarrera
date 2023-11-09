package com.gf.common.entity.activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gf.common.entity.Model
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "activities")
class ActivityModel() : Model() {

    @PrimaryKey
    @ColumnInfo("uid")
    override var uid: String = ""

    @ColumnInfo("userid")
    var userid : String = ""

    @ColumnInfo("title")
    var title : String = ""

    @ColumnInfo("type")
    var type : ActivityType = ActivityType.RUN

    @ColumnInfo("points")
    var points : List<RegistryField> = emptyList()

    @ColumnInfo("images")
    var images : List<String> = emptyList()

    @ColumnInfo("time")
    var time : List<Int> = emptyList()

    @ColumnInfo("distance")
    var distance : Int = 0

    @ColumnInfo("visibility")
    var visibility : Boolean = true

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        userid = doc.get("userid") as String
        title = doc.get("title") as String
        type = doc.get("type") as ActivityType
        points = doc.get("points") as List<RegistryField>
        images = doc.get("images") as List<String>
        time = doc.get("time") as List<Int>
        distance = doc.get("distance") as Int
        visibility = doc.get("visibility") as Boolean
    }

    override fun setModelToMap(): HashMap<String, Any> =
        hashMapOf(
            "userid" to userid,
            "title" to title,
            "type" to type,
            "points" to points,
            "images" to images,
            "time" to time,
            "distance" to distance,
            "visibility" to visibility
        )
}
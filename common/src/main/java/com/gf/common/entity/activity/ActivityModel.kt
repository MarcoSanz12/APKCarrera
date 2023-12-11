package com.gf.common.entity.activity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gf.common.entity.Model
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.annotations.SerializedName

@Entity(tableName = "activities")
class ActivityModel() : Model() {

    constructor(doc : DocumentSnapshot) : this(){
        uid = doc.id
        userid = doc.get("userid") as String
        timestamp = doc.get("timestamp") as Long
        title = doc.get("title") as String
        type = if ((doc.get("type") as? String) != null)
            ActivityType.valueOf(doc.get("type") as String)
        else
            ActivityType.RUN
        points = doc.get("points") as List<RegistryField>
        images = doc.get("images") as List<String>
        time = doc.get("time") as List<Int>
        distance = (doc.get("distance") as Long).toInt()
        visibility = doc.get("visibility") as Boolean
    }

    @PrimaryKey
    @SerializedName("uid")
    override var uid: String = ""

    @SerializedName("timestamp")
    var timestamp : Long = 0L

    @SerializedName("userid")
    var userid : String = ""

    @SerializedName("title")
    var title : String = ""

    @SerializedName("type")
    var type : ActivityType = ActivityType.RUN

    @SerializedName("points")
    var points : List<RegistryField> = emptyList()

    @SerializedName("images")
    var images : List<String> = emptyList()

    @SerializedName("time")
    var time : List<Int> = emptyList()

    @SerializedName("distance")
    var distance : Int = 0

    @SerializedName("visibility")
    var visibility : Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityModel

        if (uid != other.uid) return false
        if (timestamp != other.timestamp) return false
        if (userid != other.userid) return false
        if (title != other.title) return false
        if (type != other.type) return false
        if (points != other.points) return false
        if (images != other.images) return false
        if (time != other.time) return false
        if (distance != other.distance) return false
        if (visibility != other.visibility) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + userid.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + points.hashCode()
        result = 31 * result + images.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + distance
        result = 31 * result + visibility.hashCode()
        return result
    }

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        uid = doc.id
        userid = doc.get("userid") as String
        timestamp = doc.get("timestamp") as Long
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
            "timestamp" to timestamp,
            "title" to title,
            "type" to type,
            "points" to points,
            "images" to images,
            "time" to time,
            "distance" to distance,
            "visibility" to visibility
        )
}
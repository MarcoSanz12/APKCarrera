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

    @ColumnInfo("points")
     var points : List<List<LatLng>> = listOf()

    @ColumnInfo("time")
     var time : List<Int> = listOf()

    @ColumnInfo("distance")
     var distance : Int = 0

    @ColumnInfo("visibility")
     var visibility : Boolean = true

    override fun getModelFromDoc(doc: DocumentSnapshot) {
        TODO("Not yet implemented")
    }

    override fun setModelToMap(): HashMap<String, Any> {
        TODO("Not yet implemented")
    }
}
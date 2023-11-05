package com.gf.common.entity.activity

import android.location.Location
import com.gf.common.extensions.toLatLng
import com.google.android.gms.maps.model.LatLng

data class RegistryPoint (
    val latLng: LatLng,
    val altitude : Int,
    val distance : Int,
    val time : Int
){
    constructor(location: Location,distance: Int,time: Int) : this(location.toLatLng(),location.altitude.toInt(),distance,time)
}